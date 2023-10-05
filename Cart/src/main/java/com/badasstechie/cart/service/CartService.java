package com.badasstechie.cart.service;

import com.badasstechie.cart.dto.CartItemRequest;
import com.badasstechie.cart.dto.CartItemResponse;
import com.badasstechie.cart.dto.CartResponse;
import com.badasstechie.cart.model.CartItem;
import com.badasstechie.cart.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;

    public CartItemResponse mapCartItemToResponse(CartItem cartItem){
        return new CartItemResponse(
            cartItem.getId(),
            cartItem.getUserId(),
            cartItem.getProductId(),
            cartItem.getProductName(),
            "/api/v1/product/" + cartItem.getProductId() + "/image",
            cartItem.getUnitPrice(),
            cartItem.getQuantity()
        );
    }

    public CartResponse mapCartItemsToResponse(List<CartItem> cartItems){
        return new CartResponse(
                cartItems.stream().map(this::mapCartItemToResponse).toList(),
                cartItems.stream().map(CartItem::getUnitPrice).reduce(BigDecimal.ZERO, BigDecimal::add)
        );
    }

    public CartResponse addToCart(CartItemRequest cartItemRequest, Long userId) {
        CartItem cartItem = CartItem.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .productId(cartItemRequest.productId())
                        .productName(cartItemRequest.productName())
                        .unitPrice(cartItemRequest.unitPrice())
                        .quantity(cartItemRequest.quantity())
                        .build();

        cartItemRepository.save(cartItem);
        return getCartItems(userId);
    }

    public CartResponse getCartItems(Long userId) {
        return mapCartItemsToResponse(cartItemRepository.findAllByUserId(userId));
    }

    public CartResponse incrementQuantity(String id, Long userId) {
        cartItemRepository.incrementQuantity(id);
        return getCartItems(userId);
    }

    public CartResponse decrementQuantity(String id, Long userId) {
        cartItemRepository.decrementQuantity(id);
        return getCartItems(userId);
    }

    public CartResponse removeFromCart(String id, Long userId) {
        cartItemRepository.delete(id);
        return getCartItems(userId);
    }
}
