package com.badasstechie.identity;

import com.badasstechie.identity.dto.UserRequest;
import com.badasstechie.identity.model.User;
import com.badasstechie.identity.model.UserRole;
import com.badasstechie.identity.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class IdentityApplicationTests {
	@Container
	static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.2")
			.withDatabaseName("testdb")
			.withUsername("testuser")
			.withPassword("testpass");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	private User user;

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
		registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
	}

	@BeforeEach
	void setUp() {
		// set up test data before each test
		user = User.builder()
				.id(1L)
				.name("testuser")
				.email("testuser@example.com")
				.password("password")
				.bio("test bio")
				.image(new byte[0])
				.role(UserRole.USER)
				.created(Instant.now())
				.active(true)
				.build();
	}

	@Test
	void shouldRegisterUser() throws Exception {
		UserRequest userRequest = new UserRequest(
				user.getName(),
				user.getEmail(),
				user.getPassword(),
				user.getBio(),
				"",
				user.getRole().name()
		);

		// Response should be 201 CREATED
		mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/identity/user")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userRequest)))
				.andExpect(status().isCreated());

		// Order should be saved in the database
		assert userRepository.findAll().size() == 1;
	}

	@AfterEach
	public void cleanup() {
		// Clean up resources after each test
		userRepository.deleteAll();
	}
}
