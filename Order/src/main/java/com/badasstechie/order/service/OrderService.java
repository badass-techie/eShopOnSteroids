package com.badasstechie.order.service;

import com.badasstechie.order.dto.OrderItemDto;
import com.badasstechie.order.dto.OrderRequest;
import com.badasstechie.order.dto.OrderResponse;
import com.badasstechie.order.model.Order;
import com.badasstechie.order.model.OrderItem;
import com.badasstechie.order.model.OrderStatus;
import com.badasstechie.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    private OrderResponse mapOrderToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getItems().stream().map(this::mapOrderItemToDto).toList(),
                order.getStatus().name(),
                order.getDeliveryAddress(),
                order.getCreated().toString()
        );
    }

    private OrderItem mapDtoToOrderItem(OrderItemDto dto) {
        return OrderItem.builder()
                .productId(dto.productId())
                .productName(dto.productName())
                .unitPrice(dto.unitPrice())
                .quantity(dto.quantity())
                .build();
    }

    private OrderItemDto mapOrderItemToDto(OrderItem orderItem) {
        return new OrderItemDto(
                orderItem.getProductId(),
                orderItem.getProductName(),
                orderItem.getUnitPrice(),
                orderItem.getQuantity()
        );
    }

    public ResponseEntity<OrderResponse> placeOrder(OrderRequest orderRequest) {
        Order order = orderRepository.save(
                Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .items(orderRequest.items().stream().map(this::mapDtoToOrderItem).toList())
                .status(OrderStatus.CREATED)
                .created(Instant.now())
                .build()
        );

        return new ResponseEntity<>(mapOrderToResponse(order), HttpStatus.CREATED);
    }

    public OrderResponse getOrder(Long id) {
        return orderRepository.findById(id)
                .map(this::mapOrderToResponse)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapOrderToResponse)
                .toList();
    }
}
