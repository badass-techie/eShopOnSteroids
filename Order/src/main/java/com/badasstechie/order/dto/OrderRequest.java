package com.badasstechie.order.dto;

import java.util.List;
import java.util.Map;

public record OrderRequest(
    List<OrderItemDto> items,
    String deliveryAddress,
    String paymentMethod,
    Map<String, String> payerDetails
){}
