package com.badasstechie.product.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("brand")
public class Brand {
    @Id
    private String id;

    @NotBlank(message = "Name is required")
    private String name;

    @Size(max = 1000000, message = "Image must be less than 1000kB")
    private byte[] image;
}
