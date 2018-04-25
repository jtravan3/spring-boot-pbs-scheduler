package com.jtravan.pbs.services;

import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionNotification;
import com.jtravan.pbs.model.TransactionNotificationType;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by johnravan on 1/11/17.
 */
public class TransactionNotificationManager implements TransactionNotificationHandler {

    private ResourceNotificationManager resourceNotificationManager;

    private List<TransactionNotificationHandler> handlers;
    private static TransactionNotificationManager theInstance;

    private TransactionNotificationManager(boolean createOneTimeInstance) {
        handlers = new LinkedList<TransactionNotificationHandler>();
        resourceNotificationManager = ResourceNotificationManager.getInstance(createOneTimeInstance);
    }

    public synchronized static final TransactionNotificationManager getInstance(boolean createOneTimeInstance) {

        if(createOneTimeInstance) {
            return new TransactionNotificationManager(createOneTimeInstance);
        } else {
            if(theInstance == null) {
                theInstance = new TransactionNotificationManager(createOneTimeInstance);
            }
            return theInstance;
        }

    }

    public ResourceNotificationManager getResourceNotificationManager() {
        return resourceNotificationManager;
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
