package com.badasstechie.cart.dto;

import java.math.BigDecimal;

public record CartItemRequest (
        Long userId,
        String productId,
        String productName,
        String productImage,
        BigDecimal unitPrice,
        Integer quantity
) { }
