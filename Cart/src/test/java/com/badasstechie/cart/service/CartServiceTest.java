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
    private CartItem cartItem1, cartItem2;
    private List<CartItem> cartItems;
    private CartItemRequest cartItemRequest;

    @BeforeEach
    void setup() {
        cartItem1 = new CartItem("id1", 5L, "product1", "Product 1", BigDecimal.valueOf(10), 1);
        cartItem2 = new CartItem("id2", 6L, "product2", "Product 2", BigDecimal.valueOf(20), 2);
        cartItems = List.of(cartItem1, cartItem2);
        cartItemRequest = new CartItemRequest(cartItem1.getProductId(), cartItem1.getProductName(), cartItem1.getUnitPrice(), cartItem1.getQuantity());
    }

    @Test
    void testAddToCart() {
        ResponseEntity<CartResponse> response = cartService.addToCart(cartItemRequest, 1L);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
//        assertEquals(cartItemRequest.productId(), response.getBody().productId());
//        assertEquals(cartItemRequest.productName(), response.getBody().productName());
//        assertEquals(cartItemRequest.unitPrice(), response.getBody().unitPrice());
//        assertEquals(cartItemRequest.quantity(), response.getBody().quantity());
    }

    @Test
    void testGetCartItems() {
        when(cartItemRepository.findAllByUserId(any())).thenReturn(cartItems); // mock the repository call to return the cart items we have created
        CartResponse response = cartService.getCartItems(1L);
        assertNotNull(response);
        assertEquals(2, response.items().size());
        assertEquals(cartItem1.getId(), response.items().get(0).id());
        assertEquals(cartItem2.getId(), response.items().get(1).id());
    }

    @Test
    void testIncrementQuantity() {
        Integer initialQuantity = cartItem1.getQuantity();
        doAnswer(invocation -> {
            cartItem1.setQuantity(cartItem1.getQuantity() + 1);
            return null;
        }).when(cartItemRepository).incrementQuantity(any());  // mock the repository call to run the lambda function in place of its own code
        ResponseEntity<String> response = cartService.incrementQuantity("id");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(initialQuantity + 1, cartItem1.getQuantity());
    }

    @Test
    void testDecrementQuantity() {
        Integer initialQuantity = cartItem1.getQuantity();
        doAnswer(invocation -> {
            cartItem1.setQuantity(cartItem1.getQuantity() - 1);
            return null;
        }).when(cartItemRepository).decrementQuantity(any());  // mock the repository call to run the lambda function in place of its own code
        ResponseEntity<String> response = cartService.decrementQuantity("id");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(initialQuantity - 1, cartItem1.getQuantity());
    }

    @Test
    void testRemoveFromCart() {
        String id = "id1";
        doNothing().when(cartItemRepository).delete(id);    // mock the repository call to do nothing instead of actually deleting the cart item
        ResponseEntity<String> response = cartService.removeFromCart(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
