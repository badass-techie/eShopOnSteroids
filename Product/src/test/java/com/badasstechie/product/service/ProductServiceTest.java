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
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        List<ProductResponse> response = productService.getProducts(null);

        assertEquals(2, response.size());
        assertEquals("Product 1", response.get(0).name());
        assertEquals(BigDecimal.valueOf(10), response.get(0).price());
        assertEquals("Product 2", response.get(1).name());
        assertEquals(BigDecimal.valueOf(20), response.get(1).price());
    }

    @Test
    void testSearchProducts() {
        when(productRepository.findByNameOrDescriptionContainsIgnoreCase(any())).thenReturn(List.of(product1, product2));

        List<ProductResponse> response = productService.getProducts("prod");

        assertEquals(2, response.size());
        assertEquals(product1.getName(), response.get(0).name());
        assertEquals(product1.getPrice(), response.get(0).price());
        assertEquals(product2.getName(), response.get(1).name());
        assertEquals(product2.getPrice(), response.get(1).price());
    }

    @Test
    void testGetProduct() {
        when(productRepository.findById(any())).thenReturn(java.util.Optional.of(product1));  // mock the repository call to return the product we have created

        ProductResponse response = productService.getProduct("id");

        assertEquals(product1.getName(), response.name());
        assertEquals(BigDecimal.valueOf(10), response.price());
    }

    @Test
    void testGetBrand() {
        when(brandRepository.findById(any())).thenReturn(java.util.Optional.of(brand1));

        BrandResponse response = productService.getBrand("id");

        assertEquals(brand1.getName(), response.name());
    }

    @Test
    void testGetAllBrands() {
        when(brandRepository.findAll()).thenReturn(List.of(brand1, brand2));

        List<BrandResponse> response = productService.getAllBrands();

        assertEquals(2, response.size());
        assertEquals(brand1.getName(), response.get(0).name());
        assertEquals(brand2.getName(), response.get(1).name());
    }

    @Test
    void testGetProductsByBrand() {
        when(productRepository.findAllByBrand_Id(any())).thenReturn(List.of(product1, product2));

        List<ProductResponse> response = productService.getProductsByBrand("brandId");

        assertEquals(2, response.size());
        assertEquals(product1.getName(), response.get(0).name());
        assertEquals(product1.getPrice(), response.get(0).price());
        assertEquals(product2.getName(), response.get(1).name());
        assertEquals(product2.getPrice(), response.get(1).price());
    }

    @Test
    void testGetProductsByCategory() {
        Product product1 = new Product();
        product1.setName("Product 1");
        product1.setPrice(BigDecimal.valueOf(10));
        product1.setCreated(Instant.now());

        Product product2 = new Product();
        product2.setName("Product 2");
        product2.setPrice(BigDecimal.valueOf(20));
        product2.setCreated(Instant.now());

        when(productRepository.findAllByCategory(any())).thenReturn(List.of(product1, product2));

        List<ProductResponse> products = productService.getProductsByCategory("category");

        assertEquals(2, products.size());
        assertEquals("Product 1", products.get(0).name());
        assertEquals(BigDecimal.valueOf(10), products.get(0).price());
        assertEquals("Product 2", products.get(1).name());
        assertEquals(BigDecimal.valueOf(20), products.get(1).price());
    }

    @Test
    void setProductStocks() {
        // Given
        // A list of products that correspond to the stocks
        List<Product> products = List.of(product1, product2);
        List<ProductStockRequest> stocks = List.of(
                new ProductStockRequest(product1.getId(), product1.getStock()),
                new ProductStockRequest(product2.getId(), product2.getStock())
        );

        // When
        // The product repository is stubbed to return the products when findAllById is called with the ids
        for (Product product : products) {
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        }

        // Then
        // The setProductStocks method should update the stock of each product in the repository and return a success response
        assertEquals(new ResponseEntity<>("Stocks updated", HttpStatus.OK), productService.setProductStocks(stocks));
        verify(productRepository).saveAll(products);    // verify that the saveAll method is called with the products
        for (Product product : products) {
            assertEquals(product.getStock(), stocks.stream().filter(stock -> stock.id().equals(product.getId())).findFirst().get().stock());
        }
    }
}
