package com.badasstechie.order.service;

import com.badasstechie.order.dto.*;
import com.badasstechie.order.model.Order;
import com.badasstechie.order.model.OrderItem;
import com.badasstechie.order.model.OrderStatus;
import com.badasstechie.order.repository.OrderRepository;
import com.badasstechie.product.grpc.ProductGrpcServiceGrpc;
import com.badasstechie.product.grpc.ProductStocksRequest;
import com.badasstechie.product.grpc.ProductStocksResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final AmqpTemplate updateStockTemplate, orderAwaitingPaymentTemplate;

    @GrpcClient("product-grpc-service")
    private ProductGrpcServiceGrpc.ProductGrpcServiceBlockingStub productGrpcService;

    private final Map<String, List<String>> paymentRequirements = new HashMap<>();

    @Autowired
    public OrderService(OrderRepository orderRepository, AmqpTemplate updateStockTemplate, AmqpTemplate orderAwaitingPaymentTemplate) {
        this.orderRepository = orderRepository;
        this.updateStockTemplate = updateStockTemplate;
        this.orderAwaitingPaymentTemplate = orderAwaitingPaymentTemplate;
        paymentRequirements.put("stripe", List.of("cardToken"));
        paymentRequirements.put("mpesa", List.of("phoneNumber"));
    }

    private OrderResponse mapOrderToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getOrderNumber(),
                order.getItems().stream().map(this::mapOrderItemToDto).toList(),
                order.getItems().stream().reduce(
                        BigDecimal.ZERO,
                        (subtotal, orderItem) -> subtotal.add(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()))),
                        BigDecimal::add
                ),
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
        // validate the payment details
        String paymentMethod = orderRequest.paymentMethod();
        if (!paymentRequirements.containsKey(paymentMethod)) {
            throw new RuntimeException("Unknown payment method");
        }
        
        List<String> requiredFields = paymentRequirements.get(paymentMethod);
        for (String field : requiredFields) {
            if (!orderRequest.payerDetails().containsKey(field))
                throw new RuntimeException(field + " not found but required for " + paymentMethod + " payment");
        }

        List<ProductStockDto> stocks = getProductStocks(orderRequest.items().stream().map(OrderItemDto::productId).toList());

        // throw exception if length of stocks and order items are not equal
        if (stocks.size() != orderRequest.items().size())
            throw new RuntimeException("Product stocks not found");

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
                        .status(OrderStatus.AWAITING_PAYMENT)
                        .deliveryAddress(orderRequest.deliveryAddress())
                        .created(Instant.now())
                        .build()
        );

        // publish message to message bus for payment to be processed
        OrderPaymentRequest paymentRequest = new OrderPaymentRequest(
                order.getId(),
                order.getOrderNumber(),
                order.getItems().stream().reduce(
                        BigDecimal.ZERO,
                        (subtotal, orderItem) -> subtotal.add(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()))),
                        BigDecimal::add
                ),
                "KES",
                orderRequest.paymentMethod(),
                orderRequest.payerDetails()
        );
        orderAwaitingPaymentTemplate.convertAndSend(paymentRequest);

        // publish message to message bus for stock to be updated
        List<ProductStockDto> productsOrdered = order.getItems().stream()
                .map(item -> new ProductStockDto(item.getProductId(), item.getQuantity()))
                .toList();
        updateStockTemplate.convertAndSend(productsOrdered);

        return new ResponseEntity<>(mapOrderToResponse(order), HttpStatus.CREATED);
    }

    @CircuitBreaker(name = "product-grpc-service")
    public List<ProductStockDto> getProductStocks(List<String> ids) {
        // call grpc service to get product stocks
        ProductStocksResponse response = productGrpcService.getProductStocks(
                ProductStocksRequest.newBuilder().addAllIds(ids).build());

        return response.getStocksList().stream()
                .map(stock -> new ProductStockDto(stock.getId(), stock.getQuantity()))
                .toList();
    }

    // update order status after payment has been processed
    @RabbitListener(queues = "${message-bus.queues.order-payment-processed}")
    public void updateOrderStatus(OrderPaymentResponse paymentResponse) {
        Order order = orderRepository.findById(paymentResponse.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (paymentResponse.resultStatus() == PaymentResult.SUCCESS) {
            order.setStatus(OrderStatus.SHIPPING);
            orderRepository.save(order);
        } else if (paymentResponse.resultStatus() == PaymentResult.FAILED) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        }

        log.info("Payment processed for order number {}. Result: {}", paymentResponse.orderNumber(), paymentResponse.resultMessage());
    }

    public OrderResponse getOrder(Long id) {
        return orderRepository.findById(id)
                .map(this::mapOrderToResponse)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapOrderToResponse)
                .toList();
    }
}
