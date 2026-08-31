package com.dinoco.oficina.auth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "minha-chave-secreta-de-teste-1234567890";

    @Test
    void deveGerarTokenJwtValido() {

        JwtService jwtService = new JwtService(SECRET);

        String token = jwtService.gerarToken("00000000191");

        assertNotNull(token);
        assertFalse(token.isBlank());

        var decodedJwt = JWT.require(Algorithm.HMAC256(SECRET))
                .withIssuer("oficina-api")
                .build()
                .verify(token);

        assertEquals(
                "oficina-api",
                decodedJwt.getIssuer()
        );

        assertEquals(
                "00000000191",
                decodedJwt.getSubject()
        );

        assertNotNull(
                decodedJwt.getExpiresAt()
        );

        assertTrue(
                decodedJwt
                        .getExpiresAtAsInstant()
                        .isAfter(Instant.now())
        );
    }
}