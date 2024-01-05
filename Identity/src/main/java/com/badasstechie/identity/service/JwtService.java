package com.badasstechie.identity.service;

import com.badasstechie.identity.util.Pair;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder encoder;

    /**
     * Generates a JWT token for the given subject
     *
     * @param userId the subject of the token
     * @param scope the roles of the user
     * @return Pair<> of token and expiration time
     */
    public Pair<String, Instant> generateToken(Long userId, String scope) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(5, ChronoUnit.MINUTES))
                .subject(userId.toString())
                .claim("scope", scope)
                .build();

        String token = this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new Pair<>(token, claims.getExpiresAt());
    }
}
