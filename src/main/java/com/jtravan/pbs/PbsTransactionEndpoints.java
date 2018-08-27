package com.jtravan.pbs;

import com.jtravan.pbs.generator.TransactionGenerator;
import com.jtravan.pbs.model.Category;
import com.jtravan.pbs.model.ResourceCategoryDataStructure_READ;
import com.jtravan.pbs.model.ResourceCategoryDataStructure_WRITE;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionEvent;
import com.jtravan.pbs.scheduler.PredictionBasedScheduler;
import com.jtravan.pbs.services.PredictionBasedSchedulerActionService;
import com.jtravan.pbs.services.ResourceNotificationManager;
import com.jtravan.pbs.suppliers.TransactionEventSupplier;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Date;
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

    private final int NUM_OF_OPERATIONS = 10;
    private int schedulerCount = 0;

    public PbsTransactionEndpoints(TransactionEventSupplier transactionEventSupplier, TransactionGenerator transactionGenerator,
                                   ResourceNotificationManager resourceNotificationManager, PredictionBasedSchedulerActionService predictionBasedSchedulerActionService,
                                   ResourceCategoryDataStructure_READ resourceCategoryDataStructure_READ, ResourceCategoryDataStructure_WRITE resourceCategoryDataStructure_WRITE) {

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

        PredictionBasedScheduler pbs1 = createPredictionBasedScheduler(transaction.createCopy());
        PredictionBasedScheduler pbs2 = createPredictionBasedScheduler(transaction.createCopy());

        Transaction t = transaction.createCopy();
        t.setCategory(Category.HCHE);
        PredictionBasedScheduler pbs3 = createPredictionBasedScheduler(t);

        (new Thread(pbs1)).start();
        Thread.sleep(500);
        (new Thread(pbs2)).start();
        Thread.sleep(500);
        (new Thread(pbs3)).start();

        return Flux.zip(interval, transactionEventFlux).map(Tuple2::getT2);
    }

    @Transactional
    @GetMapping(value = "/start/so", produces = MediaType.TEXT_PLAIN_VALUE)
    public String startSystemOut() throws InterruptedException {

        transactionEventSupplier.clearSupplier();
        Transaction transaction = transactionGenerator.generateRandomTransaction(NUM_OF_OPERATIONS);

        PredictionBasedScheduler pbs1 = createPredictionBasedScheduler(transaction.createCopy());
        PredictionBasedScheduler pbs2 = createPredictionBasedScheduler(transaction.createCopy());

        Transaction t = transaction.createCopy();
        t.setCategory(Category.HCHE);
        PredictionBasedScheduler pbs3 = createPredictionBasedScheduler(t);

        (new Thread(pbs1)).start();
        //Thread.sleep(500);
        (new Thread(pbs2)).start();
        Thread.sleep(500);
        (new Thread(pbs3)).start();

        boolean doContinue = true;
        while(doContinue) {
            TransactionEvent event = transactionEventSupplier.get();
            if(event == null) {
                doContinue = false;
            } else {
                System.out.println(event.getValue());
            }
        }

        return "COMPLETE";
    }


    @Async
    public PredictionBasedScheduler createPredictionBasedScheduler(Transaction transaction) {
        PredictionBasedScheduler predictionBasedScheduler =
                new PredictionBasedScheduler(resourceNotificationManager,
                        predictionBasedSchedulerActionService, resourceCategoryDataStructure_READ,
                        resourceCategoryDataStructure_WRITE, transactionEventSupplier);

        predictionBasedScheduler.setTransaction(transaction);
        predictionBasedScheduler.setSchedulerName("Scheduler #" + schedulerCount++);
        return predictionBasedScheduler;
    }
}
