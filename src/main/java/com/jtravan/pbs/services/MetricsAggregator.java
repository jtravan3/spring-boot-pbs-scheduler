package com.jtravan.pbs.services;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLOutput;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class MetricsAggregator {

    @Value("${s3.enabled}")
    private Boolean useAmazonS3;

    private static File output1;
    private static File output2;
    private static File output3;
    private static File output4;
    private static File output5;
    private static File output6;
    private static File output7;

    private final AmazonFileUploader amazonFileUploader;

    @Autowired
    public MetricsAggregator(AmazonFileUploader amazonFileUploader) throws IOException {
        this.amazonFileUploader = amazonFileUploader;
        Runtime.getRuntime().addShutdownHook(new FileUploadShutdownHook());
        initializeFiles();
    }

    private void initializeFiles() throws IOException {
        Instant time = Instant.now();
        output1  = File.createTempFile("output1-" + time.toString(), ".csv");
        output2  = File.createTempFile("output2-" + time.toString(), ".csv");
        output3  = File.createTempFile("output3-" + time.toString(), ".csv");
        output4  = File.createTempFile("output4-" + time.toString(), ".csv");
        output5  = File.createTempFile("output5-" + time.toString(), ".csv");
        output6  = File.createTempFile("output6-" + time.toString(), ".csv");
        output7  = File.createTempFile("output7-" + time.toString(), ".csv");

        output1.deleteOnExit();
        output2.deleteOnExit();
        output3.deleteOnExit();
        output4.deleteOnExit();
        output5.deleteOnExit();
        output6.deleteOnExit();
        output7.deleteOnExit();
    }

    private File getFileByTestCaseNumber(Long testCaseNumber) {
        if (testCaseNumber == 1) {
            return output1;
        } else if (testCaseNumber == 2) {
            return output2;
        } else if (testCaseNumber == 3) {
            return output3;
        } else if (testCaseNumber == 4) {
            return output4;
        } else if (testCaseNumber == 5) {
            return output5;
        } else if (testCaseNumber == 6) {
            return output6;
        } else if (testCaseNumber == 7) {
            return output7;
        } else {
            return null;
        }
    }

    private static final String RESEARCH_OUTPUT_FILE_NAME = "/Users/jravan/Desktop/research-output";

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
    private long pbsRerunCount;

    // Traditional Values
    private Date tsStartTime;
    private Date tsEndTime;
    private long tsAbortCount;
    private long tsDeadLockCount;
    private long tsExecutionTime;

    // No-Locking Values
    private Date nlStartTime;
    private Date nlEndTime;
    private long nlAbortCount;
    private boolean isNlConsistencyLost;
    private long nlExecutionTime;

    public synchronized long getScheduleCount() {
        return scheduleCount;
    }

    public synchronized void setScheduleCount(long scheduleCount) {
        this.scheduleCount = scheduleCount;
    }

    public synchronized long getOperationCount() {
        return operationCount;
    }

    public synchronized void setOperationCount(long operationCount) {
        this.operationCount = operationCount;
    }

    public synchronized long getTotalTimeWithoutExecution() {
        return totalTimeWithoutExecution;
    }

    public synchronized void setTotalTimeWithoutExecution(long totalTimeWithoutExecution) {
        this.totalTimeWithoutExecution = totalTimeWithoutExecution;
    }

    public synchronized Date getPbsStartTime() {
        return pbsStartTime;
    }

    public synchronized void setPbsStartTime(Date pbsStartTime) {
        this.pbsStartTime = pbsStartTime;
    }

    public synchronized Date getPbsEndTime() {
        return pbsEndTime;
    }

    public synchronized void setPbsEndTime(Date pbsEndTime) {
        this.pbsEndTime = pbsEndTime;
    }

    public synchronized long getElevateCount() {
        return elevateCount;
    }

    public synchronized void setElevateCount(long elevateCount) {
        this.elevateCount = elevateCount;
    }

    public synchronized long getDeclineCount() {
        return declineCount;
    }

    public synchronized void setDeclineCount(long declineCount) {
        this.declineCount = declineCount;
    }

    public synchronized long getGrantCount() {
        return grantCount;
    }

    public synchronized void setGrantCount(long grantCount) {
        this.grantCount = grantCount;
    }

    public synchronized long getPbsAbortCount() {
        return pbsAbortCount;
    }

    public synchronized void setPbsAbortCount(long pbsAbortCount) {
        this.pbsAbortCount = pbsAbortCount;
    }

    public synchronized void setPbsExecutionTime(long pbsExecutionTime) {
        this.pbsExecutionTime = pbsExecutionTime;
    }

    public synchronized long getPbsRerunCount() {
        return pbsRerunCount;
    }

    public synchronized void setPbsRerunCount(long pbsRerunCount) {
        this.pbsRerunCount = pbsRerunCount;
    }

    public synchronized Date getTsStartTime() {
        return tsStartTime;
    }

    public synchronized void setTsStartTime(Date tsStartTime) {
        this.tsStartTime = tsStartTime;
    }

    public synchronized Date getTsEndTime() {
        return tsEndTime;
    }

    public synchronized void setTsEndTime(Date tsEndTime) {
        this.tsEndTime = tsEndTime;
    }

    public synchronized long getTsAbortCount() {
        return tsAbortCount;
    }

    public synchronized void setTsAbortCount(long tsAbortCount) {
        this.tsAbortCount = tsAbortCount;
    }

    public synchronized long getTsDeadLockCount() {
        return tsDeadLockCount;
    }

    public synchronized void setTsDeadLockCount(long tsDeadLockCount) {
        this.tsDeadLockCount = tsDeadLockCount;
    }

    public synchronized void setTsExecutionTime(long tsExecutionTime) {
        this.tsExecutionTime = tsExecutionTime;
    }

    public synchronized Date getNlStartTime() {
        return nlStartTime;
    }

    public synchronized void setNlStartTime(Date nlStartTime) {
        this.nlStartTime = nlStartTime;
    }

    public synchronized Date getNlEndTime() {
        return nlEndTime;
    }

    public synchronized void setNlEndTime(Date nlEndTime) {
        this.nlEndTime = nlEndTime;
    }

    public synchronized long getNlAbortCount() {
        return nlAbortCount;
    }

    public synchronized void setNlAbortCount(long nlAbortCount) {
        this.nlAbortCount = nlAbortCount;
    }

    public synchronized boolean isNlConsistencyLost() {
        return isNlConsistencyLost;
    }

    public synchronized void setNlConsistencyLost(boolean nlConsistencyLost) {
        isNlConsistencyLost = nlConsistencyLost;
    }

    public synchronized void setNlExecutionTime(long nlExecutionTime) {
        this.nlExecutionTime = nlExecutionTime;
    }

    private long getPbsExecutionTime() {
        long milliSeconds = ChronoUnit.MILLIS.between(pbsStartTime.toInstant(), pbsEndTime.toInstant());
        pbsExecutionTime = milliSeconds - 10000; // offset 10 second wait
        //pbsExecutionTime = pbsExecutionTime - (pbsRerunCount * 2000); // remove time waiting to execute again
        return pbsExecutionTime;
    }

    private long getTsExecutionTime() {
        long milliSeconds = ChronoUnit.MILLIS.between(tsStartTime.toInstant(), tsEndTime.toInstant());
        tsExecutionTime = milliSeconds - 10000; // offset 10 second wait
        tsExecutionTime+= tsAbortCount * tsExecutionTime; // To account for compensation transactions
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
        toString.append("Number of reruns: ");
        toString.append(pbsRerunCount);
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
        toString.append("TS Deadlock count: ");
        toString.append(tsDeadLockCount);
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

    public void writeToCsvFile(Long testCaseNumber) throws IOException {
        if(useAmazonS3) {
            File file = getFileByTestCaseNumber(testCaseNumber);
            try (
                    BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardOpenOption.APPEND);
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
//                            .withHeader(
//                            "# of Schedules",
//                            "# of Operations",
//                            "Total time w/o execution",
//                            "# of ELEVATE actions",
//                            "# of DECLINE actions",
//                            "# of GRANT actions",
//                            "PBS abort actions",
//                            "PBS Execution Time",
//                            "TS abort actions",
//                            "Is deadlocked?",
//                            "TS Execution Time",
//                            "NL abort actions",
//                            "Is consistency lost?",
//                            "NL Execution Time",
//                            "PBS Rerun Count")
                    )
            ) {
                csvPrinter.printRecord(scheduleCount, operationCount, totalTimeWithoutExecution,
                        elevateCount, declineCount, grantCount, pbsAbortCount, getPbsExecutionTime(),
                        tsAbortCount, tsDeadLockCount, getTsExecutionTime(), nlAbortCount, isNlConsistencyLost,
                        getNlExecutionTime(), pbsRerunCount, System.lineSeparator());

                csvPrinter.flush();
            }
        } else {
            try (
                    BufferedWriter writer = Files.newBufferedWriter(Paths.get(RESEARCH_OUTPUT_FILE_NAME
                            + testCaseNumber + ".csv"), StandardOpenOption.APPEND);

                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)
            ) {
                csvPrinter.printRecord(scheduleCount, operationCount, totalTimeWithoutExecution,
                        elevateCount, declineCount, grantCount, pbsAbortCount, getPbsExecutionTime(),
                        tsAbortCount, tsDeadLockCount, getTsExecutionTime(), nlAbortCount, isNlConsistencyLost,
                        getNlExecutionTime(), pbsRerunCount, System.lineSeparator());

                csvPrinter.flush();
            }
        }
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
        this.pbsRerunCount = 0;
        this.totalTimeWithoutExecution = 0;

        this.tsStartTime = null;
        this.tsEndTime = null;
        this.tsAbortCount = 0;
        this.tsDeadLockCount = 0;
        this.tsExecutionTime = 0;

        this.nlStartTime = null;
        this.nlEndTime = null;
        this.nlAbortCount = 0;
        this.isNlConsistencyLost = false;
        this.nlExecutionTime = 0;
    }

    @Value("${isTesting:false}")
    private Boolean isTesting;

    public class FileUploadShutdownHook extends Thread {
        @Override
        public void run(){
            if (useAmazonS3 && !isTesting) {
                try {
                    amazonFileUploader.uploadFile(output1);
                } catch (InterruptedException e) {
                    try {
                        System.out.println("Retrying file 1");
                        amazonFileUploader.uploadFile(output1);
                    } catch (InterruptedException e1) {
                        System.out.println("Giving up on file 1");
                    }
                }
                try {
                    amazonFileUploader.uploadFile(output2);
                } catch (InterruptedException e) {
                    try {
                        System.out.println("Retrying file 2");
                        amazonFileUploader.uploadFile(output2);
                    } catch (InterruptedException e1) {
                        System.out.println("Giving up on file 2");
                    }
                }
                try {
                    amazonFileUploader.uploadFile(output3);
                } catch (InterruptedException e) {
                    try {
                        System.out.println("Retrying file 3");
                        amazonFileUploader.uploadFile(output3);
                    } catch (InterruptedException e1) {
                        System.out.println("Giving up on file 3");
                    }
                }
                try {
                    amazonFileUploader.uploadFile(output4);
                } catch (InterruptedException e) {
                    try {
                        System.out.println("Retrying file 4");
                        amazonFileUploader.uploadFile(output4);
                    } catch (InterruptedException e1) {
                        System.out.println("Giving up on file 4");
                    }
                }
                try {
                    amazonFileUploader.uploadFile(output5);
                } catch (InterruptedException e) {
                    try {
                        System.out.println("Retrying file 5");
                        amazonFileUploader.uploadFile(output5);
                    } catch (InterruptedException e1) {
                        System.out.println("Giving up on file 5");
                    }
                }
                try {
                    amazonFileUploader.uploadFile(output6);
                } catch (InterruptedException e) {
                    try {
                        System.out.println("Retrying file 6");
                        amazonFileUploader.uploadFile(output6);
                    } catch (InterruptedException e1) {
                        System.out.println("Giving up on file 6");
                    }
                }
                try {
                    amazonFileUploader.uploadFile(output7);
                } catch (InterruptedException e) {
                    try {
                        System.out.println("Retrying file 7");
                        amazonFileUploader.uploadFile(output7);
                    } catch (InterruptedException e1) {
                        System.out.println("Giving up on file 7");
                    }
                }

                //shutdown
                amazonFileUploader.shutdownTransferManager();
            }
        }
    }
}
