package com.jtravan.pbs.model;

import com.techprimers.reactive.reactivemongoexample1.model.Employee;

public class ResourceNotification {

    private Employee resource;
    private boolean isLocked;

    public Employee getResource() {
        return resource;
    }

    public void setResource(Employee resource) {
        this.resource = resource;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}
