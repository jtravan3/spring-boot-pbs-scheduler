package com.jtravan.pbs.services;

import com.jtravan.pbs.model.TransactionNotification;

/**
 * Created by johnravan on 1/11/17.
 */
public interface TransactionNotificationHandler {
    void handleTransactionNotification(TransactionNotification transactionNotification);
}
