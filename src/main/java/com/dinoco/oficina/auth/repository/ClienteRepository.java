package com.dinoco.oficina.auth.repository;

import com.dinoco.oficina.auth.model.Cliente;
import com.dinoco.oficina.auth.security.DatabaseSecretProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class ClienteRepository {

    private static final String SQL = """
            SELECT id,
                   documento,
                   nome,
                   ativo
            FROM cliente
            WHERE documento = ?
              AND tipo_pessoa = 'F'
            """;

    private final DatabaseSecretProvider databaseSecretProvider;
    private final String dbHost;
    private final String dbPort;
    private final String dbName;

    public ClienteRepository() {
        this(
                new DatabaseSecretProvider(),
                System.getenv("DB_HOST"),
                System.getenv("DB_PORT"),
                System.getenv("DB_NAME")
        );
    }

    ClienteRepository(DatabaseSecretProvider databaseSecretProvider, String dbHost, String dbPort, String dbName) {
        this.databaseSecretProvider = databaseSecretProvider;
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbName = dbName;
    }

    public Optional<Cliente> buscarPorCpf(String cpf) {

        validarConfiguracao();

        var credentials =
                databaseSecretProvider.buscarCredenciais();

        String jdbcUrl = String.format(
                "jdbc:postgresql://%s:%s/%s",
                dbHost,
                dbPort,
                dbName
        );

        try (
                Connection connection = DriverManager.getConnection(
                        jdbcUrl,
                        credentials.username(),
                        credentials.password()
                );

                PreparedStatement statement =
                        connection.prepareStatement(SQL)
        ) {

            statement.setString(1, cpf);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        new Cliente(
                                resultSet.getLong("id"),
                                resultSet.getString("documento"),
                                resultSet.getString("nome"),
                                resultSet.getBoolean("ativo")
                        )
                );
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Erro ao consultar cliente no banco",
                    e
            );
        }
    }

    private void validarConfiguracao() {
        if (dbHost == null || dbHost.isBlank()
                || dbPort == null || dbPort.isBlank()
                || dbName == null || dbName.isBlank()) {


            throw new IllegalStateException(
                    "Configuração do banco incompleta"
            );
        }
    }
}