package com.toto.backend.common;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseRequest<BODY> {
    private String functionName;
    private String requestDateTime;
    private String requestId;
    @Valid
    private BODY data;
}
