package com.toto.backend.dto.object;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FieldConfigDTO {
    private String fieldName;
    private String fieldKey;
    private boolean isLocked;
    private boolean isOptionalUpload;
    private List<String> dropdownOptions;
}
