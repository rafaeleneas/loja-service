package com.loja.lojaservice.controller.validator;

import com.loja.lojaservice.controller.dto.ProdutoRequestDTO;
import com.loja.lojaservice.exception.LojaException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProdutoValidatorTest {

    private final ProdutoValidator validator = new ProdutoValidator();

    @ParameterizedTest
    @MethodSource("produtoRequestDTOInvalidoProvider")
    void validate_deveLancarExcecao_quandoProdutoRequestDTOInvalido(ProdutoRequestDTO dto) {
        assertThrows(LojaException.class, () -> validator.validateCriacaoOuAtualizacao(dto));
    }

    @Test
    void validate_naoDeveLancarExcecao_quandoProdutoRequestDTOValido() {
        ProdutoRequestDTO dto = new ProdutoRequestDTO("Racao", "desc", BigDecimal.ZERO, 0, true);

        assertDoesNotThrow(() -> validator.validateCriacaoOuAtualizacao(dto));
    }

    private static Stream<Arguments> produtoRequestDTOInvalidoProvider() {
        return Stream.of(
                Arguments.of((ProdutoRequestDTO) null),
                Arguments.of(new ProdutoRequestDTO(" ", "desc", new BigDecimal("10.00"), 1, true)),
                Arguments.of(new ProdutoRequestDTO("Racao", "desc", null, 1, true)),
                Arguments.of(new ProdutoRequestDTO("Racao", "desc", new BigDecimal("-1.00"), 1, true)),
                Arguments.of(new ProdutoRequestDTO("Racao", "desc", new BigDecimal("10.00"), null, true)),
                Arguments.of(new ProdutoRequestDTO("Racao", "desc", new BigDecimal("10.00"), -1, true)),
                Arguments.of(new ProdutoRequestDTO("Racao", "desc", new BigDecimal("10.00"), 1, null))
        );
    }
}

