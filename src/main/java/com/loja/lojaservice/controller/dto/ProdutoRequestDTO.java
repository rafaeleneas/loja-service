package com.loja.lojaservice.controller.dto;

import java.math.BigDecimal;

public record ProdutoRequestDTO(
        String nome,
        String descricao,
        BigDecimal preco,
        Integer estoque,
        Boolean ativo
) {
}

