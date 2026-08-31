package com.dinoco.oficina.auth.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.dinoco.oficina.auth.model.AuthRequest;
import com.dinoco.oficina.auth.model.Cliente;
import com.dinoco.oficina.auth.repository.ClienteRepository;
import com.dinoco.oficina.auth.security.SecretProvider;
import com.dinoco.oficina.auth.validation.CpfValidator;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthHandlerTest {

    private static final String SECRET =
            "minha-chave-secreta-de-teste-1234567890";

    @Test
    void deveGerarTokenParaClienteAtivo() {

        SecretProvider secretProvider =
                mock(SecretProvider.class);

        CpfValidator cpfValidator =
                mock(CpfValidator.class);

        ClienteRepository clienteRepository =
                mock(ClienteRepository.class);

        Context context = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);

        when(context.getLogger()).thenReturn(logger);

        when(cpfValidator.validarENormalizar(anyString()))
                .thenReturn("52998224725");

        when(clienteRepository.buscarPorCpf("52998224725"))
                .thenReturn(
                        Optional.of(
                                new Cliente(
                                        42L,
                                        "52998224725",
                                        "Cliente Teste",
                                        true
                                )
                        )
                );

        when(secretProvider.buscarJwtSecret())
                .thenReturn(SECRET);

        AuthHandler handler =
                new AuthHandler(
                        secretProvider,
                        cpfValidator,
                        clienteRepository
                );

        var response = handler.handleRequest(
                new AuthRequest("52998224725"),
                context
        );

        var decoded = JWT
                .require(Algorithm.HMAC256(SECRET))
                .withIssuer("oficina-api")
                .build()
                .verify(response.token());

        assertEquals("42", decoded.getSubject());

        assertEquals(
                "CLIENTE",
                decoded.getClaim("tipo").asString()
        );
    }
}