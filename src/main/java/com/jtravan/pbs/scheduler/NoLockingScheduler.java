package com.jtravan.pbs.scheduler;

import com.jtravan.pbs.model.Operation;
import com.jtravan.pbs.model.ResourceNotification;
import com.jtravan.pbs.model.ResourceOperation;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionEvent;
import com.jtravan.pbs.model.TransactionNotification;
import com.jtravan.pbs.model.TransactionNotificationType;
import com.jtravan.pbs.services.ResourceNotificationHandler;
import com.jtravan.pbs.services.ResourceNotificationManager;
import com.jtravan.pbs.services.TransactionNotificationHandler;
import com.jtravan.pbs.services.TransactionNotificationManager;
import com.jtravan.pbs.suppliers.TransactionEventSupplier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

@Component
@SessionScope
public class NoLockingScheduler implements TransactionExecutor,
        ResourceNotificationHandler, TransactionNotificationHandler, Runnable  {

    private final ResourceNotificationManager resourceNotificationManager;
    private final TransactionNotificationManager transactionNotificationManager;
    private final TransactionEventSupplier transactionEventSupplier;
    private Transaction transaction;
    private String schedulerName;
    private CyclicBarrier gate;

    private List<ResourceOperation> resourceOperationList;

    private long startTime;
    private long endTime;

    private boolean isAborted;

    private static final String NEW_LINE = "\n";

    public NoLockingScheduler(TransactionNotificationManager transactionNotificationManager,
                              ResourceNotificationManager resourceNotificationManager,
                              TransactionEventSupplier transactionEventSupplier) {

        isAborted = false;

        resourceOperationList = new LinkedList<>();

        this.transactionNotificationManager = transactionNotificationManager;
        this.transactionNotificationManager.registerHandler(this);

        this.resourceNotificationManager = resourceNotificationManager;
        this.resourceNotificationManager.registerHandler(this);

        this.transactionEventSupplier = transactionEventSupplier;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setGate(CyclicBarrier gate) { this.gate = gate; }

    public void handleTransactionEvent(String logString) {
        TransactionEvent transactionEvent = new TransactionEvent(logString, new Date());
        transactionEventSupplier.handleTransactionEvent(transactionEvent);
    }

    @SuppressWarnings("Duplicates")
    public boolean executeTransaction() {

        if (transaction == null) {
            return false;
        }

        startTime = System.currentTimeMillis();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("=========================================================")
                .append(NEW_LINE)
                .append(schedulerName)
                .append(": No locking scheduler initiated." )
                .append(NEW_LINE)
                .append("=========================================================");

        handleTransactionEvent(stringBuilder.toString());

        if (isAborted) {
            return handleAbortOperation();
        }

        for (ResourceOperation resourceOperation : transaction.getResourceOperationList()) {

            if(resourceOperation.isAbortOperation()) {
                isAborted = true;

                stringBuilder = new StringBuilder();
                stringBuilder
                        .append(schedulerName)
                        .append(": Execution aborted. Compensation transaction initiated." );

                handleTransactionEvent(stringBuilder.toString());
            }

            if (isAborted) {
                return handleAbortOperation();
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
                    resourceOperationList.add(resourceOperation);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (isAborted) {
                return handleAbortOperation();
            }

        }

        stringBuilder = new StringBuilder();
        stringBuilder
                .append(schedulerName)
                .append(": has successfully completed execution!");

        handleTransactionEvent(stringBuilder.toString());

        endTime = System.currentTimeMillis();

//        TransactionNotification scheduleNotification = new TransactionNotification();
//        scheduleNotification.setTransaction(transaction);
//        scheduleNotification.setTransactionNotificationType(TransactionNotificationType.TRANSACTION_COMPLETE);
//        transactionNotificationManager.handleTransactionNotification(scheduleNotification);

//        transactionNotificationManager.deregisterHandler(this);
//        resourceNotificationManager.deregisterHandler(this);

        return true;
    }

    public void run() {
        try {
            gate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }

        executeTransaction();

    }

    @SuppressWarnings("Duplicates")
    private boolean handleAbortOperation() {

        isAborted = false;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(schedulerName)
                .append(": Execution aborted")
                .append(NEW_LINE)
                .append(schedulerName)
                .append(": Creating and executing compensation transaction");

        handleTransactionEvent(stringBuilder.toString());

        Iterator<ResourceOperation> resourceOperationIterator = resourceOperationList.iterator();
        while(resourceOperationIterator.hasNext()) {

            ResourceOperation resourceOperation = resourceOperationIterator.next();

            if (isAborted) {
                return handleAbortOperation();
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
                    resourceOperationList.add(resourceOperation);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (isAborted) {
                return handleAbortOperation();
            }

        }

        return false;
    }

    @SuppressWarnings("Duplicates")
    public void handleResourceNotification(ResourceNotification resourceNotification) {

        if (resourceNotification == null) {
            return;
        }

    }

    public void handleTransactionNotification(TransactionNotification transactionNotification) {

        if (transactionNotification == null) {
            return;
        }

        Transaction transaction = transactionNotification.getTransaction();
        TransactionNotificationType type = transactionNotification.getTransactionNotificationType();

        switch (type) {
            case ABORT:
            case TRANSACTION_COMPLETE:

                break;
            default:
                throw new IllegalStateException("Case not handled yet");
        }
    }

}
