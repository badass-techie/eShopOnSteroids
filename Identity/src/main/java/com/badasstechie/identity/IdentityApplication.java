package com.badasstechie.identity;

import com.badasstechie.identity.model.User;
import com.badasstechie.identity.model.UserRole;
import com.badasstechie.identity.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import java.time.Instant;

@SpringBootApplication
@EnableDiscoveryClient    // Enable the Eureka Client
public class IdentityApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(UserRepository userRepository) {
        return args -> {
            // Create an admin from the environment variables ADMIN_EMAIL and ADMIN_PASSWORD
            String adminEmail = System.getenv("ADMIN_EMAIL");
            String adminPassword = System.getenv("ADMIN_PASSWORD");
            if (adminEmail != null && adminPassword != null) {
                userRepository.findByEmail(adminEmail)
                        .orElseGet(() -> userRepository.save(
								User.builder()
                                        .name("Admin")
                                        .email(adminEmail)
                                        .password(adminPassword)
										.image(new byte[0])
                                        .role(UserRole.ADMIN)
										.created(Instant.now())
										.active(true)
                                        .build()
                        ));
            }
        };
    }
}
