package com.jtravan.pbs.services;

import com.jtravan.pbs.model.Operation;
import com.jtravan.pbs.model.ResourceNotification;
import com.techprimers.reactive.reactivemongoexample1.model.Employee;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class ResourceNotificationManager implements ResourceNotificationHandler{

    private final List<ResourceNotificationHandler> handlers;


    private ResourceNotificationManager() {
        handlers = new LinkedList<>();
    }

    @SuppressWarnings("Duplicates")
    public synchronized void lock(Employee resource, Operation operation) {

        if (operation == Operation.READ) {
            resource.setLocked(true);

            ResourceNotification resourceNotification = new ResourceNotification();
            resourceNotification.setResource(resource);
            resourceNotification.setLocked(true);
            System.out.println("Locking Resource " + resource);
            handleResourceNotification(resourceNotification);
        } else {
            if (!resource.isLocked()) {
                resource.setLocked(true);

                ResourceNotification resourceNotification = new ResourceNotification();
                resourceNotification.setResource(resource);
                resourceNotification.setLocked(true);
                System.out.println("Locking Resource " + resource);
                handleResourceNotification(resourceNotification);
            } else {
                throw new IllegalStateException("Cannot lock already locked resource that has a Write lock. Resource " + resource.toString());
            }
        }
    }

    public void unlock(Employee resource) {
        if (resource.isLocked()) {
            resource.setLocked(false);

            ResourceNotification resourceNotification = new ResourceNotification();
            resourceNotification.setResource(resource);
            resourceNotification.setLocked(false);
            System.out.println("Unlocking Resource " + resource);
            handleResourceNotification(resourceNotification);
        }
    }

    public synchronized void registerHandler (ResourceNotificationHandler handler) {

        if (handler == null) {
            return;
        }

        System.out.println("Resource Notification Handler registered for notifications");
        handlers.add(handler);

    }

    public synchronized void deregisterHandler (ResourceNotificationHandler handler) {

        if (handler == null) {
            return;
        }

        System.out.println("Resource Notification Handler deregistered for notifications");
        handlers.remove(handler);

    }

    public synchronized void handleResourceNotification(ResourceNotification resourceNotification) {

        if (resourceNotification == null) {
            return;
        }

        for (ResourceNotificationHandler handler : handlers) {
            if (handler != null) {
                handler.handleResourceNotification(resourceNotification);
            }
        }

    }
}
