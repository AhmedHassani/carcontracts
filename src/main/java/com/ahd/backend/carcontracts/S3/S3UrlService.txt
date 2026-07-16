package com.ahd.backend.carcontracts.S3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class S3UrlService {
    @Value("${aws.region}")
    private String region;
    @Value("${aws.s3.bucket}")
    private String bucket;
    public String getImageUrl(String key) {
        if (key == null) return null;
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }
} 