package com.jtravan.pbs.services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;

@Component
public class AmazonFileUploader {

    @Value("${s3.bucket.name}")
    private String s3BucketName;

    @Value("${s3.access.client.id}")
    private String s3AccessClientId;

    @Value("${s3.access.secret.key}")
    private String s3AccessSecretKey;

    public void uploadFiles(File... files) {

        AWSCredentials credentials = new BasicAWSCredentials(s3AccessClientId, s3AccessSecretKey);
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTP);
        AmazonS3 client = new AmazonS3Client(credentials, clientConfig);
        client.setRegion(Region.getRegion(Regions.US_EAST_2));

        Instant time = Instant.now();
        int i = 0;
        for (File file : files) {
            i++;
            try {
                client.putObject(s3BucketName, time.toString() + "/output" + i, file);
            } catch (AmazonServiceException e) {
                System.err.println("Error occurred whiled uploading files to S3: " + e.toString());
            }
        }

        System.out.println("Shutting down S3 Client...");
        client.shutdown();
    }

}
