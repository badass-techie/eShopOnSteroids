package com.badasstechie.order.service;

import com.badasstechie.order.dto.*;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderService orderService;

    private OrderService orderServiceSpy;

    private Order order;
    private OrderRequest orderRequest;
    private List<ProductDetailsGrpcResponse> productDetails;
    
    @BeforeEach
    void setUp() {
        orderServiceSpy = spy(orderService);

        OrderItem orderItem = new OrderItem(1L, "1", "Product 1", BigDecimal.valueOf(10), 1);
        order = new Order(1L, 1L, "Order 1", List.of(orderItem), OrderStatus.AWAITING_PAYMENT, "Address 1", Instant.now());
        OrderItemRequest orderItemRequest = new OrderItemRequest(orderItem.getProductId(), orderItem.getQuantity());
        orderRequest = new OrderRequest(List.of(orderItemRequest), "Address 1", "mpesa", Map.of("phoneNumber", "254700000000"));
        productDetails = List.of(new ProductDetailsGrpcResponse(orderItem.getProductId(), orderItem.getProductName(), orderItem.getUnitPrice(), orderItem.getQuantity()));
    }

    @Test
    void testPlaceOrder() {
        doAnswer(invocation -> productDetails).when(orderServiceSpy).getProductDetails(any());
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        ResponseEntity<OrderResponse> response = orderServiceSpy.placeOrder(orderRequest, 1L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(order.getId(), response.getBody().id());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(rabbitTemplate, times(2)).convertAndSend(any());
    }
}
