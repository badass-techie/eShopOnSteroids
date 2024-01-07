package com.badasstechie.order.service;

import com.badasstechie.order.dto.*;
import com.badasstechie.order.model.Order;
import com.badasstechie.order.model.OrderItem;
import com.badasstechie.order.model.OrderStatus;
import com.badasstechie.order.repository.OrderRepository;
import com.badasstechie.product.grpc.ProductDetailsRequest;
import com.badasstechie.product.grpc.ProductDetailsResponse;
import com.badasstechie.product.grpc.ProductGrpcServiceGrpc;
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
                order.getItems().stream().map(this::mapOrderItemToResponse).toList(),
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

    private OrderItem mapRequestToOrderItem(OrderItemRequest request, String productName, BigDecimal unitPrice) {
        return OrderItem.builder()
                .productId(request.productId())
                .productName(productName)
                .unitPrice(unitPrice)
                .quantity(request.quantity())
                .build();
    }

    private OrderItemResponse mapOrderItemToResponse(OrderItem orderItem) {
        return new OrderItemResponse(
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

        List<ProductDetailsGrpcResponse> products = getProductDetails(orderRequest.items().stream().map(OrderItemRequest::productId).toList());

        // throw exception if length of products and order items are not equal
        if (products.size() != orderRequest.items().size())
            throw new RuntimeException("Some products not found in catalog");

        // throw exception if stock of any product is not enough
        List<OrderItem> orderItems = new ArrayList<>();
        for(int i = 0; i < orderRequest.items().size(); ++i) {
            if (products.get(i).quantity() < orderRequest.items().get(i).quantity())
                throw new RuntimeException("Product " + orderRequest.items().get(i).productId() + " stock is not enough");
            orderItems.add(mapRequestToOrderItem(orderRequest.items().get(i), products.get(i).name(), products.get(i).price()));
        }

        // place order
        Order order = orderRepository.save(
                Order.builder()
                        .userId(userId)
                        .orderNumber(UUID.randomUUID().toString())
                        .items(orderItems)
                        .status(OrderStatus.AWAITING_PAYMENT)
                        .deliveryAddress(orderRequest.deliveryAddress())
                        .created(Instant.now())
                        .build()
        );

        // publish event to event bus for payment to be processed
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

        // publish event to event bus for stock to be updated
        List<ProductStockDto> productsOrdered = order.getItems().stream()
                .map(item -> new ProductStockDto(item.getProductId(), item.getQuantity()))
                .toList();
        updateStockTemplate.convertAndSend(productsOrdered);

        return new ResponseEntity<>(mapOrderToResponse(order), HttpStatus.CREATED);
    }

    @CircuitBreaker(name = "product-grpc-service")
    public List<ProductDetailsGrpcResponse> getProductDetails(List<String> ids) {
        // call grpc service to get product stocks
        ProductDetailsResponse response = productGrpcService.getProductDetails(
                ProductDetailsRequest.newBuilder().addAllIds(ids).build());

        return response.getProductDetailsList().stream()
                .map(product -> new ProductDetailsGrpcResponse(product.getId(), product.getName(), new BigDecimal(product.getPrice()), product.getStock()))
                .toList();
    }

    // update order status after payment has been processed
    @RabbitListener(queues = "${event-bus.queues.order-payment-processed}")
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
