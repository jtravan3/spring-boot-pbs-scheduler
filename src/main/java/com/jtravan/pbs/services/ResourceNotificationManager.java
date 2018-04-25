package com.jtravan.pbs.services;

import com.jtravan.pbs.model.Operation;
import com.jtravan.pbs.model.Resource;
import com.jtravan.pbs.model.ResourceNotification;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by johnravan on 11/17/16.
 */
public class ResourceNotificationManager implements ResourceNotificationHandler{

    private static ResourceNotificationManager theInstance;
    List<ResourceNotificationHandler> handlers;
    Map<Integer, Resource> resourceIntegerMap;


    private ResourceNotificationManager() {
        resourceIntegerMap = new HashMap<Integer, Resource>();
        handlers = new LinkedList<ResourceNotificationHandler>();

        for (Resource resource : Resource.values()) {
            resourceIntegerMap.put(resource.getResourceNum(), resource);
        }
    }

    public static final ResourceNotificationManager getInstance(boolean createOneTimeInstance) {

        if(createOneTimeInstance) {
            return new ResourceNotificationManager();
        } else {
            if(theInstance == null) {
                theInstance = new ResourceNotificationManager();
            }
            return theInstance;
        }

    }

    @SuppressWarnings("Duplicates")
    public synchronized void lock(Resource resource, Operation operation) {

        if (operation == Operation.READ) {
            resource.lock();

            ResourceNotification resourceNotification = new ResourceNotification();
            resourceNotification.setResource(resource);
            resourceNotification.setLocked(true);
            System.out.println("Locking Resource " + resource);
            handleResourceNotification(resourceNotification);
        } else {
            if (!resource.isLocked()) {
                resource.lock();

                ResourceNotification resourceNotification = new ResourceNotification();
                resourceNotification.setResource(resource);
                resourceNotification.setLocked(true);
                System.out.println("Locking Resource " + resource);
                handleResourceNotification(resourceNotification);
            } else {
                throw new IllegalStateException("Cannot lock already locked resource that has a Write lock. Resource " + resource.name());
            }
        }
    }

    public void unlock(Resource resource) {
        if (resource.isLocked()) {
            resource.unlock();

            ResourceNotification resourceNotification = new ResourceNotification();
            resourceNotification.setResource(resource);
            resourceNotification.setLocked(false);
            System.out.println("Unlocking Resource " + resource);
            handleResourceNotification(resourceNotification);
        }
    }

    public Resource getResourceByResourceNum(int resourceNum) {
        return resourceIntegerMap.get(resourceNum);
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
