package com.loja.lojaservice.controller.dto;

public record PedidoItemRequestDTO(
        Long produtoId,
        String idempotencykey,
        Integer quantidade
) {
}

