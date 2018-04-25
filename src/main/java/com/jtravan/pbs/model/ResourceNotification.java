package com.jtravan.pbs.model;

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
