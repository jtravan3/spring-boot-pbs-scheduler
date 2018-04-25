package com.jtravan.pbs.services;

import com.jtravan.pbs.model.ResourceOperation;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionEvent;
import com.jtravan.pbs.model.TransactionNotification;
import com.jtravan.pbs.model.TransactionNotificationType;
import com.jtravan.pbs.scheduler.PredictionBasedScheduler;
import com.jtravan.pbs.suppliers.TransactionEventSupplier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Component
public class PredictionBasedSchedulerTransactionNotificationHandler implements TransactionNotificationHandler {

    private List<PredictionBasedScheduler> predictionBasedSchedulerList;
    private final ResourceNotificationManager resourceNotificationManager;
    private final TransactionEventSupplier transactionEventSupplier;

    public PredictionBasedSchedulerTransactionNotificationHandler(ResourceNotificationManager resourceNotificationManager,
                                                                  TransactionEventSupplier transactionEventSupplier) {
        this.resourceNotificationManager = resourceNotificationManager;
        this.transactionEventSupplier = transactionEventSupplier;
    }

    public List<PredictionBasedScheduler> getPredictionBasedSchedulerList() {
        return predictionBasedSchedulerList;
    }

    public void setPredictionBasedSchedulerList(List<PredictionBasedScheduler> predictionBasedSchedulerList) {
        this.predictionBasedSchedulerList = predictionBasedSchedulerList;
    }

    public void handleTransactionEvent(String logString) {
        TransactionEvent transactionEvent = new TransactionEvent(logString, new Date());
        transactionEventSupplier.handleTransactionEvent(transactionEvent);
    }

    public void handleTransactionNotification(TransactionNotification transactionNotification) {

        if (transactionNotification == null) {
            return;
        }

        Transaction transaction = transactionNotification.getTransaction();
        TransactionNotificationType type = transactionNotification.getTransactionNotificationType();

        PredictionBasedScheduler currentPBS = null;
        List<PredictionBasedScheduler> notificationList = new LinkedList<>();
        for (PredictionBasedScheduler pbs : predictionBasedSchedulerList) {
            if (!pbs.getTransaction().equals(transaction)) {
                notificationList.add(pbs);
            } else {
                currentPBS = pbs;
            }
        }

        switch (type) {
            case ABORT:

                if (transaction == currentPBS.getTransaction()) {

                    handleTransactionEvent(currentPBS.getSchedulerName() + ": Aborted due to external scheduler conflict");

                    for (ResourceOperation ro : transaction.getResourceOperationList()) {
                        currentPBS.removeFromCorrectRCDS(ro);
                        resourceNotificationManager.unlock(ro.getResource());
                    }

                    currentPBS.taskStop();
                }

                break;
            case TRANSACTION_COMPLETE:

                if(transaction == currentPBS.getTransaction()) {
                    // Notify any waiting
                    synchronized (currentPBS) {
                        handleTransactionEvent(currentPBS.getSchedulerName() + ": Notifying just in case we need to start re-run");
                        notifyAll();
                    }
                }

                break;
            default:
                throw new IllegalStateException("Case not handled yet");
        }
    }
}
