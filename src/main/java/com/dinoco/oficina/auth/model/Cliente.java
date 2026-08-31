package com.dinoco.oficina.auth.model;

public record Cliente(
        Long id,
        String documento,
        String nome,
        boolean ativo
) {
}