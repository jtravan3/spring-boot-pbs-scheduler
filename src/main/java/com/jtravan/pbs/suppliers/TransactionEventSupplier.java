package com.jtravan.pbs.suppliers;

import com.jtravan.pbs.model.TransactionEvent;

import java.util.function.Supplier;

public interface TransactionEventSupplier extends Supplier<TransactionEvent> {
    void handleTransactionEvent(TransactionEvent transactionEvent);
}
