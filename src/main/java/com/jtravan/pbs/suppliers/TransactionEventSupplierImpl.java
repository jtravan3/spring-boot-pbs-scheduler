package com.jtravan.pbs.suppliers;

import com.jtravan.pbs.model.TransactionEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Component
public class TransactionEventSupplierImpl implements TransactionEventSupplier {

    private final BlockingQueue<TransactionEvent> transactionEventQueue;

    public TransactionEventSupplierImpl() {
        transactionEventQueue = new LinkedBlockingDeque<>();
    }

    @Override
    public TransactionEvent get() {
        try {
            return transactionEventQueue.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Timeout waiting for queue to be populated");
            return null;
        }
    }

    @Override
    public Queue<TransactionEvent> getQueue() {
        return transactionEventQueue;
    }

    @Override
    public void handleTransactionEvent(TransactionEvent transactionEvent) {
        transactionEventQueue.add(transactionEvent);
    }

    @Override
    public void clearSupplier() {
        transactionEventQueue.clear();
    }

}
