package com.badasstechie.product.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;

@Data
public class PaginatedResponse<ContentType> {
    private String previous;
    private String next;
    private long count;
    private List<ContentType> results;

    public PaginatedResponse(Page<ContentType> page, HashMap<String, String> params) {
        this.count = page.getTotalElements();
        if (page.hasNext()) {
            params.put("page", String.valueOf(page.getNumber() + 1));
            params.put("size", String.valueOf(page.getSize()));
            this.next = urlFromParams(params);
        }
        if (page.hasPrevious()) {
            params.put("page", String.valueOf(page.getNumber() - 1));
            params.put("size", String.valueOf(page.getSize()));
            this.previous = urlFromParams(params);
        }
        this.results = page.getContent();
    }

    private String urlFromParams(HashMap<String, String> params) {
        StringBuilder url = new StringBuilder("/api/v1/product/?");
        for (String key : params.keySet()) {
            url.append(key).append("=").append(params.get(key)).append("&");
        }
        return url.substring(0, url.length() - 1);
    }
}
