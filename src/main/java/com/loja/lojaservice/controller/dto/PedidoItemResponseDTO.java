package com.loja.lojaservice.controller.dto;

import java.math.BigDecimal;

public record PedidoItemResponseDTO(
        Long itemId,
        Long produtoId,
        String nomeProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal totalLinha
) {
}

