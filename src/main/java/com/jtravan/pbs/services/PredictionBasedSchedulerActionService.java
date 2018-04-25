package com.jtravan.pbs.services;

import com.jtravan.pbs.model.Action;
import com.jtravan.pbs.model.ResourceCategoryDataStructure;
import com.jtravan.pbs.model.ResourceOperation;

public interface PredictionBasedSchedulerActionService {
    Action determineSchedulerAction(ResourceCategoryDataStructure rcdsRead, ResourceCategoryDataStructure rcdsWrite, ResourceOperation resourceOperation);
}
