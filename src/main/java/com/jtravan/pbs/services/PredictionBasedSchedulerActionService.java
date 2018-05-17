package com.jtravan.pbs.services;

import com.jtravan.pbs.model.Action;
import com.jtravan.pbs.model.ResourceCategoryDataStructure_READ;
import com.jtravan.pbs.model.ResourceCategoryDataStructure_WRITE;
import com.jtravan.pbs.model.ResourceOperation;

public interface PredictionBasedSchedulerActionService {
    Action determineSchedulerAction(ResourceCategoryDataStructure_READ rcdsRead, ResourceCategoryDataStructure_WRITE rcdsWrite, ResourceOperation resourceOperation);
}
