package com.badasstechie.cart;

import com.badasstechie.cart.dto.CartItemRequest;
import com.badasstechie.cart.repository.CartItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class CartApplicationTests {
	@Container
	static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:5.0.3-alpine"))
			.withExposedPorts(6379);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CartItemRepository cartItemRepository;

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.redis.host", redisContainer::getHost);
		registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379).toString());
	}

	@Test
	void shouldAddToCart() throws Exception {
		CartItemRequest cartItemRequest = new CartItemRequest("1", "Product 1", BigDecimal.valueOf(100), 1);

		// Response should be 201 CREATED
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/cart?userId=1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(cartItemRequest)))
				.andExpect(status().isCreated());

		// Order should be saved in the database
		assert cartItemRepository.findAllByUserId(1L).size() == 1;
	}
}
