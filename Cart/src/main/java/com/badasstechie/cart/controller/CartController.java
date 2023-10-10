package com.badasstechie.cart.controller;

import com.badasstechie.cart.dto.CartItemRequest;
import com.badasstechie.cart.dto.CartResponse;
import com.badasstechie.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Add a product to your cart (do not provide the user id as it will be extracted from your auth token)")
    public CartResponse addToCart(@RequestBody CartItemRequest cartItemRequest, @RequestParam(name="userId") Long userId) {
        return cartService.addToCart(cartItemRequest, userId);
    }

    @GetMapping
    @Operation(summary = "Get all items in your cart")
    public CartResponse getCartItems(@RequestParam(name="userId") Long userId) {
        return cartService.getCartItems(userId);
    }

    @GetMapping("/{productId}/increment")
    @Operation(summary = "Increase the quantity of a product in your cart")
    public CartResponse incrementQuantity(@RequestParam(name="userId") Long userId, @PathVariable String productId) {
        return cartService.incrementQuantity(userId, productId);
    }

    @GetMapping("/{productId}/decrement")
    @Operation(summary = "Decrease the quantity of a product in your cart")
    public CartResponse decrementQuantity(@RequestParam(name="userId") Long userId, @PathVariable String productId) {
        return cartService.decrementQuantity(userId, productId);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove a product from your cart")
    public CartResponse delete(@RequestParam(name="userId") Long userId, @PathVariable String productId) {
        return cartService.removeFromCart(userId, productId);
    }
}
