package com.badasstechie.product.service;

import com.badasstechie.product.dto.*;
import com.badasstechie.product.model.Brand;
import com.badasstechie.product.model.Product;
import com.badasstechie.product.repository.BrandRepository;
import com.badasstechie.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private ProductService productService;

    Product product1 = new Product();
    Product product2 = new Product();
    Brand brand1 = new Brand();
    Brand brand2 = new Brand();

    @BeforeEach
    void setUp() {
        product1.setId("1");
        product1.setName("Product 1");
        product1.setPrice(BigDecimal.valueOf(10));
        product1.setStock(10);
        product1.setCreated(Instant.now());

        product2.setId("2");
        product2.setName("Product 2");
        product2.setPrice(BigDecimal.valueOf(20));
        product1.setStock(20);
        product2.setCreated(Instant.now());

        brand1.setName("Brand 1");

        brand2.setName("Brand 2");
    }

    @Test
    void testCreateProduct() {
        when(brandRepository.findById(any())).thenReturn(java.util.Optional.of(brand1));   // mock the repository call to return the brand we have created
        when(productRepository.save(any())).thenReturn(product1);    // mock the repository call to return the product we have created

        ProductRequest productRequest = new ProductRequest(product1.getName(), "Description", "Image", product1.getPrice(), "Category", brand1.getName(), "", 10);
        ResponseEntity<ProductResponse> response = productService.createProduct(productRequest, 1L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(product1.getName(), response.getBody().name());
        assertEquals(product1.getPrice(), response.getBody().price());
    }

    @Test
    void testSetProductStocks() {
        // Given
        // A list of products that correspond to the stocks
        List<Product> products = List.of(product1, product2);
        List<ProductStockDto> stocks = List.of(
                new ProductStockDto(product1.getId(), product1.getStock()),
                new ProductStockDto(product2.getId(), product2.getStock())
        );

        // When
        // The product repository is stubbed to return the products when findAllById is called with the ids
        for (Product product : products) {
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        }

        // Then
        // The setProductStocks method should update the quantity of each product in the repository and return a success response
        assertEquals(new ResponseEntity<>("Stocks updated", HttpStatus.OK), productService.setProductStocks(stocks));
        verify(productRepository).saveAll(products);    // verify that the saveAll method is called with the products
        for (Product product : products) {
            assertEquals(product.getStock(), stocks.stream().filter(stock -> stock.id().equals(product.getId())).findFirst().get().quantity());
        }
    }
}
