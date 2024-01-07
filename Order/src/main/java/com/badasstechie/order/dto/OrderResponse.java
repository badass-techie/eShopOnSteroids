package com.badasstechie.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        String orderNumber,
        List<OrderItemResponse> items,
        BigDecimal totalCost,
        String deliveryAddress,
        String status,
        String created
){}
