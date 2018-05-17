package com.jtravan.pbs.suppliers;

import com.jtravan.pbs.model.TransactionEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

@Component
@SessionScope
public class TransactionEventSupplierImpl implements TransactionEventSupplier {

    private final Queue<TransactionEvent> transactionEventQueue;
    private int count;

    public TransactionEventSupplierImpl() {
        transactionEventQueue = new LinkedList<>();
    }

    @Override
    public TransactionEvent get() {
        if(transactionEventQueue.peek() == null && count < 10) {
            count++;
            return new TransactionEvent("WAITING...", new Date());
        } else {
            count = 0;
            if (transactionEventQueue.peek().isComplete()) {
                transactionEventQueue.poll();
                return null;
            } else {
                return transactionEventQueue.poll();
            }
        }
    }

    @Override
    @Async
    public void handleTransactionEvent(TransactionEvent transactionEvent) {
        transactionEventQueue.add(transactionEvent);
    }
}
