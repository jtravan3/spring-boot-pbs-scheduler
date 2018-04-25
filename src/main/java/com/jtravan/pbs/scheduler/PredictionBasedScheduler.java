package com.jtravan.pbs.scheduler;

import com.jtravan.pbs.model.Action;
import com.jtravan.pbs.model.Operation;
import com.jtravan.pbs.model.Resource;
import com.jtravan.pbs.model.ResourceCategoryDataStructure;
import com.jtravan.pbs.model.ResourceNotification;
import com.jtravan.pbs.model.ResourceOperation;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.model.TransactionNotification;
import com.jtravan.pbs.model.TransactionNotificationType;
import com.jtravan.pbs.services.PredictionBasedSchedulerActionService;
import com.jtravan.pbs.services.ResourceNotificationHandler;
import com.jtravan.pbs.services.ResourceNotificationManager;
import com.jtravan.pbs.services.TransactionNotificationManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PredictionBasedScheduler implements TransactionExecutor,
        ResourceNotificationHandler, Runnable {

    private final PredictionBasedSchedulerActionService predictionBasedSchedulerActionService;
    private final ResourceCategoryDataStructure resourceCategoryDataStructure_READ;
    private final ResourceCategoryDataStructure resourceCategoryDataStructure_WRITE;
    private final ResourceNotificationManager resourceNotificationManager;
    private final TransactionNotificationManager transactionNotificationManager;
    private final Map<Resource, Integer> resourcesWeHaveLockOn_Read;
    private final Map<Resource, Integer> resourcesWeHaveLockOn_Write;
    private Resource resourceWaitingOn;
    private Transaction transaction;
    private String schedulerName;
    private Thread thread;

    private long startTime;
    private long endTime;

    public PredictionBasedScheduler(ResourceNotificationManager resourceNotificationManager,
                                    PredictionBasedSchedulerActionService predictionBasedSchedulerActionService,
                                    ResourceCategoryDataStructure resourceCategoryDataStructure_READ,
                                    ResourceCategoryDataStructure resourceCategoryDataStructure_WRITE,
                                    TransactionNotificationManager transactionNotificationManager) {

        resourcesWeHaveLockOn_Read = new HashMap<>();
        resourcesWeHaveLockOn_Write = new HashMap<>();

        this.resourceNotificationManager = resourceNotificationManager;
        this.transactionNotificationManager = transactionNotificationManager;
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

    private boolean growingPhaseSuccessful() {

        if (transaction == null) {
            return false;
        }

        startTime = System.currentTimeMillis();

        // two phase locking - growing phase
        System.out.println("=========================================================");
        System.out.println(schedulerName + ": Two-phase locking growing phase initiated.");
        System.out.println("=========================================================");
        for (ResourceOperation resourceOperation : transaction.getResourceOperationList()) {

            if(resourceOperation.isAbortOperation()) {
                handleAbortOperation();
                return false;
            }

            Action action = predictionBasedSchedulerActionService
                    .determineSchedulerAction(resourceCategoryDataStructure_READ,
                            resourceCategoryDataStructure_WRITE, resourceOperation);

            System.out.println(schedulerName + ": Action for resource " + resourceOperation.getResource() + ": " + action.name());

            switch (action) {
                case DECLINE:

                    if (resourceOperation.getOperation() == Operation.WRITE &&
                            resourcesWeHaveLockOn_Write.containsKey(resourceOperation.getResource())) {
                        System.out.println(schedulerName + ": Already have lock for Resource "
                                + resourceOperation.getResource() + ". Continuing execution");

                        Integer lockCount = resourcesWeHaveLockOn_Write.get(resourceOperation.getResource());
                        lockCount++;
                        insertIntoCorrectRCDS(resourceOperation);
                        resourcesWeHaveLockOn_Write.put(resourceOperation.getResource(), lockCount);
                        continue;
                    } else if(resourceOperation.getOperation() == Operation.READ &&
                            (resourcesWeHaveLockOn_Read.containsKey(resourceOperation.getResource()) ||
                                    resourcesWeHaveLockOn_Write.containsKey(resourceOperation.getResource() ))) {

                        System.out.println(schedulerName + ": Already have lock for Resource "
                                + resourceOperation.getResource() + ". Continuing execution");

                        Integer lockCount = resourcesWeHaveLockOn_Read.get(resourceOperation.getResource());
                        lockCount++;
                        insertIntoCorrectRCDS(resourceOperation);
                        resourcesWeHaveLockOn_Read.put(resourceOperation.getResource(), lockCount);
                        continue;
                    } else {
                        resourceWaitingOn = resourceOperation.getResource();
                        System.out.println(schedulerName + ": Waiting for lock on Resource "
                                + resourceOperation.getResource() + " to be released...");
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        System.out.println(schedulerName + ": Lock for Resource " + resourceOperation.getResource()
                                + " released and obtained");

                        insertIntoCorrectRCDS(resourceOperation);
                        resourcesWeHaveLockOn_Read.put(resourceOperation.getResource(), 1);
                        resourceNotificationManager.lock(resourceOperation.getResource(), resourceOperation.getOperation());
                    }

                    break;
                case ELEVATE:

                    System.out.println(schedulerName + ": Other transaction abort initiated. Now locking resource...");
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
                        System.out.println(schedulerName + ": Already have lock for Resource "
                                + resourceOperation.getResource() + ". Continuing execution");

                        Integer lockCount = resourcesWeHaveLockOn_Write.get(resourceOperation.getResource());
                        lockCount++;
                        resourcesWeHaveLockOn_Write.put(resourceOperation.getResource(), lockCount);
                        continue;
                    } else if(resourceOperation.getOperation() == Operation.READ &&
                            resourcesWeHaveLockOn_Read.containsKey(resourceOperation.getResource())) {
                        System.out.println(schedulerName + ": Already have lock for Resource "
                                + resourceOperation.getResource() + ". Continuing execution");

                        Integer lockCount = resourcesWeHaveLockOn_Read.get(resourceOperation.getResource());
                        lockCount++;
                        resourcesWeHaveLockOn_Read.put(resourceOperation.getResource(), lockCount);
                        continue;
                    } else {
                        System.out.println(schedulerName + ": No lock obtained for Resource " + resourceOperation.getResource() + ". Locking now...");

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
        System.out.println("==========================================================");
        System.out.println(schedulerName + ": Two-phase locking shrinking phase initiated");
        System.out.println("==========================================================");
        for (ResourceOperation resourceOperation : transaction.getResourceOperationList()) {

            if(resourceOperation.isAbortOperation()) {
                handleAbortOperation();
                return false;
            }

            try {
                System.out.println(schedulerName + ": Executing operation on Resource " + resourceOperation.getResource());
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

                System.out.println(schedulerName + ": No longer needing the lock. Releasing lock...");
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
                    System.out.println(schedulerName + ": Transaction still requires lock. Not unlocking just yet...");
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

    public void taskStart() {
        this.thread = new Thread(this);
        this.thread.start();
    }

    public void taskStop() {
        this.thread.interrupt();

        try {
            this.thread.join();
        } catch (InterruptedException ex) {
            System.out.println(schedulerName + ": Failed on thread.join(). Don't exactly know what that means");
        }
    }

    @SuppressWarnings("Duplicates")
    public boolean executeTransaction() {

        if (!growingPhaseSuccessful()) {
            return false;
        }

        if (!shrinkingPhaseSuccessful()) {
            return false;
        }

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

                System.out.println(getSchedulerName() + ": Aborted. Waiting for other transaction to finish before retrying execution");
                try {
                    wait();
                } catch (Exception e) {
                    //TODO: Figure out how to do this with new Spring Boot configuration
//                    System.out.println(getSchedulerName() + ": Other transaction finished. Now retrying...");
//
//                    resetScheduler();
//                    setSchedulerName("Sandbox scheduler for " + getSchedulerName());
//                    boolean isSandboxExecutionSuccess = executeTransaction();
//
//                    if(isSandboxExecutionSuccess) {
//                        System.out.println(getSchedulerName() + ": Sandbox Execution Succeeded, Re-Run with Main System");
//
//                        executeTransaction();
//                    } else {
//                        System.out.println(getSchedulerName() + ": Sandbox Execution Failed. Execution Complete");
//                    }
                }
            } else {
                System.out.println(schedulerName + ": has successfully completed execution!");
                endTime = System.currentTimeMillis();

                TransactionNotification transactionNotification = new TransactionNotification();
                transactionNotification.setTransaction(transaction);
                transactionNotification.setTransactionNotificationType(TransactionNotificationType.TRANSACTION_COMPLETE);
                transactionNotificationManager.handleTransactionNotification(transactionNotification);

                resourceNotificationManager.deregisterHandler(this);

                taskStop();
            }
        } catch (Exception ex) {}
    }

    private boolean handleAbortOperation() {
        System.out.println(schedulerName + ": Execution aborted from within");
        System.out.println(schedulerName + ": Waiting and trying execution again");
        return false;
    }

    @SuppressWarnings("Duplicates")
    public void handleResourceNotification(ResourceNotification resourceNotification) {

        if (resourceNotification == null) {
            return;
        }

        if (!resourceNotification.isLocked()) {
            if (resourceNotification.getResource() == resourceWaitingOn) {
                System.out.println(schedulerName + ": Resource, " + resourceNotification.getResource()
                        + ", that we have been waiting on, has been released and unlocked ");

                    notifyAll();

            }
        }
    }

}
