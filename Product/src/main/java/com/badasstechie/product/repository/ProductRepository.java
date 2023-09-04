package com.badasstechie.product.repository;

import com.badasstechie.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findAllByCategory(String category);
    List<Product> findAllByBrand_Id(String brandId);
    @Query("{$or:[{'name': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}]}")
    List<Product> findByNameOrDescriptionContainsIgnoreCase(String searchTerm);
}
