package com.badasstechie.order.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        String productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity
        // TODO: String productImage
){}
