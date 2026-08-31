package com.dinoco.oficina.auth.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.dinoco.oficina.auth.exception.ClienteInativoException;
import com.dinoco.oficina.auth.exception.ClienteNaoEncontradoException;
import com.dinoco.oficina.auth.model.AuthRequest;
import com.dinoco.oficina.auth.model.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class ApiGatewayAuthHandler
        implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final ObjectMapper objectMapper;
    private final AuthHandler authHandler;

    public ApiGatewayAuthHandler() {
        this.objectMapper = new ObjectMapper();
        this.authHandler = new AuthHandler();
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {

        try {
            AuthRequest request = objectMapper.readValue(event.getBody(), AuthRequest.class);

            AuthResponse response = authHandler.handleRequest(request, context);

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withHeaders(Map.of(
                            "Content-Type", "application/json"
                    ))
                    .withBody(objectMapper.writeValueAsString(response))
                    .build();

        } catch (IllegalArgumentException e) {
            return response(400, "CPF inválido");
        } catch (ClienteNaoEncontradoException e) {
            return response(401, "Cliente não encontrado");
        } catch (ClienteInativoException e) {
            return response(403, "Cliente inativo");
        } catch (Exception e) {
            context.getLogger().log("Erro interno durante autenticação: " + e.getMessage());
            return response(500, "Erro interno durante autenticação");
        }
    }

    private APIGatewayV2HTTPResponse response(int statusCode, String message
    ) {

        try {
            String body = objectMapper.writeValueAsString(Map.of("message", message));

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(statusCode)
                    .withHeaders(
                            Map.of(
                                    "Content-Type",
                                    "application/json"
                            )
                    )
                    .withBody(body)
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}