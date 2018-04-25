package com.jtravan.pbs;

import com.jtravan.pbs.model.TransactionEvent;
import com.jtravan.pbs.suppliers.PbsTransactionEventSupplier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.stream.Stream;

@RestController
@RequestMapping("/rest/pbs")
public class PbsTransactionEndpoints {

    private final PbsTransactionEventSupplier pbsTransactionEventSupplier;

    public PbsTransactionEndpoints(PbsTransactionEventSupplier pbsTransactionEventSupplier) {
        this.pbsTransactionEventSupplier = pbsTransactionEventSupplier;
    }

    @GetMapping(value = "/transaction", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TransactionEvent> transaction() {

        Flux<Long> interval = Flux.interval(Duration.ofSeconds(2));
        Flux<TransactionEvent> transactionEventFlux = Flux.fromStream(Stream.generate(pbsTransactionEventSupplier));

        return Flux.zip(interval, transactionEventFlux).map(Tuple2::getT2);
    }
}
