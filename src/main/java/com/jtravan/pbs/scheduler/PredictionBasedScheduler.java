package com.jtravan.pbs.scheduler;

import com.jtravan.pbs.model.Action;
import com.jtravan.pbs.model.Operation;
import com.jtravan.pbs.model.ResourceCategoryDataStructure_READ;
import com.jtravan.pbs.model.ResourceCategoryDataStructure_WRITE;
import com.jtravan.pbs.model.ResourceNotification;
import com.jtravan.pbs.model.ResourceOperation;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionEvent;
import com.jtravan.pbs.services.PredictionBasedSchedulerActionService;
import com.jtravan.pbs.services.ResourceNotificationHandler;
import com.jtravan.pbs.services.ResourceNotificationManager;
import com.jtravan.pbs.suppliers.TransactionEventSupplier;
import com.techprimers.reactive.reactivemongoexample1.model.Employee;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
@SessionScope
public class PredictionBasedScheduler implements TransactionExecutor,
        ResourceNotificationHandler, Runnable {

    private final PredictionBasedSchedulerActionService predictionBasedSchedulerActionService;
    private final ResourceCategoryDataStructure_READ resourceCategoryDataStructure_READ;
    private final ResourceCategoryDataStructure_WRITE resourceCategoryDataStructure_WRITE;
    private final ResourceNotificationManager resourceNotificationManager;
    private final TransactionEventSupplier transactionEventSupplier;
    private final Map<Employee, Integer> resourcesWeHaveLockOn_Read;
    private final Map<Employee, Integer> resourcesWeHaveLockOn_Write;
    private Employee resourceWaitingOn;
    private Transaction transaction;
    private String schedulerName;

    private static final String NEW_LINE = "\n";

    public PredictionBasedScheduler(ResourceNotificationManager resourceNotificationManager,
                                    PredictionBasedSchedulerActionService predictionBasedSchedulerActionService,
                                    ResourceCategoryDataStructure_READ resourceCategoryDataStructure_READ,
                                    ResourceCategoryDataStructure_WRITE resourceCategoryDataStructure_WRITE,
                                    TransactionEventSupplier transactionEventSupplier) {

        resourcesWeHaveLockOn_Read = new HashMap<>();
        resourcesWeHaveLockOn_Write = new HashMap<>();

        this.transactionEventSupplier = transactionEventSupplier;
        this.resourceNotificationManager = resourceNotificationManager;
        this.resourceNotificationManager.registerHandler(this);
        this.predictionBasedSchedulerActionService = predictionBasedSchedulerActionService;
        this.resourceCategoryDataStructure_READ = resourceCategoryDataStructure_READ;
        this.resourceCategoryDataStructure_WRITE = resourceCategoryDataStructure_WRITE;
    }

    public void resetScheduler() {
        resourcesWeHaveLockOn_Read.clear();
        resourcesWeHaveLockOn_Write.clear();
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

    private boolean growingPhaseSuccessful() {

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
                handleAbortOperation();
                return false;
            }

            Action action = predictionBasedSchedulerActionService
                    .determineSchedulerAction(resourceCategoryDataStructure_READ,
                            resourceCategoryDataStructure_WRITE, resourceOperation);

            stringBuilder = new StringBuilder();
            stringBuilder
                    .append(schedulerName)
                    .append(": Action for resource" )
                    .append(resourceOperation.getResource())
                    .append(": ").append(action.name());

            handleTransactionEvent(stringBuilder.toString());

            switch (action) {
                case DECLINE:

                    if (resourceOperation.getOperation() == Operation.WRITE &&
                            resourcesWeHaveLockOn_Write.containsKey(resourceOperation.getResource())) {

                        stringBuilder = new StringBuilder();
                        stringBuilder
                                .append(schedulerName)
                                .append(": Already have lock for Resource " )
                                .append(resourceOperation.getResource())
                                .append(". Continuing execution");

                        handleTransactionEvent(stringBuilder.toString());

                        Integer lockCount = resourcesWeHaveLockOn_Write.get(resourceOperation.getResource());
                        lockCount++;
                        insertIntoCorrectRCDS(resourceOperation);
                        resourcesWeHaveLockOn_Write.put(resourceOperation.getResource(), lockCount);
                        continue;
                    } else if(resourceOperation.getOperation() == Operation.READ &&
                            (resourcesWeHaveLockOn_Read.containsKey(resourceOperation.getResource()) ||
                                    resourcesWeHaveLockOn_Write.containsKey(resourceOperation.getResource() ))) {

                        stringBuilder = new StringBuilder();
                        stringBuilder
                                .append(schedulerName)
                                .append(": Already have lock for Resource " )
                                .append(resourceOperation.getResource())
                                .append(". Continuing execution");

                        handleTransactionEvent(stringBuilder.toString());

                        Integer lockCount = resourcesWeHaveLockOn_Read.get(resourceOperation.getResource());
                        lockCount++;
                        insertIntoCorrectRCDS(resourceOperation);
                        resourcesWeHaveLockOn_Read.put(resourceOperation.getResource(), lockCount);
                        continue;
                    } else {
                        resourceWaitingOn = resourceOperation.getResource();

                        stringBuilder = new StringBuilder();
                        stringBuilder
                                .append(schedulerName)
                                .append(": Waiting for lock on Resource " )
                                .append(resourceOperation.getResource())
                                .append(" to be released...");

                        handleTransactionEvent(stringBuilder.toString());

                        Future<String> request = resourceNotificationManager.requestLock(resourceOperation.getResource(), resourceOperation.getOperation());

                        while(true) {
                            if(request.isDone()) {
                                break;
                            }
                        }

                        stringBuilder = new StringBuilder();
                        stringBuilder
                                .append(schedulerName)
                                .append(": Lock for Resource " )
                                .append(resourceOperation.getResource())
                                .append(" released and obtained");

                        handleTransactionEvent(stringBuilder.toString());

                        insertIntoCorrectRCDS(resourceOperation);
                        resourcesWeHaveLockOn_Read.put(resourceOperation.getResource(), 1);
                        resourceNotificationManager.lock(resourceOperation.getResource(), resourceOperation.getOperation());
                    }

                    break;
                case ELEVATE:

                    stringBuilder = new StringBuilder();
                    stringBuilder
                            .append(schedulerName)
                            .append(": Other transaction abort initiated. Now locking resource...");

                    handleTransactionEvent(stringBuilder.toString());

                    resourceWaitingOn = resourceOperation.getResource();
                    resourceNotificationManager.lock(resourceOperation.getResource(), resourceOperation.getOperation());
                    if(resourceOperation.getOperation() == Operation.READ) {
                        resourcesWeHaveLockOn_Read.put(resourceOperation.getResource(), 1);
                    } else {
                        resourcesWeHaveLockOn_Write.put(resourceOperation.getResource(), 1);
                    }

                    break;
                case GRANT:

                    insertIntoCorrectRCDS(resourceOperation);

                    if(resourceOperation.getOperation() == Operation.WRITE &&
                            resourcesWeHaveLockOn_Write.containsKey(resourceOperation.getResource())) {

                        stringBuilder = new StringBuilder();
                        stringBuilder
                                .append(schedulerName)
                                .append(": Already have lock for Resource " )
                                .append(resourceOperation.getResource())
                                .append(". Continuing execution");

                        handleTransactionEvent(stringBuilder.toString());

                        Integer lockCount = resourcesWeHaveLockOn_Write.get(resourceOperation.getResource());
                        lockCount++;
                        resourcesWeHaveLockOn_Write.put(resourceOperation.getResource(), lockCount);
                        continue;
                    } else if(resourceOperation.getOperation() == Operation.READ &&
                            resourcesWeHaveLockOn_Read.containsKey(resourceOperation.getResource())) {

                        stringBuilder = new StringBuilder();
                        stringBuilder
                                .append(schedulerName)
                                .append(": Already have lock for Resource " )
                                .append(resourceOperation.getResource())
                                .append(". Continuing execution");

                        handleTransactionEvent(stringBuilder.toString());

                        Integer lockCount = resourcesWeHaveLockOn_Read.get(resourceOperation.getResource());
                        lockCount++;
                        resourcesWeHaveLockOn_Read.put(resourceOperation.getResource(), lockCount);
                        continue;
                    } else {

                        stringBuilder = new StringBuilder();
                        stringBuilder
                                .append(schedulerName)
                                .append(": No lock obtained for Resource" )
                                .append(resourceOperation.getResource())
                                .append(". Locking now...");

                        handleTransactionEvent(stringBuilder.toString());

                        if (resourceOperation.getOperation() == Operation.READ) {
                            resourcesWeHaveLockOn_Read.put(resourceOperation.getResource(), 1);
                        } else {
                            resourcesWeHaveLockOn_Write.put(resourceOperation.getResource(), 1);
                        }

                        resourceNotificationManager.lock(resourceOperation.getResource(), resourceOperation.getOperation());
                    }

                    break;
                default:
                    throw new IllegalArgumentException("Case not handled.");
            }

        }

        return true;

    }

    private boolean shrinkingPhaseSuccessful() {

        // two phase locking - shrinking phase
        StringBuilder stringBuilder = new StringBuilder();
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
                handleAbortOperation();
                return false;
            }

            try {

                stringBuilder = new StringBuilder();
                stringBuilder
                        .append(schedulerName)
                        .append(": Executing operation on Resource " )
                        .append(resourceOperation.getResource());

                handleTransactionEvent(stringBuilder.toString());

                Thread.sleep(resourceOperation.getExecutionTime());
            } catch (InterruptedException e) {

            }

            Integer lockCount;
            if(resourceOperation.getOperation() == Operation.READ) {
                lockCount = resourcesWeHaveLockOn_Read.get(resourceOperation.getResource());
            } else {
                lockCount = resourcesWeHaveLockOn_Write.get(resourceOperation.getResource());
            }

            if (lockCount != null && lockCount == 1) {
                if (resourceOperation.getOperation() == Operation.READ) {
                    resourcesWeHaveLockOn_Read.remove(resourceOperation.getResource());
                } else {
                    resourcesWeHaveLockOn_Write.remove(resourceOperation.getResource());
                }

                stringBuilder = new StringBuilder();
                stringBuilder
                        .append(schedulerName)
                        .append(": No longer needing the lock. Releasing lock..." );

                handleTransactionEvent(stringBuilder.toString());

                removeFromCorrectRCDS(resourceOperation);
                resourceNotificationManager.unlock(resourceOperation.getResource());
            } else {
                if (lockCount == null) {
                    if(resourceOperation.getOperation() == Operation.READ) {
                        resourcesWeHaveLockOn_Read.put(resourceOperation.getResource(), 0);
                    } else {
                        resourcesWeHaveLockOn_Write.put(resourceOperation.getResource(), 0);
                    }
                } else {

                    stringBuilder = new StringBuilder();
                    stringBuilder
                            .append(schedulerName)
                            .append(": Transaction still requires lock. Not unlocking just yet..." );

                    handleTransactionEvent(stringBuilder.toString());

                    lockCount--;
                    if(resourceOperation.getOperation() == Operation.READ) {
                        resourcesWeHaveLockOn_Read.put(resourceOperation.getResource(), lockCount);
                    } else {
                        resourcesWeHaveLockOn_Write.put(resourceOperation.getResource(), lockCount);
                    }

                }
            }

        }

        return true;

    }

    public void handleTransactionEvent(String logString) {
        TransactionEvent transactionEvent = new TransactionEvent(logString, new Date());
        transactionEventSupplier.handleTransactionEvent(transactionEvent);
    }

    @SuppressWarnings("Duplicates")
    public boolean executeTransaction() {

        Date start = new Date();

        if (!growingPhaseSuccessful()) {
            return false;
        }

        if (!shrinkingPhaseSuccessful()) {
            return false;
        }

        Date end = new Date();
        long seconds = ChronoUnit.MILLIS.between(start.toInstant(), end.toInstant());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(schedulerName)
                .append(": TIME TILL COMPLETION: " )
                .append(seconds)
                .append(" milliseconds");
        handleTransactionEvent(stringBuilder.toString());

        return true;
    }

    public void insertIntoCorrectRCDS(ResourceOperation resourceOperation) {

        if (resourceOperation.getOperation() == Operation.READ) {
            resourceCategoryDataStructure_READ.insertResourceOperationForResource(resourceOperation.getResource(), resourceOperation);
        } else {
            resourceCategoryDataStructure_WRITE.insertResourceOperationForResource(resourceOperation.getResource(), resourceOperation);
        }

    }

    public void removeFromCorrectRCDS(ResourceOperation resourceOperation) {

        if (resourceOperation.getOperation() == Operation.READ) {
            resourceCategoryDataStructure_READ.removeResourceOperationForResouce(resourceOperation.getResource(), resourceOperation);
        } else {
            resourceCategoryDataStructure_WRITE.removeResourceOperationForResouce(resourceOperation.getResource(), resourceOperation);
        }

    }

    public void run() {

        try {
            boolean isFinished = executeTransaction();

            // Re-run logic
            if (!isFinished) {

                // Clean up before re-running
                resourceNotificationManager.deregisterHandler(this);

                for(ResourceOperation resourceOperation : transaction.getResourceOperationList()) {
                    if(resourceOperation.getOperation() == Operation.WRITE) {
                        resourceCategoryDataStructure_WRITE.removeResourceOperationForResouce(resourceOperation.getResource(), resourceOperation);
                        resourceNotificationManager.unlock(resourceOperation.getResource());
                    } else {
                        resourceCategoryDataStructure_READ.removeResourceOperationForResouce(resourceOperation.getResource(), resourceOperation);
                    }
                }

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder
                        .append(schedulerName)
                        .append(": Aborted. Doing nothing else" );

                handleTransactionEvent(stringBuilder.toString());

                //TODO: Figure out how to rerun with new Spring boot configuration

            }
        } catch (Exception ex) {}
    }

    private boolean handleAbortOperation() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(schedulerName)
                .append(": Execution aborted from within")
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
                        .append(", that we have been waiting on, has been released and unlocked ");

                    handleTransactionEvent(stringBuilder.toString());

            }
        }
    }

}
