package com.loja.lojaservice.messaging;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SolicitacaoPagamentoEvent(
        Long pedidoId,
        Long clienteId,
        BigDecimal valorTotal,
        OffsetDateTime solicitadoEm,
        String correlationId
) {
}

