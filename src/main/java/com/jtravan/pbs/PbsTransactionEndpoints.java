package com.jtravan.pbs;

import com.jtravan.pbs.generator.TransactionGenerator;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionEvent;
import com.jtravan.pbs.scheduler.PredictionBasedScheduler;
import com.jtravan.pbs.suppliers.TransactionEventSupplier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
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
    private final PredictionBasedScheduler predictionBasedScheduler;
    private final TransactionGenerator transactionGenerator;

    private final int NUM_OF_OPERATIONS = 10;
    private int schedulerCount = 0;

    public PbsTransactionEndpoints(TransactionEventSupplier transactionEventSupplier,
                                   PredictionBasedScheduler predictionBasedScheduler,
                                   TransactionGenerator transactionGenerator) {

        this.transactionEventSupplier = transactionEventSupplier;
        this.predictionBasedScheduler = predictionBasedScheduler;
        this.transactionGenerator = transactionGenerator;
    }

    @GetMapping(value = "/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TransactionEvent> start() {
        Transaction transaction = transactionGenerator.generateRandomTransaction(NUM_OF_OPERATIONS);
        predictionBasedScheduler.setTransaction(transaction);
        predictionBasedScheduler.setSchedulerName("Scheduler #" + schedulerCount++);
        predictionBasedScheduler.run();

        Flux<Long> interval = Flux.interval(Duration.ofMillis(200));
        Flux<TransactionEvent> transactionEventFlux = Flux.fromStream(Stream.generate(transactionEventSupplier))
                .onErrorReturn(new TransactionEvent("PROCESSING COMPLETE", new Date()));

        return Flux.zip(interval, transactionEventFlux).map(Tuple2::getT2);
    }
}
