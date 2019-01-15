package com.jtravan.pbs;

import com.jtravan.pbs.generator.TransactionGenerator;
import com.jtravan.pbs.model.Category;
import com.jtravan.pbs.model.ResourceCategoryDataStructure_READ;
import com.jtravan.pbs.model.ResourceCategoryDataStructure_WRITE;
import com.jtravan.pbs.model.TestCase;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionEvent;
import com.jtravan.pbs.scheduler.NoLockingScheduler;
import com.jtravan.pbs.scheduler.PredictionBasedScheduler;
import com.jtravan.pbs.scheduler.TraditionalScheduler;
import com.jtravan.pbs.services.MetricsAggregator;
import com.jtravan.pbs.services.PredictionBasedSchedulerActionService;
import com.jtravan.pbs.services.ResourceNotificationManager;
import com.jtravan.pbs.services.TestCaseFactory;
import com.jtravan.pbs.suppliers.TransactionEventSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/rest/pbs")
public class PbsTransactionEndpoints {

    private Logger LOG = LoggerFactory.getLogger("PbsTransactionEndpoints");

    private final TransactionEventSupplier transactionEventSupplier;
    private final TransactionGenerator transactionGenerator;

    private final ResourceNotificationManager resourceNotificationManager;
    private final PredictionBasedSchedulerActionService predictionBasedSchedulerActionService;
    private final ResourceCategoryDataStructure_READ resourceCategoryDataStructure_READ;
    private final ResourceCategoryDataStructure_WRITE resourceCategoryDataStructure_WRITE;
    private final MetricsAggregator metricsAggregator;

    private final int NUM_OF_OPERATIONS = 50;
    private final int NUM_OF_SCHEDULES = 10;

    public PbsTransactionEndpoints(TransactionEventSupplier transactionEventSupplier, TransactionGenerator transactionGenerator,
                                   ResourceNotificationManager resourceNotificationManager, PredictionBasedSchedulerActionService predictionBasedSchedulerActionService,
                                   ResourceCategoryDataStructure_READ resourceCategoryDataStructure_READ, ResourceCategoryDataStructure_WRITE resourceCategoryDataStructure_WRITE,
                                   MetricsAggregator metricsAggregator) {

        this.metricsAggregator = metricsAggregator;
        this.transactionEventSupplier = transactionEventSupplier;
        this.transactionGenerator = transactionGenerator;
        this.resourceNotificationManager = resourceNotificationManager;
        this.predictionBasedSchedulerActionService = predictionBasedSchedulerActionService;
        this.resourceCategoryDataStructure_READ = resourceCategoryDataStructure_READ;
        this.resourceCategoryDataStructure_WRITE = resourceCategoryDataStructure_WRITE;
        Runtime.getRuntime().addShutdownHook(new ThreadServiceShutdownHook());
    }

    @Transactional
    @GetMapping(value = "/start/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TransactionEvent> startFlux() throws InterruptedException {

        Flux<Long> interval = Flux.interval(Duration.ofMillis(200));

        Flux<TransactionEvent> transactionEventFlux = Flux.fromStream(
                Stream.generate(transactionEventSupplier))
                .onErrorReturn(new TransactionEvent("Error occurred", new Date()))
                .takeUntil(transactionEvent -> transactionEvent.getValue().contains("Error occurred"));

        transactionEventSupplier.clearSupplier();
        Transaction transaction = transactionGenerator.generateRandomTransaction(NUM_OF_OPERATIONS);

        PredictionBasedScheduler pbs1 = createPredictionBasedScheduler(transaction.createCopy(), 0);
        PredictionBasedScheduler pbs2 = createPredictionBasedScheduler(transaction.createCopy(), 1);

        Transaction t = transaction.createCopy();
        t.setCategory(Category.HCHE);
        PredictionBasedScheduler pbs3 = createPredictionBasedScheduler(t, 2);

        (new Thread(pbs1)).start();
        Thread.sleep(500);
        (new Thread(pbs2)).start();
        Thread.sleep(500);
        (new Thread(pbs3)).start();

        return Flux.zip(interval, transactionEventFlux).map(Tuple2::getT2);
    }

    @Transactional
    @GetMapping(value = "/start/sametrans/so/{scheduleCount}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String startSameSystemOut(@PathVariable Long scheduleCount) throws InterruptedException {

        metricsAggregator.clear();
        metricsAggregator.setScheduleCount(scheduleCount);

        transactionEventSupplier.clearSupplier();
        Transaction transaction = transactionGenerator.generateRandomTransaction(NUM_OF_OPERATIONS);
        metricsAggregator.setOperationCount(NUM_OF_OPERATIONS);

        metricsAggregator.setPbsStartTime(new Date());
        for(int i = 0; i < scheduleCount; i++) {
            PredictionBasedScheduler pbs = createPredictionBasedScheduler(transaction.createCopy(), i);
            (new Thread(pbs)).start();
            Thread.sleep(200);
        }

        printAndEndProcess();
        metricsAggregator.setTotalTimeWithoutExecution(transaction.getExecutionTime() * scheduleCount);
        metricsAggregator.setPbsEndTime(new Date());

        return metricsAggregator.toString();
    }

    @Transactional
    @GetMapping(value = "/start/difftrans/so/bulk/{testCaseNumber}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String startDifferentSystemOutBulk(@PathVariable Long testCaseNumber) throws InterruptedException, IOException {

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            stringBuilder
                    .append("=============================")
                    .append(System.lineSeparator())
                    .append("Test Run #").append(i)
                    .append(System.lineSeparator());
            metricsAggregator.clear();
            transactionEventSupplier.clearSupplier();
            resourceNotificationManager.deregisterAll();
            resourceCategoryDataStructure_READ.clearAll();
            resourceCategoryDataStructure_WRITE.clearAll();
            stringBuilder
                    .append(startDifferentSystemOut(testCaseNumber))
                    .append(System.lineSeparator())
                    .append("=============================")
                    .append(System.lineSeparator());
        }

        return stringBuilder.toString();

    }

    boolean isLoopEnded = true;

    @Transactional
    @GetMapping(value = "/endLoop", produces = MediaType.TEXT_PLAIN_VALUE)
    public String endLoop() {
        isLoopEnded = true;
        return "I ended the Loop";
    }

    private ExecutorService pbsExecutorService;
    private ExecutorService tsExecutorService;
    private ExecutorService nlExecutorService;

    @Transactional
    @GetMapping(value = "/start/difftrans/so/all", produces = MediaType.TEXT_PLAIN_VALUE)
    public String startDifferentSystemOutLoop() {
        long testCaseNumber = 0;
        isLoopEnded = false;
        while(!isLoopEnded) {
            testCaseNumber++;
            testCaseNumber = testCaseNumber % 7;
            testCaseNumber = (testCaseNumber == 0 ? 7 : testCaseNumber);
            try {
                int i; // loop counter
                metricsAggregator.clear();
                resourceNotificationManager.deregisterAll();
                resourceCategoryDataStructure_READ.clearAll();
                resourceCategoryDataStructure_WRITE.clearAll();
                metricsAggregator.setScheduleCount(NUM_OF_SCHEDULES);
                metricsAggregator.setOperationCount(NUM_OF_OPERATIONS);

                TestCase testCase = TestCaseFactory.getTestCaseByTestCaseNumber(testCaseNumber);

                long executionTime = 0;
                List<Transaction> transactionList = new LinkedList<>();
                for(int count = 0; count < NUM_OF_SCHEDULES; count++) {
                    Transaction transaction = transactionGenerator.generateRandomTransaction(NUM_OF_OPERATIONS);
                    executionTime += transaction.getExecutionTime();
                    transactionList.add(transaction);
                }
                metricsAggregator.setTotalTimeWithoutExecution(executionTime);

                transactionList = transactionGenerator.setCategoriesByTestCase(transactionList, testCase);
                Collections.shuffle(transactionList);

                // PBS Specific
                transactionEventSupplier.clearSupplier();
                metricsAggregator.setPbsStartTime(new Date());
                i = 0;
                pbsExecutorService = Executors.newFixedThreadPool(transactionList.size());
                for(Transaction transaction : transactionList) {
                    PredictionBasedScheduler pbs = createPredictionBasedScheduler(transaction.createCopy(), i);
                    i++;
//            (new Thread(pbs)).start();
                    pbsExecutorService.submit(pbs);
                    Thread.sleep(200);
                }

                printAndEndProcess();
                try {
                    pbsExecutorService.shutdown();
                    pbsExecutorService.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {}

                metricsAggregator.setPbsEndTime(new Date());

                // Traditional Specific
                transactionEventSupplier.clearSupplier();
                metricsAggregator.setTsStartTime(new Date());
                i = 0;
                tsExecutorService = Executors.newFixedThreadPool(transactionList.size());
                for(Transaction transaction : transactionList) {
                    TraditionalScheduler ts = createTraditionalScheduler(transaction.createCopy(), i);
                    i++;
//            (new Thread(ts)).start();
                    tsExecutorService.submit(ts);
                    Thread.sleep(200);
                }

                printAndEndProcess();
                try {
                    tsExecutorService.shutdown();
                    tsExecutorService.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {}

                metricsAggregator.setTsEndTime(new Date());

                // No-locking Specific
                transactionEventSupplier.clearSupplier();
                metricsAggregator.setNlStartTime(new Date());
                i = 0;
                nlExecutorService = Executors.newFixedThreadPool(transactionList.size());
                for(Transaction transaction : transactionList) {
                    NoLockingScheduler nl = createNoLockingScheduler(transaction.createCopy(), i);
                    i++;
//            (new Thread(nl)).start();
                    nlExecutorService.submit(nl);
                    Thread.sleep(200);
                }

                printAndEndProcess();
                try {
                    nlExecutorService.shutdown();
                    nlExecutorService.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {}

                metricsAggregator.setNlEndTime(new Date());
                metricsAggregator.writeToCsvFile(testCaseNumber);
            } catch(Exception ex) {
                continue;
            }
        }

        return "Process completed";
    }

    @Transactional
    @GetMapping(value = "/start/difftrans/so/{testCaseNumber}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String startDifferentSystemOut(@PathVariable Long testCaseNumber) throws InterruptedException, IOException {

        int i; // loop counter
        metricsAggregator.clear();
        resourceNotificationManager.deregisterAll();
        resourceCategoryDataStructure_READ.clearAll();
        resourceCategoryDataStructure_WRITE.clearAll();
        metricsAggregator.setScheduleCount(NUM_OF_SCHEDULES);
        metricsAggregator.setOperationCount(NUM_OF_OPERATIONS);

        TestCase testCase = TestCaseFactory.getTestCaseByTestCaseNumber(testCaseNumber);

        long executionTime = 0;
        List<Transaction> transactionList = new LinkedList<>();
        for(int count = 0; count < NUM_OF_SCHEDULES; count++) {
            Transaction transaction = transactionGenerator.generateRandomTransaction(NUM_OF_OPERATIONS);
            executionTime += transaction.getExecutionTime();
            transactionList.add(transaction);
        }
        metricsAggregator.setTotalTimeWithoutExecution(executionTime);

        transactionList = transactionGenerator.setCategoriesByTestCase(transactionList, testCase);
        Collections.shuffle(transactionList);

        // PBS Specific
        transactionEventSupplier.clearSupplier();
        metricsAggregator.setPbsStartTime(new Date());
        i = 0;
        ExecutorService pbsExecutorService = Executors.newFixedThreadPool(transactionList.size());
        for(Transaction transaction : transactionList) {
            PredictionBasedScheduler pbs = createPredictionBasedScheduler(transaction.createCopy(), i);
            i++;
//            (new Thread(pbs)).start();
            pbsExecutorService.submit(pbs);
            Thread.sleep(200);
        }

        printAndEndProcess();
        try {
            pbsExecutorService.shutdown();
            pbsExecutorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {}

        metricsAggregator.setPbsEndTime(new Date());

        // Traditional Specific
        transactionEventSupplier.clearSupplier();
        metricsAggregator.setTsStartTime(new Date());
        i = 0;
        ExecutorService tsExecutorService = Executors.newFixedThreadPool(transactionList.size());
        for(Transaction transaction : transactionList) {
            TraditionalScheduler ts = createTraditionalScheduler(transaction.createCopy(), i);
            i++;
//            (new Thread(ts)).start();
            tsExecutorService.submit(ts);
            Thread.sleep(200);
        }

        printAndEndProcess();
        try {
            tsExecutorService.shutdown();
            tsExecutorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {}

        metricsAggregator.setTsEndTime(new Date());

        // No-locking Specific
        transactionEventSupplier.clearSupplier();
        metricsAggregator.setNlStartTime(new Date());
        i = 0;
        ExecutorService nlExecutorService = Executors.newFixedThreadPool(transactionList.size());
        for(Transaction transaction : transactionList) {
            NoLockingScheduler nl = createNoLockingScheduler(transaction.createCopy(), i);
            i++;
//            (new Thread(nl)).start();
            nlExecutorService.submit(nl);
            Thread.sleep(200);
        }

        printAndEndProcess();
        try {
            nlExecutorService.shutdown();
            nlExecutorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {}

        metricsAggregator.setNlEndTime(new Date());
        metricsAggregator.writeToCsvFile(testCaseNumber);

        return metricsAggregator.toString();
    }

    private void printAndEndProcess() {

        boolean doContinue = true;
        while(doContinue) {
            TransactionEvent event = transactionEventSupplier.get();
            if(event == null) {
                doContinue = false;
            } else {
                LOG.info(event.getValue());
            }
        }

    }

    private PredictionBasedScheduler createPredictionBasedScheduler(Transaction transaction, int schedulerCount) {
        PredictionBasedScheduler predictionBasedScheduler =
                new PredictionBasedScheduler(resourceNotificationManager,
                        predictionBasedSchedulerActionService, resourceCategoryDataStructure_READ,
                        resourceCategoryDataStructure_WRITE, transactionEventSupplier, metricsAggregator);

        predictionBasedScheduler.setTransaction(transaction);
        predictionBasedScheduler.setSchedulerName("PBS Scheduler #" + schedulerCount);
        return predictionBasedScheduler;
    }

    private TraditionalScheduler createTraditionalScheduler(Transaction transaction, int schedulerCount) {
        TraditionalScheduler traditionalScheduler =
                new TraditionalScheduler(resourceNotificationManager, transactionEventSupplier, metricsAggregator);

        traditionalScheduler.setTransaction(transaction);
        traditionalScheduler.setSchedulerName("Traditional Scheduler #" + schedulerCount);
        return traditionalScheduler;
    }

    private NoLockingScheduler createNoLockingScheduler(Transaction transaction, int schedulerCount) {
        NoLockingScheduler noLockingScheduler =
                new NoLockingScheduler(resourceNotificationManager, transactionEventSupplier, metricsAggregator);

        noLockingScheduler.setTransaction(transaction);
        noLockingScheduler.setSchedulerName("No-locking Scheduler #" + schedulerCount);
        return noLockingScheduler;
    }


    /**
     * TESTING
     */

    @Transactional
    @GetMapping(value = "/start/test/{testCaseNumber}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String testSetCategory_TestCase(@PathVariable Long testCaseNumber) {

        TestCase testCase = TestCaseFactory.getTestCaseByTestCaseNumber(testCaseNumber);

        List<Transaction> transactionList = new LinkedList<>();
        for(int i = 0; i < 100; i++) {
            Transaction t = transactionGenerator.generateRandomTransaction(100);
            transactionList.add(t);
        }

        transactionList = transactionGenerator.setCategoriesByTestCase(transactionList, testCase);

        return countAndPrintList(transactionList);
    }

    private String countAndPrintList(List<Transaction> transactionList) {
        int hcheCount = 0;
        int hcleCount = 0;
        int lcheCount = 0;
        int lcleCount = 0;
        for (Transaction t : transactionList) {
            switch (t.getCategory()) {
                case HCHE:
                    hcheCount++;
                    break;
                case HCLE:
                    hcleCount++;
                    break;
                case LCHE:
                    lcheCount++;
                    break;
                case LCLE:
                    lcleCount++;
                    break;
                default:

            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("Total: ")
                .append(transactionList.size())
                .append(System.lineSeparator())
                .append("HCHE: ")
                .append(hcheCount)
                .append(System.lineSeparator())
                .append("HCLE: ")
                .append(hcleCount)
                .append(System.lineSeparator())
                .append("LCHE: ")
                .append(lcheCount)
                .append(System.lineSeparator())
                .append("LCLE: ")
                .append(lcleCount)
                .append(System.lineSeparator());

        return stringBuilder.toString();
    }

    public class ThreadServiceShutdownHook extends Thread {
        @Override
        public void run() {
            if(pbsExecutorService != null) {
                pbsExecutorService.shutdownNow();
            }

            if (tsExecutorService != null) {
                tsExecutorService.shutdownNow();
            }

            if(nlExecutorService != null) {
                nlExecutorService.shutdownNow();
            }
        }
    }
}
