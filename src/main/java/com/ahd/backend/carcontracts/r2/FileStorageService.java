package com.ahd.backend.carcontracts.r2;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String upload(MultipartFile file);
    void delete(String key);
}
