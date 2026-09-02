package com.dinoco.oficina.auth.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.dinoco.oficina.auth.exception.ClienteInativoException;
import com.dinoco.oficina.auth.exception.ClienteNaoEncontradoException;
import com.dinoco.oficina.auth.model.AuthRequest;
import com.dinoco.oficina.auth.model.AuthResponse;
import com.dinoco.oficina.auth.model.Cliente;
import com.dinoco.oficina.auth.repository.ClienteRepository;
import com.dinoco.oficina.auth.security.JwtService;
import com.dinoco.oficina.auth.security.SecretProvider;
import com.dinoco.oficina.auth.validation.CpfValidator;

public class AuthHandler
        implements RequestHandler<AuthRequest, AuthResponse> {

    private final SecretProvider secretProvider;
    private final CpfValidator cpfValidator;
    private final ClienteRepository clienteRepository;

    public AuthHandler() {
        this(new SecretProvider(), new CpfValidator(), new ClienteRepository());
    }

    AuthHandler(SecretProvider secretProvider, CpfValidator cpfValidator, ClienteRepository clienteRepository) {
        this.secretProvider = secretProvider;
        this.cpfValidator = cpfValidator;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public AuthResponse handleRequest(AuthRequest request, Context context) {

        context.getLogger().log("INFO - Iniciando autenticacao de cliente");

        String cpf = cpfValidator.validarENormalizar(request.cpf());

        context.getLogger().log("INFO - CPF validado com sucesso");

        context.getLogger().log("INFO - Consultando cliente no PostgreSQL");

        Cliente cliente = clienteRepository.buscarPorCpf(cpf)
                .orElseThrow(() -> {
                    context.getLogger().log("WARN - Cliente nao encontrado");
                    return new ClienteNaoEncontradoException();
                });

        if (!cliente.ativo()) {
            context.getLogger().log("WARN - Cliente inativo. clienteId=" + cliente.id());
            throw new ClienteInativoException();
        }

        context.getLogger().log("INFO - Cliente autenticado. clienteId=" + cliente.id());

        String secret = secretProvider.buscarJwtSecret();

        JwtService jwtService = new JwtService(secret);

        String token = jwtService.gerarTokenCliente(cliente.id());

        return new AuthResponse(token);
    }
}