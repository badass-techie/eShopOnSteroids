package com.badasstechie.order.dto;

import java.math.BigDecimal;

public record ProductDetailsGrpcResponse(
    String id,
    String name,
    BigDecimal price,
    Integer quantity
){}
