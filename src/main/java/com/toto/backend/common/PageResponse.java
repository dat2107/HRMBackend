package com.toto.backend.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PageResponse<T> {
    private List<T> items;
    private long totalItems;
    private int totalPages;
    private int page;
    private int size;
}
