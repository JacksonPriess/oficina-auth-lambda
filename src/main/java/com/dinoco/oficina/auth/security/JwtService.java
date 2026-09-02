package com.dinoco.oficina.auth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class JwtService {

    private static final String ISSUER = "oficina-api";

    private final String secret;

    public JwtService(String secret) {
        this.secret = secret;
    }

    public String gerarToken(String cpf) {

        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(cpf)
                .withIssuedAt(Instant.now())
                .withExpiresAt(gerarDataExpiracao())
                .sign(algorithm);
    }

    public String gerarTokenCliente(Long clienteId) {

        Algorithm algorithm = Algorithm.HMAC256(secret);

        Instant agora = Instant.now();

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(clienteId.toString())
                .withClaim("tipo", "CLIENTE")
                .withIssuedAt(agora)
                .withExpiresAt(agora.plusSeconds(7200))
                .sign(algorithm);
    }

    private Instant gerarDataExpiracao() {
        return LocalDateTime.now()
                .plusHours(2)
                .toInstant(ZoneOffset.of("-03:00"));
    }
}