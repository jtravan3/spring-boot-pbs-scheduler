package com.jtravan.pbs.scheduler;

import com.jtravan.pbs.model.ResourceNotification;
import com.jtravan.pbs.model.ResourceOperation;
import com.jtravan.pbs.model.Transaction;
import com.jtravan.pbs.services.ResourceNotificationHandler;
import com.jtravan.pbs.services.ResourceNotificationManager;
import com.techprimers.reactive.reactivemongoexample1.model.Employee;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TraditionalScheduler implements TransactionExecutor, ResourceNotificationHandler, Runnable {

    private final Map<Employee, Integer> resourcesWeHaveLockOn;
    private Employee resourceWaitingOn;
    private Transaction transaction;
    private String schedulerName;
    private final ResourceNotificationManager resourceNotificationManager;

    public TraditionalScheduler(ResourceNotificationManager resourceNotificationManager) {
        this.resourcesWeHaveLockOn = new HashMap<>();
        this.resourceNotificationManager = resourceNotificationManager;
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

    @SuppressWarnings("Duplicates")
    public synchronized boolean executeTransaction() {

        if (transaction == null) {
            return false;
        }

        // two phase locking - growing phase
        System.out.println("=========================================================");
        System.out.println(schedulerName + ": Two-phase locking growing phase initiated.");
        System.out.println("=========================================================");
        for (ResourceOperation resourceOperation : transaction.getResourceOperationList()) {

            if (resourceOperation.getResource().isLocked()) {

                System.out.println(schedulerName + ": Obtaining lock for Resource " + resourceOperation.getResource());
                if(resourcesWeHaveLockOn.containsKey(resourceOperation.getResource())) {
                    System.out.println(schedulerName + ": Already have lock for Resource "
                            + resourceOperation.getResource() + ". Continuing execution");

                    Integer lockCount = resourcesWeHaveLockOn.get(resourceOperation.getResource());
                    resourcesWeHaveLockOn.put(resourceOperation.getResource(), ++lockCount);
                    continue;
                } else {
                    resourceWaitingOn = resourceOperation.getResource();
                    System.out.println(schedulerName + ": Waiting for lock on Resource "
                            + resourceOperation.getResource() + " to be released...");
                    try {
                        synchronized (this) {
                            wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println(schedulerName + ": Lock for Resource " + resourceOperation.getResource()
                            + " released and obtained");
                    resourcesWeHaveLockOn.put(resourceOperation.getResource(), 1);
                    resourceNotificationManager.lock(resourceOperation.getResource(), resourceOperation.getOperation());

                }

            } else {

                System.out.println(schedulerName + ": No lock obtained for Resource " + resourceOperation.getResource());
                resourcesWeHaveLockOn.put(resourceOperation.getResource(), 1);
                resourceNotificationManager.lock(resourceOperation.getResource(), resourceOperation.getOperation());

            }

        }

        // two phase locking - shrinking phase
        System.out.println("==========================================================");
        System.out.println(schedulerName + ": Two-phase locking shrinking phase initiated");
        System.out.println("==========================================================");
        for (ResourceOperation resourceOperation : transaction.getResourceOperationList()) {

            try {
                System.out.println(schedulerName + ": Executing operation on Resource " + resourceOperation.getResource());
                Thread.sleep(resourceOperation.getExecutionTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Integer lockCount = resourcesWeHaveLockOn.get(resourceOperation.getResource());
            if (lockCount == 1) {
                resourcesWeHaveLockOn.remove(resourceOperation.getResource());
                resourceNotificationManager.unlock(resourceOperation.getResource());
            } else {
                resourcesWeHaveLockOn.put(resourceOperation.getResource(), --lockCount);
            }

        }

        System.out.println(schedulerName + ": has successfully completed execution!");

        return true;
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
                synchronized (this) {
                    notifyAll();
                }
            }
        }

    }
}
