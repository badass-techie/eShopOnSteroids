package com.badasstechie.product.service;

import com.badasstechie.product.dto.*;
import com.badasstechie.product.model.Brand;
import com.badasstechie.product.model.Product;
import com.badasstechie.product.repository.BrandRepository;
import com.badasstechie.product.repository.ProductRepository;
import com.badasstechie.product.util.Time;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;

    private ProductResponse mapProductToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getStoreId(),
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

    public ResponseEntity<ProductResponse> createProduct(ProductRequest productRequest, Long storeId) {
        // create brand if not found
        Brand brand = brandRepository.findByName(productRequest.brandName())
                .orElseGet(() -> {
                    Brand newBrand = Brand.builder()
                            .name(productRequest.brandName())
                            .image(productRequest.brandImage().getBytes())
                            .build();

                    brandRepository.save(newBrand);

                    return newBrand;
                });

        Product product = Product.builder()
                .storeId(storeId)
                .name(productRequest.name())
                .description(productRequest.description())
                .image(productRequest.image().getBytes())
                .price(productRequest.price())
                .category(productRequest.category())
                .brand(brandRepository.findById(brand.getId())
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

    public PaginatedResponse<ProductResponse> getProducts(Pageable pageable, String category, String brandId, Long storeId, String partOfNameOrDescription) {
        return new PaginatedResponse<>(
                productRepository
                        .findByFilters(pageable, category, brandId, storeId, partOfNameOrDescription)
                        .map(this::mapProductToResponse)
        );
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

    public ResponseEntity<String> setProductStocks(List<ProductStockDto> stocks) {
        List<Product> products = new ArrayList<>();
        for (ProductStockDto stock : stocks) {
            if (stock.quantity() < 0)
                return new ResponseEntity<>("Stock quantity cannot be negative", HttpStatus.BAD_REQUEST);

            Optional<Product> productOptional = productRepository.findById(stock.id());
            if (productOptional.isEmpty())
                return new ResponseEntity<>("Product " + stock.id() + " not found", HttpStatus.NOT_FOUND);

            Product product = productOptional.get();
            product.setStock(stock.quantity());
            products.add(product);
        }

        productRepository.saveAll(products);

        return new ResponseEntity<>("Stocks updated", HttpStatus.OK);
    }

    @RabbitListener(queues = "${message-bus.queue-name}")
    public void newOrderListener(List<ProductStockDto> productsOrdered){
        log.info("Received new order: {}", productsOrdered);

        List<Product> products = new ArrayList<>();
        for (ProductStockDto productOrdered : productsOrdered) {
            Optional<Product> productOptional = productRepository.findById(productOrdered.id());
            if (productOptional.isEmpty())
                continue;

            Product product = productOptional.get();
            int newStock = Math.max(product.getStock() - productOrdered.quantity(), 0);

            product.setStock(newStock);
            products.add(product);
        }

        productRepository.saveAll(products);
        log.info("Stocks updated");
    }
}
