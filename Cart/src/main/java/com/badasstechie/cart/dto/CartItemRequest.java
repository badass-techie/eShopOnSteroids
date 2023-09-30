package com.badasstechie.cart.dto;

import java.math.BigDecimal;

public record CartItemRequest (
        String productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity
) { }
