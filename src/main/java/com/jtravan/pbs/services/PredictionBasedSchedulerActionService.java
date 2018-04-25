package com.jtravan.pbs.services;

import com.jtravan.pbs.model.Action;
import com.jtravan.pbs.model.ResourceCategoryDataStructure;
import com.jtravan.pbs.model.ResourceOperation;

/**
 * Created by johnravan on 11/9/16.
 */
public interface PredictionBasedSchedulerActionService {
    Action determineSchedulerAction(ResourceCategoryDataStructure rcdsRead, ResourceCategoryDataStructure rcdsWrite, ResourceOperation resourceOperation);
}
