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
@RedisHash("cart_item")
public class CartItem implements Serializable {
    private String id;
    private Long userId;
    private String productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
}
