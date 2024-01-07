package com.badasstechie.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productId,
        String productName,
        String productImageUrl,
        BigDecimal unitPrice,
        Integer quantity
){}
