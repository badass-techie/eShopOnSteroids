package com.badasstechie.order.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import java.time.Instant;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "order_table") // order is a reserved keyword in SQL
public class Order {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @OneToMany(cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<OrderItem> items;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    @PastOrPresent(message = "Created date must be in the past or present")
    private Instant created;
}
