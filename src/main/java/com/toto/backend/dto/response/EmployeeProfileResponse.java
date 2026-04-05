package com.toto.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmployeeProfileResponse {

    private BaseInfo baseInfo;
    private List<FieldDetail> details;
    private boolean hasPending;
    private ConfirmStatus confirmStatus;

    @Data
    @Builder
    public static class BaseInfo {
        private String id;
        private String fullName;
        private String department;
        private String positionCode;
        private String email;
    }

    @Data
    @Builder
    public static class FieldDetail {
        private String key;
        private String label;
        private String value;
        private String rawValue;
        private boolean editable;
        private String inputType;
        private boolean isPending;
        private String pendingVal;
    }

    @Data
    @Builder
    public static class ConfirmStatus {
        private boolean globalActive;
        private boolean userConfirmed;
        private String confirmedAt;
        private boolean hasPending;
    }
}
