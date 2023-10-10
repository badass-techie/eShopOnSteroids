package com.badasstechie.cart.model;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItem implements Serializable {
    private String productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
}
