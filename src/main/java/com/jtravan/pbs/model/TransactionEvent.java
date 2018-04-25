package com.jtravan.pbs.model;

import java.util.Date;

public class TransactionEvent {

    private String value;
    private Date date;

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

    @Override
    public String toString() {
        return "TransactionEvent{" +
                "value='" + value + '\'' +
                ", date=" + date +
                '}';
    }
}
