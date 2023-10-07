package com.badasstechie.product.controller;

import com.badasstechie.product.dto.*;
import com.badasstechie.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest, @RequestParam(name="userId") Long userId) {
        return productService.createProduct(productRequest, userId);
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable String id) {
        return productService.getProduct(id);
    }

    @GetMapping
    public List<ProductResponse> getProducts(@RequestParam(name="search", required=false) String searchTerm) {
        return productService.getProducts(searchTerm);
    }

    @GetMapping("/{id}/image")
    public String getProductImage(@PathVariable String id) {
        return productService.getProduct(id).image();
    }

    @GetMapping("/brand/{id}")
    public BrandResponse getBrand(@PathVariable String id) {
        return productService.getBrand(id);
    }

    @GetMapping("/brand")
    public List<BrandResponse> getAllBrands() {
        return productService.getAllBrands();
    }

    @GetMapping("/brand/{brandId}")
    public List<ProductResponse> getProductsByBrand(@PathVariable String brandId) {
        return productService.getProductsByBrand(brandId);
    }

    @GetMapping("/category/{category}")
    public List<ProductResponse> getProductsByCategory(@PathVariable String category) {
        return productService.getProductsByCategory(category);
    }

    @GetMapping("/store/{storeId}")
    public List<ProductResponse> getProductsByStore(@PathVariable Long storeId) {
        return productService.getProductsByStore(storeId);
    }

    @PostMapping("/stocks")
    public ResponseEntity<String> setProductStocks(@RequestBody List<ProductStockRequest> stocks) {
        return productService.setProductStocks(stocks);
    }
}
