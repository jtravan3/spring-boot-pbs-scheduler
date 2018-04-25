package com.jtravan.pbs.suppliers;

import com.jtravan.pbs.model.TransactionEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

@Component
public class TransactionEventSupplierImpl implements TransactionEventSupplier {

    private final Queue<TransactionEvent> transactionEventQueue;

    public TransactionEventSupplierImpl() {
        transactionEventQueue = new LinkedList<>();
    }

    @Override
    public TransactionEvent get() {
        return transactionEventQueue.poll();
    }

    @Override
    @Async
    public void handleTransactionEvent(TransactionEvent transactionEvent) {
        transactionEventQueue.add(transactionEvent);
    }
}
