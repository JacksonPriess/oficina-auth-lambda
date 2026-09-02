package com.dinoco.oficina.auth.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidatorTest {

    private final CpfValidator validator = new CpfValidator();

    @Test
    void deveAceitarCpfValidoSemFormatacao() {
        String cpf = validator.validarENormalizar("52998224725");

        assertEquals("52998224725", cpf);
    }

    @Test
    void deveAceitarCpfValidoFormatado() {
        String cpf = validator.validarENormalizar("529.982.247-25");

        assertEquals("52998224725", cpf);
    }

    @Test
    void deveRejeitarCpfInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarENormalizar("12345678901")
        );
    }

    @Test
    void deveRejeitarCpfComTodosDigitosIguais() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarENormalizar("11111111111")
        );
    }

    @Test
    void deveRejeitarCpfNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validarENormalizar(null)
        );
    }
}