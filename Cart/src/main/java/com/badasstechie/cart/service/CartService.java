package com.badasstechie.cart.service;

import com.badasstechie.cart.dto.CartItemRequest;
import com.badasstechie.cart.dto.CartItemResponse;
import com.badasstechie.cart.model.CartItem;
import com.badasstechie.cart.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    public ResponseEntity<CartItemResponse> addToCart(CartItemRequest cartItemRequest) {
        CartItem cartItem = CartItem.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(cartItemRequest.userId())
                        .productId(cartItemRequest.productId())
                        .productName(cartItemRequest.productName())
                        .unitPrice(cartItemRequest.unitPrice())
                        .quantity(cartItemRequest.quantity())
                        .build();

        cartItemRepository.save(cartItem);
        return new ResponseEntity<>(mapCartItemToResponse(cartItem), HttpStatus.CREATED);
    }

    public List<CartItemResponse> getCartItems(Long userId) {
        return cartItemRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapCartItemToResponse)
                .collect(Collectors.toList());
    }

    public ResponseEntity<String> incrementQuantity(String id) {
        cartItemRepository.incrementQuantity(id);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> decrementQuantity(String id) {
        cartItemRepository.decrementQuantity(id);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<String> removeFromCart(String id) {
        cartItemRepository.delete(id);
        return ResponseEntity.ok().build();
    }
}
