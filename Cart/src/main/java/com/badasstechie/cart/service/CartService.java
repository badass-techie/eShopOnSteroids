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
                cartItems.stream().reduce(
                        BigDecimal.ZERO,
                        (subtotal, cartItem) -> subtotal.add(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))),
                        BigDecimal::add
                )
        );
    }

    public CartResponse addToCart(CartItemRequest cartItemRequest, Long userId) {
        CartItem cartItem = CartItem.builder()
                        .productId(cartItemRequest.productId())
                        .productName(cartItemRequest.productName())
                        .unitPrice(cartItemRequest.unitPrice())
                        .quantity(cartItemRequest.quantity())
                        .build();

        cartItemRepository.save(cartItem, userId);
        return getCartItems(userId);
    }

    public CartResponse getCartItems(Long userId) {
        return mapCartItemsToResponse(cartItemRepository.findAllByUserId(userId));
    }

    public CartResponse incrementQuantity(Long userId, String productId) {
        cartItemRepository.incrementQuantity(userId, productId);
        return getCartItems(userId);
    }

    public CartResponse decrementQuantity(Long userId, String productId) {
        cartItemRepository.decrementQuantity(userId, productId);
        return getCartItems(userId);
    }

    public CartResponse removeFromCart(Long userId, String productId) {
        cartItemRepository.delete(userId, productId);
        return getCartItems(userId);
    }
}
