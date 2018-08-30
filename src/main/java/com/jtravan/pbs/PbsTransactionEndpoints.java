package com.jtravan.pbs;

import com.jtravan.pbs.generator.TransactionGenerator;
import com.jtravan.pbs.model.Category;
import com.jtravan.pbs.model.ResourceCategoryDataStructure_READ;
import com.jtravan.pbs.model.ResourceCategoryDataStructure_WRITE;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionEvent;
import com.jtravan.pbs.scheduler.NoLockingScheduler;
import com.jtravan.pbs.scheduler.PredictionBasedScheduler;
import com.jtravan.pbs.scheduler.TraditionalScheduler;
import com.jtravan.pbs.services.MetricsAggregator;
import com.jtravan.pbs.services.PredictionBasedSchedulerActionService;
import com.jtravan.pbs.services.ResourceNotificationManager;
import com.jtravan.pbs.suppliers.TransactionEventSupplier;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

@RestController
@RequestMapping("/rest/pbs")
public class PbsTransactionEndpoints {

    private final TransactionEventSupplier transactionEventSupplier;
    private final TransactionGenerator transactionGenerator;

    private final ResourceNotificationManager resourceNotificationManager;
    private final PredictionBasedSchedulerActionService predictionBasedSchedulerActionService;
    private final ResourceCategoryDataStructure_READ resourceCategoryDataStructure_READ;
    private final ResourceCategoryDataStructure_WRITE resourceCategoryDataStructure_WRITE;
    private final MetricsAggregator metricsAggregator;

    private final int NUM_OF_OPERATIONS = 10;

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
    @GetMapping(value = "/start/difftrans/so/{scheduleCount}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String startDifferentSystemOut(@PathVariable Long scheduleCount) throws InterruptedException {

        int i; // loop counter
        metricsAggregator.clear();
        metricsAggregator.setScheduleCount(scheduleCount);
        metricsAggregator.setOperationCount(NUM_OF_OPERATIONS);

        long executionTime = 0;
        List<Transaction> transactionList = new LinkedList<>();
        for(int count = 0; count < scheduleCount; count++) {
            Transaction transaction = transactionGenerator.generateRandomTransaction(NUM_OF_OPERATIONS);
            executionTime += transaction.getExecutionTime();
            transactionList.add(transaction);
        }
        metricsAggregator.setTotalTimeWithoutExecution(executionTime);

        // PBS Specific
        transactionEventSupplier.clearSupplier();
        metricsAggregator.setPbsStartTime(new Date());
        i = 0;
        for(Transaction transaction : transactionList) {
            PredictionBasedScheduler pbs = createPredictionBasedScheduler(transaction.createCopy(), i);
            i++;
            (new Thread(pbs)).start();
            Thread.sleep(200);
        }

        printAndEndProcess();
        metricsAggregator.setPbsEndTime(new Date());

        // Traditional Specific
        transactionEventSupplier.clearSupplier();
        metricsAggregator.setTsStartTime(new Date());
        i = 0;
        for(Transaction transaction : transactionList) {
            TraditionalScheduler ts = createTraditionalScheduler(transaction.createCopy(), i);
            i++;
            (new Thread(ts)).start();
            Thread.sleep(200);
        }

        printAndEndProcess();
        metricsAggregator.setTsEndTime(new Date());

        // No-locking Specific
        transactionEventSupplier.clearSupplier();
        metricsAggregator.setNlStartTime(new Date());
        i = 0;
        for(Transaction transaction : transactionList) {
            NoLockingScheduler nl = createNoLockingScheduler(transaction.createCopy(), i);
            i++;
            (new Thread(nl)).start();
            Thread.sleep(200);
        }

        printAndEndProcess();
        metricsAggregator.setNlEndTime(new Date());

        return metricsAggregator.toString();
    }

    private void printAndEndProcess() {

        boolean doContinue = true;
        while(doContinue) {
            TransactionEvent event = transactionEventSupplier.get();
            if(event == null) {
                doContinue = false;
            } else {
                System.out.println(event.getValue());
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
}
