package com.toto.backend.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    private static final long MAX_EXCEL_BYTES = 50L * 1024 * 1024; // 50MB for admin uploads

    public String uploadMultipartFile(MultipartFile file, String folder) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File không được trống");
            }
            if (file.getSize() > MAX_EXCEL_BYTES) {
                throw new IllegalArgumentException("File quá lớn (Tối đa 50MB)");
            }
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String ext = originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf('.'))
                    : "";
            String objectName = folder + "/" + UUID.randomUUID() + ext;
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

            try (InputStream stream = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(stream, file.getSize(), -1)
                        .contentType(contentType)
                        .build());
            }

            return getPresignedUrl(objectName);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("[MINIO] Multipart upload failed: {}", e.getMessage());
            throw new RuntimeException("File upload failed");
        }
    }

    public String uploadBase64(String base64Data, String mimeType, String originalFilename) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64Data);
            validateFileSize(bytes.length);
            validateMimeType(mimeType);

            String ext = getExtension(mimeType);
            String objectName = "uploads/" + UUID.randomUUID() + "." + ext;

            try (InputStream stream = new ByteArrayInputStream(bytes)) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(stream, bytes.length, -1)
                        .contentType(mimeType)
                        .build());
            }

            return getPresignedUrl(objectName);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("[MINIO] Upload failed: {}", e.getMessage());
            throw new RuntimeException("File upload failed");
        }
    }

    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("[MINIO] Get URL failed: {}", e.getMessage());
            return "";
        }
    }

    private void validateFileSize(int size) {
        if (size > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File quá lớn (Tối đa 5MB)");
        }
    }

    private void validateMimeType(String mimeType) {
        java.util.Set<String> allowed = java.util.Set.of(
                "image/jpeg", "image/png", "image/jpg", "application/pdf"
        );
        if (!allowed.contains(mimeType)) {
            throw new IllegalArgumentException("Loại file không được phép: " + mimeType);
        }
    }

    private String getExtension(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "application/pdf" -> "pdf";
            default -> "bin";
        };
    }
}
