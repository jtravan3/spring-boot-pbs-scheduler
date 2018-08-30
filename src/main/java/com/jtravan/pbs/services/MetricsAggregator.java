package com.jtravan.pbs.services;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
@Data
public class MetricsAggregator {

    // General values
    private long scheduleCount;
    private long operationCount;
    private long totalTimeWithoutExecution;

    // PBS Values
    private Date pbsStartTime;
    private Date pbsEndTime;
    private long elevateCount;
    private long declineCount;
    private long grantCount;
    private long pbsAbortCount;
    private long pbsExecutionTime;

    // Traditional Values
    private Date tsStartTime;
    private Date tsEndTime;
    private long tsAbortCount;
    private boolean isTsDeadLocked;
    private long tsExecutionTime;

    // No-Locking Values
    private Date nlStartTime;
    private Date nlEndTime;
    private long nlAbortCount;
    private boolean isNlConsistencyLost;
    private long nlExecutionTime;


    private long getPbsExecutionTime() {
        long milliSeconds = ChronoUnit.MILLIS.between(pbsStartTime.toInstant(), pbsEndTime.toInstant());
        pbsExecutionTime = milliSeconds - 10000; // offset 10 second wait
        return pbsExecutionTime;
    }

    private long getTsExecutionTime() {
        long milliSeconds = ChronoUnit.MILLIS.between(tsStartTime.toInstant(), tsEndTime.toInstant());
        tsExecutionTime = milliSeconds - 10000; // offset 10 second wait
        return tsExecutionTime;
    }

    private long getNlExecutionTime() {
        long milliSeconds = ChronoUnit.MILLIS.between(nlStartTime.toInstant(), nlEndTime.toInstant());
        nlExecutionTime = milliSeconds - 10000; // offset 10 second wait
        return nlExecutionTime;
    }

    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder();
        toString.append("=============================================");
        toString.append(System.lineSeparator());
        toString.append("Execution Metrics --->");
        toString.append(System.lineSeparator());
        toString.append("Number of Schedules: ");
        toString.append(scheduleCount);
        toString.append(System.lineSeparator());
        toString.append("Number of Operations: ");
        toString.append(operationCount);
        toString.append(System.lineSeparator());
        toString.append("Total time w/o execution: ");
        toString.append(totalTimeWithoutExecution);
        toString.append(System.lineSeparator());
        toString.append("=============================================");
        toString.append(System.lineSeparator());
        toString.append("*******************");
        toString.append(System.lineSeparator());
        toString.append("PBS Metrics --->");
        toString.append(System.lineSeparator());
        toString.append("Number of ELEVATE actions: ");
        toString.append(elevateCount);
        toString.append(System.lineSeparator());
        toString.append("Number of DECLINE actions: ");
        toString.append(declineCount);
        toString.append(System.lineSeparator());
        toString.append("Number of GRANT actions: ");
        toString.append(grantCount);
        toString.append(System.lineSeparator());
        toString.append("Number of abort actions: ");
        toString.append(pbsAbortCount);
        toString.append(System.lineSeparator());
        toString.append("Execution time: ");
        toString.append(getPbsExecutionTime());
        toString.append(System.lineSeparator());
        toString.append("*******************");
        toString.append(System.lineSeparator());
        toString.append("-------------------------");
        toString.append(System.lineSeparator());
        toString.append("Traditional Scheduler Metrics --->");
        toString.append(System.lineSeparator());
        toString.append("Number of abort actions: ");
        toString.append(tsAbortCount);
        toString.append(System.lineSeparator());
        toString.append("Is it deadlocked?: ");
        toString.append(isTsDeadLocked);
        toString.append(System.lineSeparator());
        toString.append("Execution time: ");
        toString.append(getTsExecutionTime());
        toString.append(System.lineSeparator());
        toString.append("-------------------------");
        toString.append(System.lineSeparator());
        toString.append("%%%%%%%%%%%%%%%%%%%%%%%%%%");
        toString.append(System.lineSeparator());
        toString.append("No-Locking Scheduler Metrics --->");
        toString.append(System.lineSeparator());
        toString.append("Number of abort actions: ");
        toString.append(nlAbortCount);
        toString.append(System.lineSeparator());
        toString.append("Is consistency lost?: ");
        toString.append(isNlConsistencyLost);
        toString.append(System.lineSeparator());
        toString.append("Execution time: ");
        toString.append(getNlExecutionTime());
        toString.append(System.lineSeparator());
        toString.append("%%%%%%%%%%%%%%%%%%%%%%%%%%");
        return toString.toString();
    }

    public void clear() {
        this.pbsStartTime = null;
        this.pbsEndTime = null;
        this.scheduleCount = 0;
        this.operationCount = 0;
        this.elevateCount = 0;
        this.declineCount = 0;
        this.grantCount = 0;
        this.pbsAbortCount = 0;
        this.totalTimeWithoutExecution = 0;

        this.tsStartTime = null;
        this.tsEndTime = null;
        this.tsAbortCount = 0;
        this.isTsDeadLocked = false;
        this.tsExecutionTime = 0;

        this.nlStartTime = null;
        this.nlEndTime = null;
        this.nlAbortCount = 0;
        this.isNlConsistencyLost = false;
        this.nlExecutionTime = 0;
    }
}
