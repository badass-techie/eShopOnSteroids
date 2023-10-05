package com.badasstechie.order.service;

import com.badasstechie.order.dto.OrderItemDto;
import com.badasstechie.order.dto.OrderRequest;
import com.badasstechie.order.dto.OrderResponse;
import com.badasstechie.order.dto.ProductStockResponse;
import com.badasstechie.order.model.Order;
import com.badasstechie.order.model.OrderItem;
import com.badasstechie.order.model.OrderStatus;
import com.badasstechie.order.repository.OrderRepository;
import com.badasstechie.product.grpc.ProductGrpcServiceGrpc;
import com.badasstechie.product.grpc.ProductStocksRequest;
import com.badasstechie.product.grpc.ProductStocksResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    public final WebClient.Builder webClientBuilder;

    @GrpcClient("product-grpc-service")
    private ProductGrpcServiceGrpc.ProductGrpcServiceBlockingStub productGrpcService;

    @Autowired
    public OrderService(OrderRepository orderRepository, WebClient.Builder webClientBuilder) {
        this.orderRepository = orderRepository;
        this.webClientBuilder = webClientBuilder;
    }

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
        // call grpc service to get product stocks
        ProductStocksResponse response = productGrpcService.getProductStocks(
                ProductStocksRequest.newBuilder().addAllIds(orderRequest.items().stream().map(OrderItemDto::productId).toList()).build());

        List<ProductStockResponse> stocks = response.getStocksList().stream()
                .map(stock -> new ProductStockResponse(stock.getId(), stock.getStock()))
                .toList();

        // throw exception if stock of any product is not enough
        for(int i = 0; i < orderRequest.items().size(); ++i) {
            if (stocks.get(i).stock() < orderRequest.items().get(i).quantity())
                throw new RuntimeException("Product " + orderRequest.items().get(i).productId() + " stock is not enough");
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

        // TODO: publish message to message bus that order is created

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
