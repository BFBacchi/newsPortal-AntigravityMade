package com.newsportal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.UUID;

/**
 * Service for handling file storage (S3-compatible)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.storage.s3.endpoint:}")
    private String s3Endpoint;

    @Value("${app.storage.s3.bucket:newsportal-media}")
    private String bucket;

    @Value("${app.storage.s3.access-key:}")
    private String accessKey;

    @Value("${app.storage.s3.secret-key:}")
    private String secretKey;

    @Value("${app.storage.s3.region:us-east-1}")
    private String region;

    private S3Client s3Client;

    /**
     * Initialize S3 client lazily
     */
    private S3Client getS3Client() {
        if (s3Client == null) {
            var credentialsProvider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey));

            var builder = S3Client.builder()
                    .credentialsProvider(credentialsProvider)
                    .region(Region.of(region));

            if (s3Endpoint != null && !s3Endpoint.isEmpty()) {
                builder.endpointOverride(URI.create(s3Endpoint));
            }

            s3Client = builder.build();
        }
        return s3Client;
    }

    /**
     * Download image from URL and store in S3
     */
    public String downloadAndStoreImage(String imageUrl, String newsId) {
        log.info("Downloading and storing image from: {}", imageUrl);

        try {
            byte[] imageData;

            // Handle base64 data URLs (from some AI services)
            if (imageUrl.startsWith("data:image")) {
                String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                imageData = Base64.getDecoder().decode(base64Data);
            } else {
                // Download from URL
                WebClient webClient = webClientBuilder.build();
                imageData = webClient.get()
                        .uri(imageUrl)
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .block();
            }

            if (imageData == null || imageData.length == 0) {
                throw new RuntimeException("Failed to download image");
            }

            // Generate unique filename
            String filename = String.format("news/%s/%s.jpg", newsId, UUID.randomUUID());

            // Upload to S3
            return uploadToS3(imageData, filename, "image/jpeg");

        } catch (Exception e) {
            log.error("Error downloading and storing image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to store image", e);
        }
    }

    /**
     * Upload file to S3
     */
    public String uploadToS3(byte[] data, String key, String contentType) {
        log.info("Uploading to S3: {}", key);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            getS3Client().putObject(putObjectRequest, RequestBody.fromBytes(data));

            // Return public URL
            if (s3Endpoint != null && !s3Endpoint.isEmpty()) {
                return String.format("%s/%s/%s", s3Endpoint, bucket, key);
            } else {
                return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
            }

        } catch (Exception e) {
            log.error("Error uploading to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload to S3", e);
        }
    }

    /**
     * Upload file from InputStream
     */
    public String uploadToS3(InputStream inputStream, String key, String contentType, long contentLength) {
        log.info("Uploading stream to S3: {}", key);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            getS3Client().putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));

            // Return public URL
            if (s3Endpoint != null && !s3Endpoint.isEmpty()) {
                return String.format("%s/%s/%s", s3Endpoint, bucket, key);
            } else {
                return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
            }

        } catch (Exception e) {
            log.error("Error uploading stream to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload to S3", e);
        }
    }

    /**
     * Delete file from S3
     */
    public void deleteFromS3(String key) {
        log.info("Deleting from S3: {}", key);

        try {
            getS3Client().deleteObject(builder -> builder.bucket(bucket).key(key));
        } catch (Exception e) {
            log.error("Error deleting from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete from S3", e);
        }
    }
}
