package com.toto.backend.dto.object;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackStatus {
    private String status;
    private String note;
}
