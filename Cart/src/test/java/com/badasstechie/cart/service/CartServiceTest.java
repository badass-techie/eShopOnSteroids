package com.badasstechie.cart.service;

import com.badasstechie.cart.dto.CartItemRequest;
import com.badasstechie.cart.dto.CartItemResponse;
import com.badasstechie.cart.dto.CartResponse;
import com.badasstechie.cart.model.CartItem;
import com.badasstechie.cart.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {
    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartService cartService;
    private CartItem cartItem1;
    private CartItemRequest cartItemRequest;

    @BeforeEach
    void setup() {
        cartItem1 = new CartItem("product1", "Product 1", BigDecimal.valueOf(10), 1);
        cartItemRequest = new CartItemRequest(cartItem1.getProductId(), cartItem1.getProductName(), cartItem1.getUnitPrice(), cartItem1.getQuantity());
    }

    @Test
    void testAddToCart() {
        doNothing().when(cartItemRepository).save(any(), any());    // mock the repository call to do nothing instead of actually saving the cart item
        when(cartItemRepository.findAllByUserId(any())).thenReturn(List.of(cartItem1)); // mock the repository call to return the cart items we have created

        CartResponse response = cartService.addToCart(cartItemRequest, 1L);

        assertEquals(1, response.items().size());
        assertEquals(cartItemRequest.productId(), response.items().get(0).productId());
        assertEquals(cartItemRequest.productName(), response.items().get(0).productName());
        assertEquals(cartItemRequest.unitPrice(), response.items().get(0).unitPrice());
        assertEquals(cartItemRequest.quantity(), response.items().get(0).quantity());
    }
}
