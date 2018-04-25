package com.jtravan.pbs.model;

/**
 * Created by johnravan on 11/17/16.
 */
public class ResourceNotification {

    private Resource resource;
    private boolean isLocked;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}
