package com.badasstechie.product.controller;

import com.badasstechie.product.dto.BrandRequest;
import com.badasstechie.product.dto.BrandResponse;
import com.badasstechie.product.dto.ProductRequest;
import com.badasstechie.product.dto.ProductResponse;
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
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest) {
        return productService.createProduct(productRequest);
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

    @PutMapping("/{id}/stock")
    public ResponseEntity<String> setProductStock(@PathVariable String id, @RequestBody Integer stock) {
        return productService.setProductStock(id, stock);
    }

    @PostMapping("/brand")
    public ResponseEntity<BrandResponse> addBrand(@RequestBody BrandRequest brandRequest) {
        return productService.addBrand(brandRequest);
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
    public List<ProductResponse> getProductSByCategory(@PathVariable String category) {
        return productService.getProductsByCategory(category);
    }
}
