package com.jtravan.pbs.scheduler;

import com.jtravan.pbs.model.Operation;
import com.jtravan.pbs.model.ResourceNotification;
import com.jtravan.pbs.model.ResourceOperation;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionNotification;
import com.jtravan.pbs.model.TransactionNotificationType;
import com.jtravan.pbs.services.ResourceNotificationHandler;
import com.jtravan.pbs.services.ResourceNotificationManager;
import com.jtravan.pbs.services.TransactionNotificationHandler;
import com.jtravan.pbs.services.TransactionNotificationManager;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class NoLockingScheduler implements TransactionExecutor,
        ResourceNotificationHandler, TransactionNotificationHandler, Runnable  {

    private ResourceNotificationManager resourceNotificationManager;
    private TransactionNotificationManager scheduleNotificationManager;
    private Transaction transaction;
    private String schedulerName;
    private CyclicBarrier gate;

    private List<ResourceOperation> resourceOperationList;

    private long startTime;
    private long endTime;

    private boolean isAborted;

    public NoLockingScheduler(Transaction transaction, String name, boolean isSandBoxExecution) {
        constructorOperations(transaction, name, isSandBoxExecution);
    }

    private void constructorOperations(Transaction schedule, String name, boolean isSandBoxExecution) {

        this.transaction = schedule;
        this.schedulerName = name;

        isAborted = false;

        resourceOperationList = new LinkedList<ResourceOperation>();

        scheduleNotificationManager = TransactionNotificationManager.getInstance(isSandBoxExecution);
        scheduleNotificationManager.registerHandler(this);

        resourceNotificationManager = scheduleNotificationManager.getResourceNotificationManager();
        resourceNotificationManager.registerHandler(this);

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

    @SuppressWarnings("Duplicates")
    public boolean executeTransaction() {

        if (transaction == null) {
            return false;
        }

        startTime = System.currentTimeMillis();

        // two phase locking - growing phase
        System.out.println("=========================================================");
        System.out.println(schedulerName + ": Two-phase locking growing phase initiated.");
        System.out.println("=========================================================");

        if (isAborted) {
            return handleAbortOperation();
        }

        // two phase locking - shrinking phase
        System.out.println("==========================================================");
        System.out.println(schedulerName + ": Two-phase locking shrinking phase initiated");
        System.out.println("==========================================================");
        for (ResourceOperation resourceOperation : transaction.getResourceOperationList()) {

            if(resourceOperation.isAbortOperation()) {
                isAborted = true;
                System.out.println(schedulerName + ": Execution aborted. Compensation transaction initiated.");
            }

            if (isAborted) {
                return handleAbortOperation();
            }

            try {
                System.out.println(schedulerName + ": Executing operation on Resource " + resourceOperation.getResource());
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

        System.out.println(schedulerName + ": has successfully completed execution!");
        endTime = System.currentTimeMillis();

//        TransactionNotification scheduleNotification = new TransactionNotification();
//        scheduleNotification.setTransaction(transaction);
//        scheduleNotification.setTransactionNotificationType(TransactionNotificationType.TRANSACTION_COMPLETE);
//        scheduleNotificationManager.handleTransactionNotification(scheduleNotification);

//        scheduleNotificationManager.deregisterHandler(this);
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

        System.out.println(schedulerName + ": Execution aborted");
        System.out.println(schedulerName + ": Creating and executing compensation transaction");

        Iterator<ResourceOperation> resourceOperationIterator = resourceOperationList.iterator();
        while(resourceOperationIterator.hasNext()) {

            ResourceOperation resourceOperation = resourceOperationIterator.next();

            if (isAborted) {
                return handleAbortOperation();
            }

            try {
                System.out.println(schedulerName + ": Executing operation on Resource " + resourceOperation.getResource());
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

//                if(transaction != this.transaction) {
//                    // Notify any waiting
//                    synchronized (this) {
//                        System.out.println(schedulerName + ": Notifying just in case we need to start re-run");
//                        notifyAll();
//                    }
//                }

                break;
            default:
                throw new IllegalStateException("Case not handled yet");
        }
    }

}
