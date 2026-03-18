package com.loja.lojaservice.controller.dto;

import com.loja.lojaservice.enums.StatusPedido;

public record ConfirmacaoPagamentoResponseDTO(
        Long pedidoId,
        boolean processando,
        String protocolo,
        StatusPedido status
) {
}

