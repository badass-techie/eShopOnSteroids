package com.badasstechie.product.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
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

    private Long storeId;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @Size(max = 1000000, message = "Image must be less than 1000kB")
    private byte[] image;

    @Min(value = 1, message = "Price must be at least 1")
    private BigDecimal price;

    private String category;

    @DBRef
    private Brand brand;

    @Min(value = 0, message = "Stock must be at least 0")
    private Integer stock;

    @PastOrPresent(message = "Created date must be in the past or present")
    private Instant created;
}
