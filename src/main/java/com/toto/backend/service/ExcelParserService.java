package com.toto.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface ExcelParserService {
    String parseToJson(MultipartFile file);
}
