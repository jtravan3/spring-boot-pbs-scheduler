package com.jtravan.pbs.scheduler;

import com.jtravan.pbs.model.Operation;
import com.jtravan.pbs.model.ResourceNotification;
import com.jtravan.pbs.model.ResourceOperation;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionEvent;
import com.jtravan.pbs.services.MetricsAggregator;
import com.jtravan.pbs.services.ResourceNotificationHandler;
import com.jtravan.pbs.services.ResourceNotificationManager;
import com.jtravan.pbs.suppliers.TransactionEventSupplier;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class NoLockingScheduler implements TransactionExecutor,
        ResourceNotificationHandler, Runnable  {

    private final ResourceNotificationManager resourceNotificationManager;
    private final TransactionEventSupplier transactionEventSupplier;
    private final MetricsAggregator metricsAggregator;
    private Transaction transaction;
    private String schedulerName;

    private static final String NEW_LINE = "\n";

    public NoLockingScheduler(ResourceNotificationManager resourceNotificationManager,
                              TransactionEventSupplier transactionEventSupplier,
                              MetricsAggregator metricsAggregator) {

        this.resourceNotificationManager = resourceNotificationManager;
        this.resourceNotificationManager.registerHandler(this);

        this.transactionEventSupplier = transactionEventSupplier;
        this.metricsAggregator = metricsAggregator;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void handleTransactionEvent(String logString) {
        TransactionEvent transactionEvent = new TransactionEvent(logString, new Date());
        transactionEventSupplier.handleTransactionEvent(transactionEvent);
    }

    @SuppressWarnings("Duplicates")
    public boolean executeTransaction() {

        if (transaction == null) {
            return false;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("=========================================================")
                .append(NEW_LINE)
                .append(schedulerName)
                .append(": No locking scheduler initiated." )
                .append(NEW_LINE)
                .append("=========================================================");

        handleTransactionEvent(stringBuilder.toString());

        for (ResourceOperation resourceOperation : transaction.getResourceOperationList()) {

            if (resourceOperation.isAbortOperation()) {
                handleAbortOperation(": Execution aborted from within");
                return false;
            }

            if (resourceOperation.getResource().isLocked().get() && resourceOperation.getOperation() == Operation.WRITE) {
                metricsAggregator.setNlConsistencyLost(true);

                stringBuilder = new StringBuilder();
                stringBuilder
                        .append(schedulerName)
                        .append(": CONFLICT! Consistency Lost! Stepping on another transactions toes ")
                        .append(resourceOperation.getResource());

                handleTransactionEvent(stringBuilder.toString());
            }

            if (!resourceOperation.getResource().isLocked().get() && resourceOperation.getOperation() == Operation.WRITE) {
                resourceNotificationManager.lock(resourceOperation.getResource(), resourceOperation.getOperation());
            }

            try {

                stringBuilder = new StringBuilder();
                stringBuilder
                        .append(schedulerName)
                        .append(": Executing operation on Resource ")
                        .append(resourceOperation.getResource());

                handleTransactionEvent(stringBuilder.toString());

                Thread.sleep(resourceOperation.getExecutionTime());

                if (resourceOperation.getOperation() == Operation.WRITE) {
                    resourceNotificationManager.unlock(resourceOperation.getResource());
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        stringBuilder = new StringBuilder();
        stringBuilder
                .append(schedulerName)
                .append(": has successfully completed execution!");

        handleTransactionEvent(stringBuilder.toString());

        return true;
    }

    public void run() {

        executeTransaction();

    }

    private boolean handleAbortOperation(String reason) {

        for (ResourceOperation resourceOperation : transaction.getResourceOperationList()) {
            resourceNotificationManager.unlock(resourceOperation.getResource());
        }

        long abortCount = metricsAggregator.getNlAbortCount();
        metricsAggregator.setNlAbortCount(++abortCount);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(schedulerName)
                .append(reason)
                .append(NEW_LINE)
                .append(schedulerName)
                .append(": Waiting and trying execution again");

        handleTransactionEvent(stringBuilder.toString());

        return false;
    }

    @SuppressWarnings("Duplicates")
    public void handleResourceNotification(ResourceNotification resourceNotification) {

        if (resourceNotification == null) {
            return;
        }

    }

}
