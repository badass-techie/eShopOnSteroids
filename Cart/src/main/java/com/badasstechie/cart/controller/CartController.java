package com.badasstechie.cart.controller;

import com.badasstechie.cart.dto.CartItemRequest;
import com.badasstechie.cart.dto.CartResponse;
import com.badasstechie.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addToCart(@RequestBody CartItemRequest cartItemRequest, @RequestParam(name="userId") Long userId) {
        return cartService.addToCart(cartItemRequest, userId);
    }

    @GetMapping
    public CartResponse getCartItems(@RequestParam(name="userId") Long userId) {
        return cartService.getCartItems(userId);
    }

    @GetMapping("/{productId}/increment")
    public CartResponse incrementQuantity(@RequestParam(name="userId") Long userId, @PathVariable String productId) {
        return cartService.incrementQuantity(userId, productId);
    }

    @GetMapping("/{productId}/decrement")
    public CartResponse decrementQuantity(@RequestParam(name="userId") Long userId, @PathVariable String productId) {
        return cartService.decrementQuantity(userId, productId);
    }

    @DeleteMapping("/{productId}")
    public CartResponse delete(@RequestParam(name="userId") Long userId, @PathVariable String productId) {
        return cartService.removeFromCart(userId, productId);
    }
}
