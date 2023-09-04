package com.badasstechie.product.model;

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

    private String name;

    private byte[] image;
}
