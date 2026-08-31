package com.dinoco.oficina.auth.security;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecretProviderTest {

    @Test
    void deveBuscarJwtSecretNoSecretsManager() {

        SecretsManagerClient client =
                mock(SecretsManagerClient.class);

        String secretArn =
                "arn:aws:secretsmanager:us-east-1:123456789:secret:teste";

        when(
                client.getSecretValue(
                        any(GetSecretValueRequest.class)
                )
        ).thenReturn(
                GetSecretValueResponse.builder()
                        .secretString("segredo-jwt-teste")
                        .build()
        );

        SecretProvider provider =
                new SecretProvider(
                        client,
                        secretArn
                );

        String secret =
                provider.buscarJwtSecret();

        assertEquals(
                "segredo-jwt-teste",
                secret
        );

        verify(
                client,
                times(1)
        ).getSecretValue(
                any(GetSecretValueRequest.class)
        );
    }

    @Test
    void deveFalharQuandoSecretArnNaoEstiverConfigurado() {

        SecretsManagerClient client =
                mock(SecretsManagerClient.class);

        SecretProvider provider =
                new SecretProvider(
                        client,
                        null
                );

        var exception =
                assertThrows(
                        IllegalStateException.class,
                        provider::buscarJwtSecret
                );

        assertEquals(
                "Variável JWT_SECRET_ARN não configurada",
                exception.getMessage()
        );

        verifyNoInteractions(client);
    }
}