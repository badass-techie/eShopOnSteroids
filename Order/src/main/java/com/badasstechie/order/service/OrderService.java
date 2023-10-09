package com.badasstechie.order.service;

import com.badasstechie.order.dto.OrderItemDto;
import com.badasstechie.order.dto.OrderRequest;
import com.badasstechie.order.dto.OrderResponse;
import com.badasstechie.order.dto.ProductStockDto;
import com.badasstechie.order.model.Order;
import com.badasstechie.order.model.OrderItem;
import com.badasstechie.order.model.OrderStatus;
import com.badasstechie.order.repository.OrderRepository;
import com.badasstechie.product.grpc.ProductGrpcServiceGrpc;
import com.badasstechie.product.grpc.ProductStocksRequest;
import com.badasstechie.product.grpc.ProductStocksResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    @GrpcClient("product-grpc-service")
    private ProductGrpcServiceGrpc.ProductGrpcServiceBlockingStub productGrpcService;

    @Value("${message-bus.exchange-name}")
    private String messageBusExchangeName;

    @Value("${message-bus.routing-key}")
    private String messageBusRoutingKey;

    @Autowired
    public OrderService(OrderRepository orderRepository, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
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
        List<ProductStockDto> stocks = getProductStocks(orderRequest.items().stream().map(OrderItemDto::productId).toList());

        // throw exception if stock of any product is not enough
        for(int i = 0; i < orderRequest.items().size(); ++i) {
            if (stocks.get(i).quantity() < orderRequest.items().get(i).quantity())
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

        // publish message to message bus with the product ids and quantities
        List<ProductStockDto> productsOrdered = order.getItems().stream()
                .map(item -> new ProductStockDto(item.getProductId(), item.getQuantity()))
                .toList();

        rabbitTemplate.convertAndSend(messageBusExchangeName, messageBusRoutingKey, productsOrdered);

        return new ResponseEntity<>(mapOrderToResponse(order), HttpStatus.CREATED);
    }

    @CircuitBreaker(name = "product-grpc-service")
    private List<ProductStockDto> getProductStocks(List<String> ids) {
        // call grpc service to get product stocks
        ProductStocksResponse response = productGrpcService.getProductStocks(
                ProductStocksRequest.newBuilder().addAllIds(ids).build());

        return response.getStocksList().stream()
                .map(stock -> new ProductStockDto(stock.getId(), stock.getQuantity()))
                .toList();
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
