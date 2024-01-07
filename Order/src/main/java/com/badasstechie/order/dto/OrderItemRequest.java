package com.badasstechie.order.dto;

public record OrderItemRequest(
        String productId,
        Integer quantity
){}
