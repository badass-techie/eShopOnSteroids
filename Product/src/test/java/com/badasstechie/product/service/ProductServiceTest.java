package com.badasstechie.product.service;

import com.badasstechie.product.dto.BrandRequest;
import com.badasstechie.product.dto.BrandResponse;
import com.badasstechie.product.dto.ProductRequest;
import com.badasstechie.product.dto.ProductResponse;
import com.badasstechie.product.model.Brand;
import com.badasstechie.product.model.Product;
import com.badasstechie.product.repository.BrandRepository;
import com.badasstechie.product.repository.ProductRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    public void testCreateProduct() {
        Brand brand = new Brand();
        brand.setName("Brand Name");
        when(brandRepository.findById(any())).thenReturn(java.util.Optional.of(brand));   // mock the repository call to return the brand we have created

        Product product = new Product();
        product.setName("Product Name");
        product.setPrice(BigDecimal.valueOf(10));
        when(productRepository.save(any())).thenReturn(product);    // mock the repository call to return the product we have created

        ProductRequest productRequest = new ProductRequest("Product Name", "Description", "Image", BigDecimal.valueOf(10), "Category", "BrandId", 10);
        ResponseEntity<ProductResponse> response = productService.createProduct(productRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Product Name", response.getBody().name());
        assertEquals(BigDecimal.valueOf(10), response.getBody().price());
    }

    @Test
    public void testGetAllProducts() {
        Product product1 = new Product();
        product1.setName("Product 1");
        product1.setPrice(BigDecimal.valueOf(10));
        product1.setCreated(Instant.now());

        Product product2 = new Product();
        product2.setName("Product 2");
        product2.setPrice(BigDecimal.valueOf(20));
        product2.setCreated(Instant.now());

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        List<ProductResponse> products = productService.getProducts(null);

        assertEquals(2, products.size());
        assertEquals("Product 1", products.get(0).name());
        assertEquals(BigDecimal.valueOf(10), products.get(0).price());
        assertEquals("Product 2", products.get(1).name());
        assertEquals(BigDecimal.valueOf(20), products.get(1).price());
    }

    @Test
    public void testSearchProducts() {
        Product product1 = new Product();
        product1.setName("Product 1");
        product1.setPrice(BigDecimal.valueOf(10));
        product1.setCreated(Instant.now());

        Product product2 = new Product();
        product2.setName("Product 2");
        product2.setPrice(BigDecimal.valueOf(20));
        product2.setCreated(Instant.now());

        when(productRepository.findByNameOrDescriptionContainsIgnoreCase(any())).thenReturn(List.of(product1, product2));

        List<ProductResponse> products = productService.getProducts("prod");

        assertEquals(2, products.size());
        assertEquals("Product 1", products.get(0).name());
        assertEquals(BigDecimal.valueOf(10), products.get(0).price());
        assertEquals("Product 2", products.get(1).name());
        assertEquals(BigDecimal.valueOf(20), products.get(1).price());
    }

    @Test
    public void testGetProduct() {
        Product product = new Product();
        product.setName("Product Name");
        product.setPrice(BigDecimal.valueOf(10));
        product.setCreated(Instant.now());
        when(productRepository.findById(any())).thenReturn(java.util.Optional.of(product));  // mock the repository call to return the product we have created

        ProductResponse response = productService.getProduct("id");

        assertEquals("Product Name", response.name());
        assertEquals(BigDecimal.valueOf(10), response.price());
    }

    @Test
    public void testSetProductStock() {
        Product product = new Product();
        product.setName("Product Name");
        product.setPrice(BigDecimal.valueOf(10));
        when(productRepository.findById(any())).thenReturn(java.util.Optional.of(product));
        when(productRepository.save(any())).thenReturn(product);

        ResponseEntity<String> response = productService.setProductStock("id", 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Stock updated successfully", response.getBody());
    }

    @Test
    public void testAddBrand() {
        Brand brand = new Brand();
        brand.setName("Brand Name");
        when(brandRepository.save(any())).thenReturn(brand);

        BrandRequest brandRequest = new BrandRequest("Brand Name", "Image");
        ResponseEntity<BrandResponse> response = productService.addBrand(brandRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Brand Name", response.getBody().name());
    }

    @Test
    public void testGetBrand() {
        Brand brand = new Brand();
        brand.setName("Brand Name");
        when(brandRepository.findById(any())).thenReturn(java.util.Optional.of(brand));

        BrandResponse response = productService.getBrand("id");

        assertEquals("Brand Name", response.name());
    }

    @Test
    public void testGetAllBrands() {
        Brand brand1 = new Brand();
        brand1.setName("Brand 1");

        Brand brand2 = new Brand();
        brand2.setName("Brand 2");

        when(brandRepository.findAll()).thenReturn(List.of(brand1, brand2));

        List<BrandResponse> brands = productService.getAllBrands();

        assertEquals(2, brands.size());
        assertEquals("Brand 1", brands.get(0).name());
        assertEquals("Brand 2", brands.get(1).name());
    }

    @Test
    public void testGetProductsByBrand() {
        Product product1 = new Product();
        product1.setName("Product 1");
        product1.setPrice(BigDecimal.valueOf(10));
        product1.setCreated(Instant.now());

        Product product2 = new Product();
        product2.setName("Product 2");
        product2.setPrice(BigDecimal.valueOf(20));
        product2.setCreated(Instant.now());

        when(productRepository.findAllByBrand_Id(any())).thenReturn(List.of(product1, product2));

        List<ProductResponse> products = productService.getProductsByBrand("brandId");

        assertEquals(2, products.size());
        assertEquals("Product 1", products.get(0).name());
        assertEquals(BigDecimal.valueOf(10), products.get(0).price());
        assertEquals("Product 2", products.get(1).name());
        assertEquals(BigDecimal.valueOf(20), products.get(1).price());
    }

    @Test
    public void testGetProductsByCategory() {
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
}
