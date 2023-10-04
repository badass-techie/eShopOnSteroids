package com.badasstechie.identity.model;

import javax.persistence.Entity;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

import static javax.persistence.GenerationType.IDENTITY;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_table") // user is a reserved word in PostgreSQL
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Getter(AccessLevel.NONE)   // Don't generate getter for password
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

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}

