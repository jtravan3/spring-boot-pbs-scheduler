package com.jtravan.pbs.services;

import com.jtravan.pbs.model.Action;
import com.jtravan.pbs.model.Category;
import com.jtravan.pbs.model.Operation;
import com.jtravan.pbs.model.ResourceCategoryDataStructure_READ;
import com.jtravan.pbs.model.ResourceCategoryDataStructure_WRITE;
import com.jtravan.pbs.model.ResourceOperation;
import com.techprimers.reactive.reactivemongoexample1.model.Employee;
import org.springframework.stereotype.Component;

@SuppressWarnings("Duplicates")
@Component
public class PredictionBasedSchedulerActionServiceImpl implements PredictionBasedSchedulerActionService {

    private final ResourceNotificationManager resourceNotificationManager;
    private final MetricsAggregator metricsAggregator;

    public PredictionBasedSchedulerActionServiceImpl(ResourceNotificationManager resourceNotificationManager,
                                                     MetricsAggregator metricsAggregator) {

        this.resourceNotificationManager = resourceNotificationManager;
        this.metricsAggregator = metricsAggregator;
    }

    public synchronized Action determineSchedulerAction(ResourceCategoryDataStructure_READ rcdsRead,
                                                        ResourceCategoryDataStructure_WRITE rcdsWrite,
                                                        ResourceOperation resourceOperation) {

        Employee resource = resourceOperation.getResource();

        synchronized (resource) {
            if (resourceOperation.getOperation() == Operation.WRITE) {

                if (rcdsWrite.getHighestPriorityForResource(resource) == null) {

                    if (rcdsRead.getHighestPriorityForResource(resource) == null) {

                        // Means there is currently no lock granted for the resource. Free for all...
                        long grantCount = metricsAggregator.getGrantCount();
                        metricsAggregator.setGrantCount(++grantCount);
                        return Action.GRANT;

                    } else {

                        Category requestingCategory = resourceOperation.getAssociatedTransaction().getCategory();
                        Category existingCategory = rcdsRead.getHighestPriorityForResource(resource).getAssociatedTransaction().getCategory();
                        if (Category.isCategory1HigherThanCategory2(requestingCategory, existingCategory)) {

                            // Means a write operation needs to operate on something that has a read lock of lower priority.
                            // We are going to elevate it
                            ResourceOperation ro = rcdsRead.getHighestPriorityForResource(resource);
                            resourceNotificationManager.abortTransaction(ro.getAssociatedTransaction());
                            rcdsRead.clearHeapForResource(resource);
                            rcdsWrite.insertResourceOperationForResource(resource, resourceOperation);
                            long elevateCount = metricsAggregator.getElevateCount();
                            metricsAggregator.setElevateCount(++elevateCount);
                            return Action.ELEVATE;

                        } else {

                            // Means a write operation needs to operate however, the lock that is currently granted has a
                            // higher priority and we must wait
                            long declineCount = metricsAggregator.getDeclineCount();
                            metricsAggregator.setDeclineCount(++declineCount);
                            return Action.DECLINE;

                        }
                    }


                } else { // rcdsWrite is not empty

                    Category existingWriteCategory = rcdsWrite.getHighestPriorityForResource(resource).getAssociatedTransaction().getCategory();
                    Category requestingCategory = resourceOperation.getAssociatedTransaction().getCategory();

                    if (Category.isCategory1HigherThanOrEqualCategory2(existingWriteCategory, requestingCategory)) {

                        // There is a write lock granted but the requesting lock is not high enough to elevate it. We must wait
                        long declineCount = metricsAggregator.getDeclineCount();
                        metricsAggregator.setDeclineCount(++declineCount);
                        return Action.DECLINE;

                    } else {

                        if (rcdsRead.getHighestPriorityForResource(resource) == null) {

                            // The granted write lock has a lower priority than the requesting one AND there is no
                            // read lock granted so we are good to elevate
                            ResourceOperation ro = rcdsWrite.getHighestPriorityForResource(resource);
                            resourceNotificationManager.abortTransaction(ro.getAssociatedTransaction());
                            rcdsWrite.clearHeapForResource(resource);
                            rcdsWrite.insertResourceOperationForResource(resource, resourceOperation);
                            long elevateCount = metricsAggregator.getElevateCount();
                            metricsAggregator.setElevateCount(++elevateCount);
                            return Action.ELEVATE;

                        } else {

                            Category existingReadCategory = rcdsRead.getHighestPriorityForResource(resource).getAssociatedTransaction().getCategory();
                            if (Category.isCategory1HigherThanCategory2(requestingCategory, existingReadCategory )) {

                                // The granted write lock has a lower priority than the requesting one AND the
                                // read lock granted has a lower priority so we are good to elevate over both

                                // Abort read lock
                                ResourceOperation ro = rcdsRead.getHighestPriorityForResource(resource);
                                resourceNotificationManager.abortTransaction(ro.getAssociatedTransaction());
                                rcdsRead.clearHeapForResource(resource);

                                // Abort write lock
                                ResourceOperation ro2 = rcdsWrite.getHighestPriorityForResource(resource);
                                resourceNotificationManager.abortTransaction(ro2.getAssociatedTransaction());
                                rcdsWrite.clearHeapForResource(resource);
                                rcdsWrite.insertResourceOperationForResource(resource, resourceOperation);
                                long elevateCount = metricsAggregator.getElevateCount();
                                metricsAggregator.setElevateCount(++elevateCount);
                                return Action.ELEVATE;

                            } else {

                                // The granted write lock has a lower priority than the requesting one BUT the
                                // read lock granted has a higher priority so we must wait
                                long declineCount = metricsAggregator.getDeclineCount();
                                metricsAggregator.setDeclineCount(++declineCount);
                                return Action.DECLINE;

                            }

                        }

                    }

                }

            } else { // operation is a read

                if (rcdsWrite.getHighestPriorityForResource(resource) == null) {

                    // There is no write locks granted so we are good to go. There can be shared
                    // read locks granted
                    long grantCount = metricsAggregator.getGrantCount();
                    metricsAggregator.setGrantCount(++grantCount);
                    return Action.GRANT;

                } else {

                    Category existingWriteCategory = rcdsWrite.getHighestPriorityForResource(resource).getAssociatedTransaction().getCategory();
                    Category requestingCategory = resourceOperation.getAssociatedTransaction().getCategory();
                    if (Category.isCategory1HigherThanOrEqualCategory2(existingWriteCategory, requestingCategory)) {

                        // There is a write lock granted AND it has a higher priority than the requesting lock
                        // so we must wait
                        long declineCount = metricsAggregator.getDeclineCount();
                        metricsAggregator.setDeclineCount(++declineCount);
                        return Action.DECLINE;

                    } else {

                        // There is a write lock granted BUT it has a lower priority than the requesting lock
                        // so we can elevate
                        ResourceOperation ro = rcdsWrite.getHighestPriorityForResource(resource);
                        resourceNotificationManager.abortTransaction(ro.getAssociatedTransaction());
                        rcdsWrite.clearHeapForResource(resource);
                        rcdsRead.insertResourceOperationForResource(resource, resourceOperation);
                        long elevateCount = metricsAggregator.getElevateCount();
                        metricsAggregator.setElevateCount(++elevateCount);
                        return Action.ELEVATE;

                    }

                }

            }
        }

    }

}
