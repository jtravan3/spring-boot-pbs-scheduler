package com.jtravan.pbs.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by johnravan on 3/30/16.
 */
public class Transaction {

    private List<ResourceOperation> resourceOperationList;
    private Category category;

    public Transaction() {
        resourceOperationList = new LinkedList<ResourceOperation>();
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void addResourceOperation(ResourceOperation resourceOperation) {
        if(resourceOperation == null) {
            return;
        }
        resourceOperationList.add(resourceOperation);
    }

    public ResourceOperation getNextResourceOperation() {
        return resourceOperationList.remove(0);
    }

    public boolean hasMoreResourceOperations() {
        return !resourceOperationList.isEmpty();
    }

    public List<ResourceOperation> getResourceOperationList() {
        return resourceOperationList;
    }

    public List<ResourceOperation> getAndRemoveOperationsByResource(Resource resource) {

        if(resource == null || resourceOperationList.isEmpty()) {
            return Collections.emptyList();
        }

        List rtnList = new LinkedList();

        for(ResourceOperation resourceOperation : resourceOperationList) {
            if(resource == resourceOperation.getResource()) {
                rtnList.add(resourceOperation);
            }
        }

        resourceOperationList.removeAll(rtnList);

        return rtnList;

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for(ResourceOperation resourceOperation: resourceOperationList) {
            if(resourceOperation.isCommitOperation()) {
                builder.append(resourceOperation.toString());
                builder.append(" - ");
                builder.append(category.name());
                break;
            }

            builder.append(resourceOperation.toString());
            builder.append(", ");
        }

        return builder.toString();
    }

}
