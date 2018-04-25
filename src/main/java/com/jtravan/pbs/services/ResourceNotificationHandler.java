package com.jtravan.pbs.services;

import com.jtravan.pbs.model.ResourceNotification;

/**
 * Created by johnravan on 11/17/16.
 */
public interface ResourceNotificationHandler {
    void handleResourceNotification(ResourceNotification resourceNotification);
}
