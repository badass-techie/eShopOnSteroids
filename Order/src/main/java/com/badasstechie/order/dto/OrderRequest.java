package com.badasstechie.order.dto;

import java.util.List;

public record OrderRequest(
    List<OrderItemDto> items,
    String deliveryAddress
){}
