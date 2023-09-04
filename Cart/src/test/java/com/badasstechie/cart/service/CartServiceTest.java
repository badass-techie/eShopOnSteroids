package com.badasstechie.cart.service;

import com.badasstechie.cart.dto.CartItemRequest;
import com.badasstechie.cart.dto.CartItemResponse;
import com.badasstechie.cart.model.CartItem;
import com.badasstechie.cart.repository.CartItemRepository;
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

    @Test
    public void testAddToCart() {
        CartItemRequest cartItemRequest = new CartItemRequest(1L, "product1", "Product 1", "image1", BigDecimal.valueOf(10), 1);
        ResponseEntity<CartItemResponse> response = cartService.addToCart(cartItemRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(cartItemRequest.userId(), response.getBody().userId());
        assertEquals(cartItemRequest.productId(), response.getBody().productId());
        assertEquals(cartItemRequest.productName(), response.getBody().productName());
        assertEquals(cartItemRequest.productImage(), response.getBody().productImage());
        assertEquals(cartItemRequest.unitPrice(), response.getBody().unitPrice());
        assertEquals(cartItemRequest.quantity(), response.getBody().quantity());
    }

    @Test
    public void testGetCartItems() {
        Long userId = 1L;
        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(new CartItem("id1", userId, "product1", "Product 1", "image1", BigDecimal.valueOf(10), 1));
        cartItems.add(new CartItem("id2", userId, "product2", "Product 2", "image2", BigDecimal.valueOf(20), 2));
        when(cartItemRepository.findAllByUserId(userId)).thenReturn(cartItems); // mock the repository call to return the cart items we have created
        List<CartItemResponse> response = cartService.getCartItems(userId);
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("id1", response.get(0).id());
        assertEquals("id2", response.get(1).id());
    }

    @Test
    public void testIncrementQuantity() {
        String id = "id1";
        CartItem cartItem = new CartItem(id, 1L, "product1", "Product 1", "image1", BigDecimal.valueOf(10), 1);
        doAnswer(invocation -> {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            return null;
        }).when(cartItemRepository).incrementQuantity(id);  // mock the repository call to run the lambda function in place of its own code
        ResponseEntity<String> response = cartService.incrementQuantity(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, cartItem.getQuantity());
    }

    @Test
    public void testDecrementQuantity() {
        String id = "id1";
        CartItem cartItem = new CartItem(id, 1L, "product1", "Product 1", "image1", BigDecimal.valueOf(10), 2);
        doAnswer(invocation -> {
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            return null;
        }).when(cartItemRepository).decrementQuantity(id);  // mock the repository call to run the lambda function in place of its own code
        ResponseEntity<String> response = cartService.decrementQuantity(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, cartItem.getQuantity());
    }

    @Test
    public void testRemoveFromCart() {
        String id = "id1";
        doNothing().when(cartItemRepository).delete(id);    // mock the repository call to do nothing instead of actually deleting the cart item
        ResponseEntity<String> response = cartService.removeFromCart(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
