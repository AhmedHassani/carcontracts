package com.ahd.backend.carcontracts.S3;

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
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;




@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3;

    @Value("${aws.s3.bucket}") private String bucket;

    /* ----------------– config ----------------– */

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp",
            MediaType.APPLICATION_PDF_VALUE          // 👈 add PDFs
    );

    private static final Map<String,String> CONTENT_TYPE_TO_EXT = Map.of(
            MediaType.IMAGE_JPEG_VALUE, "jpg",
            MediaType.IMAGE_PNG_VALUE , "png",
            "image/webp"              , "webp",
            MediaType.APPLICATION_PDF_VALUE, "pdf"
    );

    private static final Set<String> ALLOWED_EXTENSIONS =
            new HashSet<>(CONTENT_TYPE_TO_EXT.values());

    private static final String INVALID_TYPE_MSG =
            "Invalid file type. Allowed types: " + String.join(", ", ALLOWED_EXTENSIONS);


    @Override
    public String upload(MultipartFile file) {
        var contentType = Optional.ofNullable(file.getContentType())
                .orElseThrow(() -> bad("Missing Content-Type header"));

        /* 1. Check Content-Type */
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) throw bad(INVALID_TYPE_MSG);

        /* 2. Determine extension */
        var ext = getExtension(file.getOriginalFilename())
                .orElse(CONTENT_TYPE_TO_EXT.get(contentType)); // may still be null
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext))
            throw bad(INVALID_TYPE_MSG);

        /* 3. Generate key */
        var key = UUID.randomUUID() + "." + ext;

        /* 4. Upload */
        try (var in = file.getInputStream()) {
            s3.putObject(PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(in, file.getSize()));
        } catch (IOException ex) {
            log.error("S3 upload failed", ex);
            throw new UncheckedIOException("Failed to upload file", ex);
        }
        return key;
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) return;          // idempotent
        try {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (S3Exception ex) {
            log.warn("S3 delete failed for key={} – {}", key, ex.awsErrorDetails().errorMessage());
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
