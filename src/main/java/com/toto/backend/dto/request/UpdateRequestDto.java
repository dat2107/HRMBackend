package com.toto.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRequestDto {

    @NotBlank
    private String fieldLabel;

    @NotBlank
    private String newVal;

    private String oldVal;

    // Base64-encoded file (optional)
    private String fileData;
    private String fileMimeType;
    private String fileName;
}
