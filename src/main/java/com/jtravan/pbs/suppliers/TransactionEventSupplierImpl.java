package com.jtravan.pbs.suppliers;

import com.jtravan.pbs.model.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private Logger LOG = LoggerFactory.getLogger("TransactionEventSupplierImpl");


    public TransactionEventSupplierImpl() {
        transactionEventQueue = new LinkedBlockingDeque<>();
    }

    @Override
    public TransactionEvent get() {
        synchronized (transactionEventQueue) {
            try {
                return transactionEventQueue.poll(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOG.error("Timeout waiting for queue to be populated");
                return null;
            }
        }
    }

    @Override
    public Queue<TransactionEvent> getQueue() {
        return transactionEventQueue;
    }

    @Override
    public void handleTransactionEvent(TransactionEvent transactionEvent) {
        synchronized (transactionEventQueue) {
            transactionEventQueue.add(transactionEvent);
        }
    }

    @Override
    public void clearSupplier() {
        synchronized (transactionEventQueue) {
            transactionEventQueue.clear();
        }
    }

}
