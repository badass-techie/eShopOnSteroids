package com.badasstechie.product.controller;

import com.badasstechie.product.dto.*;
import com.badasstechie.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest, @RequestParam(name="userId") Long userId) {
        return productService.createProduct(productRequest, userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by id")
    public ProductResponse getProduct(@PathVariable String id) {
        return productService.getProduct(id);
    }

    @GetMapping
    @Operation(summary = "Get products by a combination of various filters (if any is null, it gets ignored)")
    public PaginatedResponse<ProductResponse> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        HashMap<String, String> params = new HashMap<>();
        if (category != null) params.put("category", category);
        if (brandId != null) params.put("brandId", brandId);
        if (storeId != null) params.put("storeId", storeId.toString());
        if (search != null) params.put("search", search);

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getProducts(pageable, category, brandId, storeId, search);

        return new PaginatedResponse<>(products, params);
    }

    @GetMapping("/{id}/image")
    @Operation(summary = "Get a product's image by id")
    public String getProductImage(@PathVariable String id) {
        return productService.getProduct(id).image();
    }

    @GetMapping("/brand/{id}")
    @Operation(summary = "Get a brand by id")
    public BrandResponse getBrand(@PathVariable String id) {
        return productService.getBrand(id);
    }

    @GetMapping("/brand")
    @Operation(summary = "Get all brands")
    public List<BrandResponse> getAllBrands() {
        return productService.getAllBrands();
    }

    @PostMapping("/stocks")
    @Operation(summary = "Set product stocks")
    public ResponseEntity<String> setProductStocks(@RequestBody List<ProductStockDto> stocks) {
        return productService.setProductStocks(stocks);
    }
}
