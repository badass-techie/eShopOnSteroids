package com.badasstechie.order.controller;

import com.badasstechie.order.dto.OrderRequest;
import com.badasstechie.order.dto.OrderResponse;
import com.badasstechie.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new order")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest, @RequestParam(name="userId") Long userId){
        return orderService.placeOrder(orderRequest, userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by id")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping
    @Operation(summary = "Get all orders by user id")
    public List<OrderResponse> getAllOrders(@RequestParam(name="userId") Long userId) {
        return orderService.getOrdersByUser(userId);
    }
}
