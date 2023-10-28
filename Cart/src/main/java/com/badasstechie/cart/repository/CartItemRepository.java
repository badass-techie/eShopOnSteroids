package com.badasstechie.cart.repository;

import com.badasstechie.cart.model.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class CartItemRepository {
    private static final String KEY = "cart_item";
    private final RedisTemplate<String, CartItem> redisTemplate;

    @Autowired
    public CartItemRepository(RedisTemplate<String, CartItem> redisTemplate) {
        this.redisTemplate = redisTemplate;
        redisTemplate.expire(KEY, 1L, TimeUnit.DAYS);
    }

    public void save(CartItem cartItem, Long userId) {
        redisTemplate.opsForHash().put(userId.toString(), cartItem.getProductId(), cartItem);
    }

    public CartItem findByUserIdAndProductId(Long userId, String productId) {
        return (CartItem)redisTemplate.opsForHash().get(userId.toString(), productId);
    }

    public List<CartItem> findAllByUserId(Long userId) {
        return redisTemplate.opsForHash().values(userId.toString()).stream()
                .map(cartItem -> (CartItem) cartItem)
                .collect(Collectors.toList());
    }

    public void incrementQuantity(Long userId, String productId) {
        CartItem cartItem = (CartItem) redisTemplate.opsForHash().get(userId.toString(), productId);
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            redisTemplate.opsForHash().put(userId.toString(), productId, cartItem);
        }
        else {
            throw new RuntimeException("Cart item not found");
        }
    }

    public void decrementQuantity(Long userId, String productId) {
        CartItem cartItem = (CartItem) redisTemplate.opsForHash().get(userId.toString(), productId);
        if (cartItem != null && cartItem.getQuantity() > 0) {
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            redisTemplate.opsForHash().put(userId.toString(), productId, cartItem);
        }
        else {
            throw new RuntimeException("Cart item not found");
        }
    }

    public void delete(Long userId, String productId) {
        redisTemplate.opsForHash().delete(userId.toString(), productId);
    }
}
