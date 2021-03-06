package com.jtravan.pbs.model;

import com.techprimers.reactive.reactivemongoexample1.model.Employee;

public class ResourceOperation {

    private Operation operation;
    private Employee resource;
    private long executionTime_InMilliSeconds;
    private Transaction associatedTransaction;
    private boolean isCommitOperation;
    private boolean isAbortOperation;

    public boolean isAbortOperation() {
        return isAbortOperation;
    }

    public void setAbortOperation(boolean abortOperation) {
        isAbortOperation = abortOperation;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Employee getResource() {
        return resource;
    }

    public void setResource(Employee resource) {
        this.resource = resource;
    }

    public long getExecutionTime() {
        return executionTime_InMilliSeconds;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime_InMilliSeconds = executionTime;
    }

    public Transaction getAssociatedTransaction() {
        return associatedTransaction;
    }

    public void setAssociatedTransaction(Transaction associatedTransaction) {
        this.associatedTransaction = associatedTransaction;
    }

    public boolean isCommitOperation() {
        return isCommitOperation;
    }

    public void setIsCommitOperation(boolean commitOperation) {
        isCommitOperation = commitOperation;
    }

    @Override
    public String toString() {
        if (isCommitOperation) {
            return "COMMIT";
        }

        if(resource == null || operation == null) {
            return "";
        }

        return resource.toString() + "_" + operation.name() + " - " + executionTime_InMilliSeconds + "secs";
    }
}
