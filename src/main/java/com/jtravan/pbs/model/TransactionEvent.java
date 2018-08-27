package com.jtravan.pbs.model;

import java.util.Date;

public class TransactionEvent {

    private boolean isComplete = false;
    private String value;
    private Date date;
    private Throwable exception;

    public TransactionEvent(String value, Date date) {
        this.value = value;
        this.date = date;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "TransactionEvent{" +
                "isComplete=" + isComplete +
                ", value='" + value + '\'' +
                ", date=" + date +
                ", exception=" + exception +
                '}';
    }
}
