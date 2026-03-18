package com.loja.lojaservice.client.pagamento.dto;

public record PagamentoResponse(Long pedidoId, boolean aprovado, String protocolo, String authenticatedAs) {
}

