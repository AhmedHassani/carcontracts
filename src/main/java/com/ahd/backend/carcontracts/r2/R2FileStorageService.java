package com.ahd.backend.carcontracts.r2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class R2FileStorageService implements FileStorageService {

    private final S3Client s3;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp",
            MediaType.APPLICATION_PDF_VALUE
    );

    private static final Map<String, String> CONTENT_TYPE_TO_EXT = Map.of(
            MediaType.IMAGE_JPEG_VALUE, "jpg",
            MediaType.IMAGE_PNG_VALUE, "png",
            "image/webp", "webp",
            MediaType.APPLICATION_PDF_VALUE, "pdf"
    );

    private static final Set<String> ALLOWED_EXTENSIONS =
            new HashSet<>(CONTENT_TYPE_TO_EXT.values());

    private static final String INVALID_TYPE_MSG =
            "Invalid file type. Allowed types: " + String.join(", ", ALLOWED_EXTENSIONS);

    @Override
    public String upload(MultipartFile file) {
        // log.info("=== Starting R2 Upload ===");
        // log.info("Bucket: {}", bucket);
        // log.info("File name: {}", file.getOriginalFilename());
        // log.info("File size: {} bytes", file.getSize());
        // log.info("Content type: {}", file.getContentType());

        var contentType = Optional.ofNullable(file.getContentType())
                .orElseThrow(() -> {
                    log.error("Missing Content-Type header");
                    return bad("Missing Content-Type header");
                });

        /* 1. Check Content-Type */
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            log.error("Invalid content type: {}", contentType);
            throw bad(INVALID_TYPE_MSG);
        }

        /* 2. Determine extension */
        var ext = getExtension(file.getOriginalFilename())
                .orElse(CONTENT_TYPE_TO_EXT.get(contentType));
        
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext)) {
            log.error("Invalid extension: {}", ext);
            throw bad(INVALID_TYPE_MSG);
        }

        /* 3. Generate key */
        var key = UUID.randomUUID() + "." + ext;
            //log.info("Generated key: {}", key);

        /* 4. Upload to R2 */
        try (var in = file.getInputStream()) {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();
            
            //log.info("Uploading to R2 - Bucket: {}, Key: {}", bucket, key);
            
            s3.putObject(putRequest,
                    RequestBody.fromInputStream(in, file.getSize()));
            
            //log.info("✅ File uploaded successfully to R2: {}", key);
            return key;
            
        } catch (IOException ex) {
            log.error("❌ IO Exception during R2 upload", ex);
            throw new UncheckedIOException("Failed to upload file", ex);
        } catch (S3Exception ex) {
            log.error("❌ R2 S3 error during upload");
            log.error("Error message: {}", ex.awsErrorDetails().errorMessage());
            log.error("Error code: {}", ex.awsErrorDetails().errorCode());
            log.error("Status code: {}", ex.statusCode());
            log.error("Request ID: {}", ex.requestId());
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, 
                "Failed to upload file to R2: " + ex.awsErrorDetails().errorMessage());
        } catch (Exception ex) {
            log.error("❌ Unexpected error during R2 upload", ex);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, 
                "Failed to upload file to R2: " + ex.getMessage());
        }
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) return;
        try {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            //log.info("File deleted successfully from R2: {}", key);
        } catch (S3Exception ex) {
            log.warn("R2 delete failed for key={} – {}", key, ex.awsErrorDetails().errorMessage());
        }
    }

    private static Optional<String> getExtension(String filename) {
        if (filename == null) return Optional.empty();
        int dot = filename.lastIndexOf('.');
        return (dot >= 0 && dot < filename.length() - 1)
                ? Optional.of(filename.substring(dot + 1).toLowerCase(Locale.ROOT))
                : Optional.empty();
    }

    private static ResponseStatusException bad(String msg) {
        return new ResponseStatusException(BAD_REQUEST, msg);
    }
}