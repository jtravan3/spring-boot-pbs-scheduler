package com.jtravan.pbs.services;

import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionNotification;
import com.jtravan.pbs.model.TransactionNotificationType;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class TransactionNotificationManager implements TransactionNotificationHandler {

    private final ResourceNotificationManager resourceNotificationManager;
    private final List<TransactionNotificationHandler> handlers;

    public TransactionNotificationManager(ResourceNotificationManager resourceNotificationManager) {
        handlers = new LinkedList<>();
        this.resourceNotificationManager = resourceNotificationManager;
    }

    public void abortTransaction(Transaction transaction) {

        if (transaction == null) {
            return;
        }

        TransactionNotification transactionNotification = new TransactionNotification();
        transactionNotification.setTransaction(transaction);
        transactionNotification.setTransactionNotificationType(TransactionNotificationType.ABORT);
        handleTransactionNotification(transactionNotification);

    }

    public void registerHandler (TransactionNotificationHandler handler) {

        if (handler == null) {
            return;
        }

        System.out.println("Transaction Notification Handler registered for notifications");
        handlers.add(handler);

    }

    public void deregisterHandler (TransactionNotificationHandler handler) {

        if (handler == null) {
            return;
        }

        System.out.println("Transaction Notification Handler deregistered for notifications");
        handlers.remove(handler);

    }

    public void handleTransactionNotification(TransactionNotification transactionNotification) {

        if (transactionNotification == null) {
            return;
        }

        for (TransactionNotificationHandler handler : handlers) {
            handler.handleTransactionNotification(transactionNotification);
        }

    }

}
