package com.badasstechie.cart.controller;

import com.badasstechie.cart.dto.CartItemRequest;
import com.badasstechie.cart.dto.CartItemResponse;
import com.badasstechie.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartItemResponse> addToCart(@RequestBody CartItemRequest cartItemRequest, @RequestParam(name="userId") Long userId) {
        return cartService.addToCart(cartItemRequest, userId);
    }

    @GetMapping("/{userId}")
    public List<CartItemResponse> getCartItems(@PathVariable Long userId) {
        return cartService.getCartItems(userId);
    }

    @GetMapping("/{id}/increment")
    public ResponseEntity<String> incrementQuantity(@PathVariable String id) {
        return cartService.incrementQuantity(id);
    }

    @GetMapping("/{id}/decrement")
    public ResponseEntity<String> decrementQuantity(@PathVariable String id) {
        return cartService.decrementQuantity(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        return cartService.removeFromCart(id);
    }
}
