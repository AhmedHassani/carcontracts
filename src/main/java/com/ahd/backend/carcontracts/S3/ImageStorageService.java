package com.ahd.backend.carcontracts.S3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class ImageStorageService {

    private final S3Client s3;
    private final String bucket;
    
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png"
    );

    public ImageStorageService(S3Client s3,
                             @Value("${aws.s3.bucket}") String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    public String upload(MultipartFile file) {
        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(BAD_REQUEST, 
                "Invalid image type. Allowed types: JPG, JPEG, PNG");
        }

        // Get file extension from original filename
        String originalFilename = file.getOriginalFilename();
        String extension = null;
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new ResponseStatusException(BAD_REQUEST, 
                    "Invalid file extension. Allowed extensions: jpg, jpeg, png, webp");
            }
        } else {
            // Fallback to content type for extension
            extension = switch (contentType) {
                case MediaType.IMAGE_JPEG_VALUE -> "jpg";
                case MediaType.IMAGE_PNG_VALUE -> "png";
                case "image/webp" -> "webp";
                default -> throw new ResponseStatusException(BAD_REQUEST, "Unable to determine file extension");
            };
        }

        // Generate unique key with extension
        String key = UUID.randomUUID().toString() + "." + extension;

        try {
            s3.putObject(PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
        return key;
    }

    public void delete(String key) {
        if (key != null) {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        }
    }
}
