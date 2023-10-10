package com.badasstechie.product.repository;

import com.badasstechie.product.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Product> findByFilters(Pageable pageable, String category, String brandId, Long storeId, String partOfNameOrDescription) {
        Query query = new Query().with(pageable);   // create query with pagination
        Query countQuery = new Query();             // create query for counting total number of products

        // apply specified filters
        if (category != null) {
            query.addCriteria(Criteria.where("category").is(category));
            countQuery.addCriteria(Criteria.where("category").is(category));
        }
        if (brandId != null) {
            query.addCriteria(Criteria.where("brand._id").is(brandId));
            countQuery.addCriteria(Criteria.where("brand._id").is(brandId));
        }
        if (storeId != null) {
            query.addCriteria(Criteria.where("storeId").is(storeId));
            countQuery.addCriteria(Criteria.where("storeId").is(storeId));
        }
        if (partOfNameOrDescription != null) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("name").regex(partOfNameOrDescription, "i"),
                    Criteria.where("description").regex(partOfNameOrDescription, "i")
            ));
            countQuery.addCriteria(new Criteria().orOperator(
                    Criteria.where("name").regex(partOfNameOrDescription, "i"),
                    Criteria.where("description").regex(partOfNameOrDescription, "i")
            ));
        }

        // execute query to get paginated result
        List<Product> products = mongoTemplate.find(query, Product.class);

        // execute query to get total number of products matching the filters
        long total = mongoTemplate.count(countQuery, Product.class);

        return new PageImpl<>(products, pageable, total);
    }
}
