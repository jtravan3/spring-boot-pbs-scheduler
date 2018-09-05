package com.jtravan.pbs.scheduler;

import com.jtravan.pbs.model.ResourceNotification;
import com.jtravan.pbs.model.ResourceOperation;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionEvent;
import com.jtravan.pbs.services.MetricsAggregator;
import com.jtravan.pbs.services.ResourceNotificationHandler;
import com.jtravan.pbs.services.ResourceNotificationManager;
import com.jtravan.pbs.suppliers.TransactionEventSupplier;
import com.techprimers.reactive.reactivemongoexample1.model.Employee;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class TraditionalScheduler implements TransactionExecutor, ResourceNotificationHandler, Runnable {

    private static final String NEW_LINE = "\n";
    private final Map<Employee, Integer> resourcesWeHaveLockOn;
    private Employee resourceWaitingOn;
    private Transaction transaction;
    private String schedulerName;
    private final ResourceNotificationManager resourceNotificationManager;
    private final TransactionEventSupplier transactionEventSupplier;
    private final MetricsAggregator metricsAggregator;

    public TraditionalScheduler(ResourceNotificationManager resourceNotificationManager,
                                TransactionEventSupplier transactionEventSupplier, MetricsAggregator metricsAggregator) {
        this.resourcesWeHaveLockOn = new HashMap<>();
        this.resourceNotificationManager = resourceNotificationManager;
        this.transactionEventSupplier = transactionEventSupplier;
        this.metricsAggregator = metricsAggregator;
        this.resourceNotificationManager.registerHandler(this);
    }

    public void run() {
        executeTransaction();
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
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

        // two phase locking - growing phase
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("=========================================================")
                .append(NEW_LINE)
                .append(schedulerName)
                .append(": Two-phase locking growing phase initiated." )
                .append(NEW_LINE)
                .append("=========================================================");

        handleTransactionEvent(stringBuilder.toString());

        for (ResourceOperation resourceOperation : transaction.getResourceOperationList()) {

            if(resourceOperation.isAbortOperation()) {
                handleAbortOperation(": Execution aborted from within");
                return false;
            }

            if (resourceOperation.getResource().isLocked().get()) {

                stringBuilder = new StringBuilder();
                stringBuilder
                        .append(schedulerName)
                        .append(": Obtaining lock for Resource ")
                        .append(resourceOperation.getResource());

                handleTransactionEvent(stringBuilder.toString());


                if (resourcesWeHaveLockOn.containsKey(resourceOperation.getResource())) {

                    stringBuilder = new StringBuilder();
                    stringBuilder
                            .append(schedulerName)
                            .append(": Already have lock for Resource ")
                            .append(resourceOperation.getResource())
                            .append(". Continuing execution");

                    handleTransactionEvent(stringBuilder.toString());

                    Integer lockCount = resourcesWeHaveLockOn.get(resourceOperation.getResource());
                    resourcesWeHaveLockOn.put(resourceOperation.getResource(), ++lockCount);
                    continue;
                } else {
                    resourceWaitingOn = resourceOperation.getResource();

                    stringBuilder = new StringBuilder();
                    stringBuilder
                            .append(schedulerName)
                            .append(": Waiting for lock on Resource ")
                            .append(resourceOperation.getResource())
                            .append(" to be released...");

                    handleTransactionEvent(stringBuilder.toString());

                    lockResource(resourceOperation);

                    stringBuilder = new StringBuilder();
                    stringBuilder
                            .append(schedulerName)
                            .append(": Lock for Resource ")
                            .append(resourceOperation.getResource())
                            .append(" released and obtained");

                    handleTransactionEvent(stringBuilder.toString());

                }

            } else {

                synchronized (resourceNotificationManager) {
                    resourcesWeHaveLockOn.put(resourceOperation.getResource(), 1);
                    resourceNotificationManager.lock(resourceOperation.getResource(), resourceOperation.getOperation());
                }

                stringBuilder = new StringBuilder();
                stringBuilder
                        .append(schedulerName)
                        .append(": No lock obtained for Resource ")
                        .append(resourceOperation.getResource());

                handleTransactionEvent(stringBuilder.toString());

            }

        }

        // two phase locking - shrinking phase
        stringBuilder = new StringBuilder();
        stringBuilder
                .append("=========================================================")
                .append(NEW_LINE)
                .append(schedulerName)
                .append(": Two-phase locking shrinking phase initiated." )
                .append(NEW_LINE)
                .append("=========================================================");

        handleTransactionEvent(stringBuilder.toString());

        for (ResourceOperation resourceOperation : transaction.getResourceOperationList()) {

            if(resourceOperation.isAbortOperation()) {
                handleAbortOperation(": Execution aborted from within");
                return false;
            }

            try {
                stringBuilder = new StringBuilder();
                stringBuilder
                        .append(schedulerName)
                        .append(": Executing operation on Resource ")
                        .append(resourceOperation.getResource());

                handleTransactionEvent(stringBuilder.toString());

                Thread.sleep(resourceOperation.getExecutionTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Integer lockCount = resourcesWeHaveLockOn.get(resourceOperation.getResource());
            if (lockCount != null && lockCount == 1) {
                synchronized (resourceNotificationManager) {
                    resourcesWeHaveLockOn.remove(resourceOperation.getResource());
                    resourceNotificationManager.unlock(resourceOperation.getResource());
                }
            } else {
                if (lockCount == null) {
                    resourcesWeHaveLockOn.put(resourceOperation.getResource(), 0);
                } else {
                    resourcesWeHaveLockOn.put(resourceOperation.getResource(), --lockCount);
                }
            }

        }

        stringBuilder = new StringBuilder();
        stringBuilder
                .append(schedulerName)
                .append(": has successfully completed execution!");

        handleTransactionEvent(stringBuilder.toString());

        return true;
    }

    private boolean lockResource(ResourceOperation resourceOperation) {

        StringBuilder stringBuilder;
        long startTime = System.currentTimeMillis(); //fetch starting time
        boolean isDeadLocked = false;

        synchronized (resourceNotificationManager) {
            while (true) {
                try {
                    resourceNotificationManager.lock(resourceOperation.getResource(), resourceOperation.getOperation());
                    break;
                } catch (IllegalStateException ex) {
                    stringBuilder = new StringBuilder();
                    stringBuilder
                            .append(schedulerName)
                            .append(": Resource: " )
                            .append(resourceOperation.getResource())
                            .append(" is already locked. Requesting lock and waiting");

                    handleTransactionEvent(stringBuilder.toString());

                    Future<String> request = resourceNotificationManager.requestLock(resourceOperation.getResource(), resourceOperation.getOperation());

                    while(true) {
                        if(request.isDone()) {
                            stringBuilder = new StringBuilder();
                            stringBuilder
                                    .append(schedulerName)
                                    .append(": Resource: " )
                                    .append(resourceOperation.getResource())
                                    .append(" is released and requesting lock again");

                            handleTransactionEvent(stringBuilder.toString());
                            isDeadLocked =  false;
                            break;
                        }
                    }

                    // If locked for 10 seconds then deadlocked
                    if ((System.currentTimeMillis()-startTime) < 10000) {
                        metricsAggregator.setTsDeadLocked(true);
                        stringBuilder = new StringBuilder();
                        stringBuilder
                                .append(schedulerName)
                                .append(": Deadlocked on Resource: " )
                                .append(resourceOperation.getResource());

                        handleTransactionEvent(stringBuilder.toString());
                        isDeadLocked =  true;
                        break;
                    }
                }
            }
        }

        return isDeadLocked;
    }

    private boolean handleAbortOperation(String reason) {

        synchronized (resourceNotificationManager) {
            for (ResourceOperation resourceOperation : transaction.getResourceOperationList()) {
                resourceNotificationManager.unlock(resourceOperation.getResource());
            }
        }

        long abortCount = metricsAggregator.getTsAbortCount();
        metricsAggregator.setTsAbortCount(++abortCount);

        resourcesWeHaveLockOn.clear();
        resourceWaitingOn = null;

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

        if (!resourceNotification.isLocked()) {
            if (resourceNotification.getResource() == resourceWaitingOn) {

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder
                        .append(schedulerName)
                        .append(": Resource, ")
                        .append(resourceNotification.getResource())
                        .append(", that we have been waiting on, has been released and unlocked");

                handleTransactionEvent(stringBuilder.toString());
            }
        }

    }
}
