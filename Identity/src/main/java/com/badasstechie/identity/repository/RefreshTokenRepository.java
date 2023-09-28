package com.badasstechie.identity.repository;

import com.badasstechie.identity.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByOwnerEmailAndToken(String email, String token);
    void deleteAllByOwnerEmail(String email);
}
