package com.jtravan.pbs.model;

/**
 * Created by johnravan on 3/30/16.
 */
public enum Operation {
    READ(0),
    WRITE(1);

    private final int operationNum;

    Operation(int operationNum) {
        this.operationNum = operationNum;
    }

    public int getOperationNum() {
        return this.operationNum;
    }

    public static final Operation getOperationByOperationNum(int operationNum) {

        if(operationNum == 0) {
            return READ;
        } else if(operationNum == 1) {
            return WRITE;
        } else {
            return null;
        }

    }
}
