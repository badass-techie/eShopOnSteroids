package com.badasstechie.order.dto;

import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        List<OrderItemDto> items,
        String deliveryAddress,
        String status,
        String created
){}
