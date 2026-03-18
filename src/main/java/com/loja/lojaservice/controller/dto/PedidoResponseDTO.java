package com.loja.lojaservice.controller.dto;

import com.loja.lojaservice.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PedidoResponseDTO(
        Long id,
        Long clienteId,
        StatusPedido status,
        BigDecimal valorTotal,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm,
        List<PedidoItemResponseDTO> itens
) {
}

