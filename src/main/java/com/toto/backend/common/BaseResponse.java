package com.toto.backend.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BaseResponse<T> {
    private int status;
    private String message;
    private T data;
    private long timestamp;
}
