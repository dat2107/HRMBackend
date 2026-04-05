package com.toto.backend.dto.object;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryPeriodDTO {
    private Long categoryId;
    private String categoryName;
    private String period;
    private String feedbackType;
    private String customOptions;
    private boolean isPriority;
}
