package com.dinoco.oficina.auth.exception;

public class ClienteInativoException extends RuntimeException {

    public ClienteInativoException() {
        super("Cliente inativo");
    }
}