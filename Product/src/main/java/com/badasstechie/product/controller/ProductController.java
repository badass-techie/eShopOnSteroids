package com.badasstechie.product.controller;

import com.badasstechie.product.dto.*;
import com.badasstechie.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    public PaginatedResponse<ProductResponse> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productService.getProducts(pageable, category, brandId, storeId, search);
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

    @PostMapping("/stocks")
    public ResponseEntity<String> setProductStocks(@RequestBody List<ProductStockDto> stocks) {
        return productService.setProductStocks(stocks);
    }
}
