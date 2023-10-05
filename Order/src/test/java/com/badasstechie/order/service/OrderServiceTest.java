package com.badasstechie.order.service;

import com.badasstechie.order.dto.OrderItemDto;
import com.badasstechie.order.dto.OrderRequest;
import com.badasstechie.order.dto.OrderResponse;
import com.badasstechie.order.model.Order;
import com.badasstechie.order.model.OrderItem;
import com.badasstechie.order.model.OrderStatus;
import com.badasstechie.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderRequest orderRequest;
    
    @BeforeEach
    void setUp() {
        OrderItem orderItem = new OrderItem(1L, "1", "Product 1", BigDecimal.valueOf(10), 1);
        order = new Order(1L, 1L, "Order 1", List.of(orderItem), OrderStatus.CREATED, "Address 1", Instant.now());
        OrderItemDto orderItemDto = new OrderItemDto(orderItem.getProductId(), orderItem.getProductName(), "", orderItem.getUnitPrice(), orderItem.getQuantity());
        orderRequest = new OrderRequest(List.of(orderItemDto), "Address 1");
    }

    @Test
    void testPlaceOrder() {
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        ResponseEntity<OrderResponse> response = orderService.placeOrder(orderRequest, 1L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().id());
    }

    @Test
    void testGetOrder() {
        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));    // mock the repository call to return the order we have created

        OrderResponse response = orderService.getOrder(1L);

        assertEquals(1L, response.id());
    }

    @Test
    void testGetAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(order));   // mock the repository call to return the order we have created

        List<OrderResponse> response = orderService.getAllOrders();

        assertEquals(1, response.size());
    }
}
