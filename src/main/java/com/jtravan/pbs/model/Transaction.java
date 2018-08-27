package com.jtravan.pbs.model;

import com.techprimers.reactive.reactivemongoexample1.model.Employee;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Transaction {

    private List<ResourceOperation> resourceOperationList;
    private Category category;

    public Transaction() {
        resourceOperationList = new LinkedList<>();
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

    public void setResourceOperationList(List<ResourceOperation> resourceOperationList) {
        this.resourceOperationList = resourceOperationList;
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

    public List<ResourceOperation> getAndRemoveOperationsByResource(Employee resource) {

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

    public Transaction createCopy() {
        Transaction transaction = new Transaction();
        transaction.setCategory(this.category);

        List<ResourceOperation> tempList = new LinkedList<>();
        for(ResourceOperation resourceOperation : this.resourceOperationList) {
            ResourceOperation newRO = new ResourceOperation();
            newRO.setAssociatedTransaction(transaction);
            newRO.setExecutionTime(resourceOperation.getExecutionTime());
            newRO.setIsCommitOperation(resourceOperation.isCommitOperation());
            newRO.setOperation(resourceOperation.getOperation());
            newRO.setResource(resourceOperation.getResource());
            newRO.setAbortOperation(resourceOperation.isAbortOperation());
            tempList.add(newRO);
        }

        transaction.setResourceOperationList(tempList);
        return transaction;
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
