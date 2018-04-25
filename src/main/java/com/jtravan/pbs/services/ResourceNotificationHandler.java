package com.jtravan.pbs.services;

import com.jtravan.pbs.model.ResourceNotification;

public interface ResourceNotificationHandler {
    void handleResourceNotification(ResourceNotification resourceNotification);
}
