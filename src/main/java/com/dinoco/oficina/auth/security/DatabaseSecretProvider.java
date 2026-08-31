package com.dinoco.oficina.auth.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

public class DatabaseSecretProvider {

    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper objectMapper;
    private final String secretArn;

    public DatabaseSecretProvider() {
        this(
                SecretsManagerClient.create(),
                new ObjectMapper(),
                System.getenv("DB_SECRET_ARN")
        );
    }

    DatabaseSecretProvider(
            SecretsManagerClient secretsManagerClient,
            ObjectMapper objectMapper,
            String secretArn
    ) {
        this.secretsManagerClient = secretsManagerClient;
        this.objectMapper = objectMapper;
        this.secretArn = secretArn;
    }

    public DatabaseCredentials buscarCredenciais() {
        try {
            if (secretArn == null || secretArn.isBlank()) {
                throw new IllegalStateException(
                        "Variável DB_SECRET_ARN não configurada"
                );
            }

            var request = GetSecretValueRequest.builder()
                    .secretId(secretArn)
                    .build();

            String secretString = secretsManagerClient
                    .getSecretValue(request)
                    .secretString();

            JsonNode json = objectMapper.readTree(secretString);

            return new DatabaseCredentials(
                    json.get("username").asText(),
                    json.get("password").asText()
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Erro ao obter credenciais do banco",
                    e
            );
        }
    }

    public record DatabaseCredentials(
            String username,
            String password
    ) {
    }
}