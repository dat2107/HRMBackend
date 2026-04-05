package com.toto.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {
    String uploadMultipartFile(MultipartFile file, String folder);
    String uploadBase64(String base64Data, String mimeType, String originalFilename);
    String getPresignedUrl(String objectName);
}
