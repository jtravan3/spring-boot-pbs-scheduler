package com.jtravan.pbs.services;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
@Data
public class MetricsAggregator {

    private Date startTime;
    private Date endTime;
    private long scheduleCount;
    private long operationCount;
    private long elevateCount;
    private long declineCount;
    private long grantCount;
    private long abortCount;
    private long totalTimeWithoutExecution;

    public long getExecutionTime() {
        long milliSeconds = ChronoUnit.MILLIS.between(startTime.toInstant(), endTime.toInstant());
        return milliSeconds - 10000; // offset 10 second wait
    }

    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder();
        toString.append("Execution Metrics ---> Number of Schedules: ");
        toString.append(scheduleCount);
        toString.append(", Number of Operations: ");
        toString.append(operationCount);
        toString.append(", Number of ELEVATE actions: ");
        toString.append(elevateCount);
        toString.append(", Number of DECLINE actions: ");
        toString.append(declineCount);
        toString.append(", Number of GRANT actions: ");
        toString.append(grantCount);
        toString.append(", Number of abort actions: ");
        toString.append(abortCount);
        toString.append(", Total time if executed serial: ");
        toString.append(totalTimeWithoutExecution);
        toString.append(", Actual execution time: ");
        toString.append(getExecutionTime());
        return toString.toString();
    }

    public void clear() {
        this.startTime = null;
        this.endTime = null;
        this.scheduleCount = 0;
        this.operationCount = 0;
        this.elevateCount = 0;
        this.declineCount = 0;
        this.grantCount = 0;
        this.abortCount = 0;
        this.totalTimeWithoutExecution = 0;
    }
}
