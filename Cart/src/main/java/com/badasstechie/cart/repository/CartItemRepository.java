package com.badasstechie.cart.repository;

import com.badasstechie.cart.model.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CartItemRepository {
    private static final String KEY = "cart_item";
    private final RedisTemplate<String, CartItem> redisTemplate;

    public boolean save(CartItem cartItem) {
        redisTemplate.opsForHash().put(KEY, cartItem.getId(), cartItem);
        return true;
    }

    public List<CartItem> findAllByUserId(Long userId) {
        return redisTemplate.opsForHash().values(KEY).stream()
                .filter(cartItem -> ((CartItem) cartItem).getUserId().equals(userId))
                .map(cartItem -> (CartItem) cartItem)
                .collect(Collectors.toList());
    }

    public void incrementQuantity(String id) {
        CartItem cartItem = (CartItem) redisTemplate.opsForHash().get(KEY, id);
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            redisTemplate.opsForHash().put(KEY, id, cartItem);
        }
        else {
            throw new RuntimeException("Cart item not found");
        }
    }

    public void decrementQuantity(String id) {
        CartItem cartItem = (CartItem) redisTemplate.opsForHash().get(KEY, id);
        if (cartItem != null && cartItem.getQuantity() > 0) {
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            redisTemplate.opsForHash().put(KEY, id, cartItem);
        }
        else {
            throw new RuntimeException("Cart item not found");
        }
    }

    public void delete(String id) {
        redisTemplate.opsForHash().delete(KEY, id);
    }
}
