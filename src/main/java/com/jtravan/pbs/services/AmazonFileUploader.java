package com.jtravan.pbs.services;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
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

    public void uploadFile(File file) {
        System.setProperty(SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");
        AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, accessSecret);

        AmazonS3 s3client = new AmazonS3Client(credentials);
        s3client.setRegion(Region.getRegion(Regions.US_EAST_1));

        s3client.putObject(new PutObjectRequest(s3BucketName, file.getName(), file));
    }
}
