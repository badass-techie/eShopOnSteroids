package com.badasstechie.product.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("product")
public class Product {
    @Id
    private String id;

    private String name;

    private String description;

    private byte[] image;

    private BigDecimal price;

    private String category;

    @DBRef
    private Brand brand;

    private Integer stock;

    private Instant created;
}
