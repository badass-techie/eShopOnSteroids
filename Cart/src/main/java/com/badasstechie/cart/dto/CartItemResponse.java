package com.badasstechie.cart.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        String id,
        Long userId,
        String productId,
        String productName,
        String productImageUrl,
        BigDecimal unitPrice,
        Integer quantity) {
}
