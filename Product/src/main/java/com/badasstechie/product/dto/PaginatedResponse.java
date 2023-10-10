package com.badasstechie.product.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PaginatedResponse<ContentType> {
    private List<ContentType> results;
    private int previousPage;
    private int nextPage;
    private long count;

    public PaginatedResponse(Page<ContentType> page) {
        this.results = page.getContent();
        this.previousPage = page.hasPrevious() ? page.previousPageable().getPageNumber() : -1;
        this.nextPage = page.hasNext() ? page.nextPageable().getPageNumber() : -1;
        this.count = page.getTotalElements();
    }
}
