package com.badasstechie.product.repository;

import com.badasstechie.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String>, ProductRepositoryCustom {
}
