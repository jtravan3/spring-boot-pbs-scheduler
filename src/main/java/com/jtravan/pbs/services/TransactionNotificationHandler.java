package com.jtravan.pbs.services;

import com.jtravan.pbs.model.TransactionNotification;

public interface TransactionNotificationHandler {
    void handleTransactionNotification(TransactionNotification transactionNotification);
}
