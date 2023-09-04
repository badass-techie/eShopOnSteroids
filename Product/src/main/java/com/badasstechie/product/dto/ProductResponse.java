package com.badasstechie.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String name,
        String description,
        String image,
        BigDecimal price,
        String category,
        String brandId,
        String brandName,
        Integer stock,
        String created) {
}
