package com.dinoco.oficina.auth.security;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

public class SecretProvider {

    private final SecretsManagerClient secretsManagerClient;
    private final String secretArn;

    public SecretProvider() {
        this(
                SecretsManagerClient.create(),
                System.getenv("JWT_SECRET_ARN")
        );
    }

    SecretProvider(
            SecretsManagerClient secretsManagerClient,
            String secretArn
    ) {
        this.secretsManagerClient = secretsManagerClient;
        this.secretArn = secretArn;
    }

    public String buscarJwtSecret() {

        if (secretArn == null || secretArn.isBlank()) {
            throw new IllegalStateException(
                    "Variável JWT_SECRET_ARN não configurada"
            );
        }

        var request = GetSecretValueRequest.builder()
                .secretId(secretArn)
                .build();

        var response = secretsManagerClient.getSecretValue(request);

        return response.secretString();
    }
}