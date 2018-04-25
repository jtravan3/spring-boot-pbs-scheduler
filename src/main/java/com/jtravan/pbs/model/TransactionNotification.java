package com.jtravan.pbs.model;

/**
 * Created by johnravan on 1/11/17.
 */
public class TransactionNotification {

    private TransactionNotificationType transactionNotificationType;
    private Transaction transaction;

    public TransactionNotificationType getTransactionNotificationType() {
        return transactionNotificationType;
    }

    public void setTransactionNotificationType(TransactionNotificationType transactionNotificationType) {
        this.transactionNotificationType = transactionNotificationType;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
