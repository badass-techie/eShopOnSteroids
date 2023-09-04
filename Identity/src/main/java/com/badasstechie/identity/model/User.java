package com.badasstechie.identity.model;

import jakarta.persistence.Entity;
import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;

import static jakarta.persistence.GenerationType.IDENTITY;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_table") // user is a reserved word in PostgreSQL
public class User {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @Lob
    private String bio;

    @Column(nullable = false, length = 1000000) // 1000kB
    @Size(max = 1000000, message = "Image must be less than 1000kB")
    private byte[] image;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @PastOrPresent(message = "Created date must be in the past or present")
    private Instant created;

    private boolean active;
}

