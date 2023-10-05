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

    @GetMapping("/{id}/increment")
    public CartResponse incrementQuantity(@PathVariable String id, @RequestParam(name="userId") Long userId) {
        return cartService.incrementQuantity(id, userId);
    }

    @GetMapping("/{id}/decrement")
    public CartResponse decrementQuantity(@PathVariable String id, @RequestParam(name="userId") Long userId) {
        return cartService.decrementQuantity(id, userId);
    }

    @DeleteMapping("/{id}")
    public CartResponse delete(@PathVariable String id, @RequestParam(name="userId") Long userId) {
        return cartService.removeFromCart(id, userId);
    }
}
