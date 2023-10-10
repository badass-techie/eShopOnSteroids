package com.badasstechie.product.repository;

import com.badasstechie.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    /**
     * Find products by any combination of the following filters (if any is null, it gets ignored):
     *
     * @param category (optional) - category of the product
     * @param brandId (optional) - brand id of the product
     * @param storeId (optional) - user id of the store that owns the product
     * @param partOfNameOrDescription (optional) - part of the name or description of the product (case-insensitive)
     * @param pageable (required) - page number and size
     * @return paginated list of products
     */
    Page<Product> findByFilters(Pageable pageable, String category, String brandId, Long storeId, String partOfNameOrDescription);
}
