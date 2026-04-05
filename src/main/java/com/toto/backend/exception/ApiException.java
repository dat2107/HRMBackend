package com.toto.backend.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiException {

    private HttpStatus status;
    private String code;
    private String message;
    private String path;
    private long timestamp;
}

