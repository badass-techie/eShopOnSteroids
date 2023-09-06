package com.badasstechie.product;

import com.badasstechie.product.dto.BrandRequest;
import com.badasstechie.product.dto.ProductRequest;
import com.badasstechie.product.repository.BrandRepository;
import com.badasstechie.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductApplicationTests {
	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.3");	// remote mongoDB docker container

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private BrandRepository brandRepository;

	/*
	 * Set the properties for the MongoDB connection
	 */
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Test
	void shouldCreateBrandAndProduct() throws Exception {
		BrandRequest brandRequest = new BrandRequest(
				"Test Brand",
				"Test Brand Image"
		);

		MvcResult brand = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/product/brand")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(brandRequest)))
				.andExpect(status().isCreated())
				.andReturn();

		String brandId = objectMapper.readTree(brand.getResponse().getContentAsString()).get("id").asText();

		ProductRequest productRequest = new ProductRequest(
				"Test Product",
				"Test Product Description",
				"Test Product Image",
				BigDecimal.valueOf(100.00),
				"Test Product Category",
				brandId,
				10
		);

		// Response should be 201 CREATED
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/product")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(productRequest)))
				.andExpect(status().isCreated());

		// Product should be saved in the database
		assert productRepository.findAll().size() == 1;
	}

	@AfterEach
	public void cleanup() {
		// Clean up resources after each test
		productRepository.deleteAll();
		brandRepository.deleteAll();
	}
}
