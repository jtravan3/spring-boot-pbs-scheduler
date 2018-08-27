package com.jtravan.pbs.suppliers;

import com.jtravan.pbs.model.TransactionEvent;

import java.util.Queue;
import java.util.function.Supplier;

public interface TransactionEventSupplier extends Supplier<TransactionEvent> {
    Queue<TransactionEvent> getQueue();
    void handleTransactionEvent(TransactionEvent transactionEvent);
    void clearSupplier();
}
