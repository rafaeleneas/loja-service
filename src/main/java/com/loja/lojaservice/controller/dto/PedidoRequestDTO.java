package com.loja.lojaservice.controller.dto;

import java.util.List;

public record PedidoRequestDTO(
        Long clienteId,
        List<PedidoItemRequestDTO> itens
) {
}

