package com.dinoco.oficina.auth.validation;

public class CpfValidator {

    public String validarENormalizar(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF não informado");
        }

        String cpfNormalizado = cpf.replaceAll("\\D", "");

        if (cpfNormalizado.length() != 11) {
            throw new IllegalArgumentException("CPF inválido");
        }

        if (cpfNormalizado.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CPF inválido");
        }

        if (!digitosValidos(cpfNormalizado)) {
            throw new IllegalArgumentException("CPF inválido");
        }

        return cpfNormalizado;
    }

    private boolean digitosValidos(String cpf) {
        int primeiroDigito = calcularDigito(cpf.substring(0, 9), 10);
        int segundoDigito = calcularDigito(cpf.substring(0, 10), 11);

        return primeiroDigito == Character.getNumericValue(cpf.charAt(9))
                && segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    private int calcularDigito(String base, int pesoInicial) {
        int soma = 0;

        for (int i = 0; i < base.length(); i++) {
            soma += Character.getNumericValue(base.charAt(i))
                    * (pesoInicial - i);
        }

        int resto = soma % 11;

        return resto < 2 ? 0 : 11 - resto;
    }
}