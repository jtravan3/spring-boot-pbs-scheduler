package com.jtravan.pbs.services;

import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionEvent;
import com.jtravan.pbs.model.TransactionNotification;
import com.jtravan.pbs.model.TransactionNotificationType;
import com.jtravan.pbs.suppliers.TransactionEventSupplier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Component
public class TransactionNotificationManager implements TransactionNotificationHandler {

    private final List<TransactionNotificationHandler> handlers;
    private final TransactionEventSupplier transactionEventSupplier;

    public TransactionNotificationManager(TransactionEventSupplier transactionEventSupplier) {
        handlers = new LinkedList<>();
        this.transactionEventSupplier = transactionEventSupplier;
    }

    public void handleTransactionEvent(String logString) {
        TransactionEvent transactionEvent = new TransactionEvent(logString, new Date());
        transactionEventSupplier.handleTransactionEvent(transactionEvent);
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

        handleTransactionEvent("Transaction Notification Handler registered for notifications");
        handlers.add(handler);

    }

    public void deregisterHandler (TransactionNotificationHandler handler) {

        if (handler == null) {
            return;
        }

        handleTransactionEvent("Transaction Notification Handler deregistered for notifications");
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
