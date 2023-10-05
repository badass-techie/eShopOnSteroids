package com.badasstechie.order.service;

import com.badasstechie.order.dto.OrderItemDto;
import com.badasstechie.order.dto.OrderRequest;
import com.badasstechie.order.dto.OrderResponse;
import com.badasstechie.order.dto.ProductStockDto;
import com.badasstechie.order.model.Order;
import com.badasstechie.order.model.OrderItem;
import com.badasstechie.order.model.OrderStatus;
import com.badasstechie.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    public final WebClient.Builder webClientBuilder;

    private OrderResponse mapOrderToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getOrderNumber(),
                order.getItems().stream().map(this::mapOrderItemToDto).toList(),
                order.getItems().stream().map(OrderItem::getUnitPrice).reduce(BigDecimal.ZERO, BigDecimal::add),
                order.getDeliveryAddress(),
                order.getStatus().name(),
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
                "/api/v1/product/" + orderItem.getProductId() + "/image",
                orderItem.getUnitPrice(),
                orderItem.getQuantity()
        );
    }

    public ResponseEntity<OrderResponse> placeOrder(OrderRequest orderRequest, Long userId) {
        // check if stock for each product is enough
        ProductStockDto[] stocks = webClientBuilder
                .build()
                .get()
                .uri("http://product/api/v1/product/stocks",
                        uriBuilder -> uriBuilder.queryParam("ids", orderRequest.items().stream().map(OrderItemDto::productId).toList()).build())
                .retrieve()
                .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Error while checking stock")))
                .bodyToMono(ProductStockDto[].class)
                .block();

        for(int i = 0; i < orderRequest.items().size(); i++) {
            if (stocks[i].getStock() < orderRequest.items().get(i).quantity()) {
                throw new RuntimeException("Product " + orderRequest.items().get(i).productId() + " stock is not enough");
            } else {
                stocks[i].setStock(stocks[i].getStock() - orderRequest.items().get(i).quantity());
            }
        }

        // place order
        Order order = orderRepository.save(
                Order.builder()
                        .userId(userId)
                        .orderNumber(UUID.randomUUID().toString())
                        .items(orderRequest.items().stream().map(this::mapDtoToOrderItem).toList())
                        .status(OrderStatus.CREATED)
                        .deliveryAddress(orderRequest.deliveryAddress())
                        .created(Instant.now())
                        .build()
        );

        // update stocks
        webClientBuilder
                .build()
                .post()
                .uri("http://product/api/v1/product/stocks")
                .bodyValue(stocks)
                .retrieve()
                .onStatus(HttpStatus::isError, response -> Mono.error(new RuntimeException("Error while updating stock")))
                .bodyToMono(String.class)
                .block();

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
