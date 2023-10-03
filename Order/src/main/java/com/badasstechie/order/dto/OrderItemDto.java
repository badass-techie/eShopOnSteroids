package com.badasstechie.order.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        String productId,
        String productName,
        String productImageUrl,
        BigDecimal unitPrice,
        Integer quantity
){}
