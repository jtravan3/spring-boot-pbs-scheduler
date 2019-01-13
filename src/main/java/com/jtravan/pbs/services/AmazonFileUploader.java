package com.jtravan.pbs.services;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class AmazonFileUploader {

    @Value("${s3.bucket.name}")
    private String s3BucketName;

    @Value("${s3.access.key.id}")
    private String accessKeyId;

    @Value("${s3.access.secret}")
    private String accessSecret;

    private boolean isInitialized = false;

    private TransferManager transferManager;

    private void initializeTransferManager() {
        if (!isInitialized) {
            AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, accessSecret);
            transferManager = new TransferManager(credentials);
            isInitialized = true;
        }
    }

    public void uploadFile(File file) throws InterruptedException {
        initializeTransferManager();
        String name;
        if (file.getName().startsWith("/") || file.getName().startsWith("\\")) {
            name = file.getName().substring(1);
        } else {
            name = file.getName();
        }
        Upload myUpload = transferManager.upload(s3BucketName, name, file);
        myUpload.waitForCompletion();
    }

    public void shutdownTransferManager() {
        isInitialized = false;
        transferManager.shutdownNow();
    }
}
