package com.badasstechie.product.service;

import com.badasstechie.product.dto.BrandRequest;
import com.badasstechie.product.dto.BrandResponse;
import com.badasstechie.product.dto.ProductRequest;
import com.badasstechie.product.dto.ProductResponse;
import com.badasstechie.product.model.Brand;
import com.badasstechie.product.model.Product;
import com.badasstechie.product.repository.BrandRepository;
import com.badasstechie.product.repository.ProductRepository;
import com.badasstechie.product.util.Time;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;

    private ProductResponse mapProductToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                new String(product.getImage() != null ? product.getImage() : new byte[0]),
                product.getPrice(),
                product.getCategory(),
                product.getBrand() == null ? null : product.getBrand().getId(),
                product.getBrand() == null ? null : product.getBrand().getName(),
                product.getStock(),
                Time.formatTime(product.getCreated())
        );
    }

    private BrandResponse mapBrandToResponse(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                new String(brand.getImage() != null ? brand.getImage() : new byte[0])
        );
    }

    public ResponseEntity<ProductResponse> createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.name())
                .description(productRequest.description())
                .image(productRequest.image().getBytes())
                .price(productRequest.price())
                .category(productRequest.category())
                .brand(brandRepository.findById(productRequest.brandId())
                        .orElseThrow(() -> new RuntimeException("Brand not found")))
                .stock(productRequest.stock())
                .created(Instant.now())
                .build();

        productRepository.save(product);

        return new ResponseEntity<>(mapProductToResponse(product), HttpStatus.CREATED);
    }

    public ProductResponse getProduct(String id) {
        return productRepository.findById(id)
                .map(this::mapProductToResponse)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<ProductResponse> getProducts(String searchTerm) {
        return ((searchTerm == null || searchTerm.isEmpty()) ?
                    productRepository.findAll() :
                    productRepository.findByNameOrDescriptionContainsIgnoreCase(searchTerm))
                .stream()
                .map(this::mapProductToResponse)
                .toList();
    }

    public ResponseEntity<String> setProductStock(String id, Integer stock) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setStock(stock);

        productRepository.save(product);

        return new ResponseEntity<>("Stock updated successfully", HttpStatus.OK);
    }

    public ResponseEntity<BrandResponse> addBrand(BrandRequest brandRequest) {
        Brand brand = Brand.builder()
                .name(brandRequest.name())
                .image(brandRequest.image().getBytes())
                .build();

        brandRepository.save(brand);

        return new ResponseEntity<>(mapBrandToResponse(brand), HttpStatus.CREATED);
    }

    public BrandResponse getBrand(String id) {
        return brandRepository.findById(id)
                .map(this::mapBrandToResponse)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
    }

    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll()
                .stream()
                .map(this::mapBrandToResponse)
                .toList();
    }

    public List<ProductResponse> getProductsByBrand(String brandId) {
        return productRepository.findAllByBrand_Id(brandId)
                .stream()
                .map(this::mapProductToResponse)
                .toList();
    }

    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findAllByCategory(category)
                .stream()
                .map(this::mapProductToResponse)
                .toList();
    }
}
