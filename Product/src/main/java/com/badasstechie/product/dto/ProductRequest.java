package com.badasstechie.product.dto;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String description,
        String image,
        BigDecimal price,
        String category,
        String brandId,
        Integer stock)
{}
