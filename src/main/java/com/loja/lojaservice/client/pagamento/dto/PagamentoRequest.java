package com.loja.lojaservice.client.pagamento.dto;

import java.math.BigDecimal;

public record PagamentoRequest(Long pedidoId, BigDecimal valorTotal) {
}

