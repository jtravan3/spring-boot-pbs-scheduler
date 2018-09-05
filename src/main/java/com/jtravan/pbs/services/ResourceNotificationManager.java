package com.jtravan.pbs.services;

import com.jtravan.pbs.model.Operation;
import com.jtravan.pbs.model.ResourceNotification;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionEvent;
import com.jtravan.pbs.model.TransactionNotification;
import com.jtravan.pbs.model.TransactionNotificationType;
import com.jtravan.pbs.suppliers.TransactionEventSupplier;
import com.techprimers.reactive.reactivemongoexample1.model.Employee;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ResourceNotificationManager implements ResourceNotificationHandler {

    private final List<ResourceNotificationHandler> handlers;
    private final TransactionEventSupplier transactionEventSupplier;

    public ResourceNotificationManager(TransactionEventSupplier transactionEventSupplier) {
        this.transactionEventSupplier = transactionEventSupplier;
        handlers = new LinkedList<>();
    }

    public void handleTransactionEvent(String logString) {
        TransactionEvent transactionEvent = new TransactionEvent(logString, new Date());
        transactionEventSupplier.handleTransactionEvent(transactionEvent);
    }

    @SuppressWarnings("Duplicates")
    public synchronized void lock(Employee resource, Operation operation) {

            if (operation == Operation.READ) {
                AtomicBoolean isLocked = resource.isLocked();
                isLocked.set(true);

                ResourceNotification resourceNotification = new ResourceNotification();
                resourceNotification.setResource(resource);
                resourceNotification.setLocked(true);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder
                        .append("Locking Resource ").append(resource);

                handleTransactionEvent(stringBuilder.toString());

                handleResourceNotification(resourceNotification);
            } else {
                AtomicBoolean isLocked = resource.isLocked();
                if (isLocked.compareAndSet(false, true)) {

                    ResourceNotification resourceNotification = new ResourceNotification();
                    resourceNotification.setResource(resource);
                    resourceNotification.setLocked(true);

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder
                            .append("Locking Resource ").append(resource);

                    handleTransactionEvent(stringBuilder.toString());

                    handleResourceNotification(resourceNotification);
                } else {
                    throw new IllegalStateException("Cannot lock already locked resource that has a Write lock. Resource " + resource.toString());
                }
            }

    }

    public Future<String> requestLock(Employee resource, Operation operation) {
        while(true) {
            if (operation == Operation.READ) {
                return new AsyncResult<>("Resource Available");
            } else { // operation is a WRITE
                AtomicBoolean isLocked = resource.isLocked();
                if (isLocked.get()) {
                    return new AsyncResult<>("Resource Available");
                }
            }
        }
    }

    public synchronized void unlock(Employee resource) {

            AtomicBoolean isLocked = resource.isLocked();
            if (isLocked.compareAndSet(true, false)) {

                ResourceNotification resourceNotification = new ResourceNotification();
                resourceNotification.setResource(resource);
                resourceNotification.setLocked(false);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder
                        .append("Unlocking Resource ").append(resource);

                handleTransactionEvent(stringBuilder.toString());

                handleResourceNotification(resourceNotification);
            }

    }

    public void registerHandler (ResourceNotificationHandler handler) {

        if (handler == null) {
            return;
        }

        handleTransactionEvent("Resource Notification Handler registered for notifications");
        handlers.add(handler);

    }

    public void deregisterHandler (ResourceNotificationHandler handler) {

        if (handler == null) {
            return;
        }

        handleTransactionEvent("Resource Notification Handler deregistered for notifications");
        handlers.remove(handler);

    }

    public void deregisterAll() {
        handlers.clear();
    }

    public void handleResourceNotification(ResourceNotification resourceNotification) {

        if (resourceNotification == null) {
            return;
        }

        for (ResourceNotificationHandler handler : handlers) {
            if (handler != null) {
                handler.handleResourceNotification(resourceNotification);
            }
        }

    }

    void abortTransaction(Transaction transaction) {

        if (transaction == null) {
            return;
        }

        TransactionNotification transactionNotification = new TransactionNotification();
        transactionNotification.setTransaction(transaction);
        transactionNotification.setTransactionNotificationType(TransactionNotificationType.ABORT);
        handleResourceNotification(transactionNotification);

    }
}
