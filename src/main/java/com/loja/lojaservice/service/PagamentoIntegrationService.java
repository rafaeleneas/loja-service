package com.loja.lojaservice.service;

import com.loja.lojaservice.client.pagamento.PagamentoApiClient;
import com.loja.lojaservice.client.pagamento.dto.PagamentoHealthResponse;
import com.loja.lojaservice.client.pagamento.dto.PagamentoPingResponse;
import com.loja.lojaservice.client.pagamento.dto.PagamentoRequest;
import com.loja.lojaservice.client.pagamento.dto.PagamentoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.integrations.pagamento", name = "enabled", havingValue = "true")
public class PagamentoIntegrationService {

    private final PagamentoApiClient pagamentoApiClient;

    public PagamentoPingResponse ping() {
        return pagamentoApiClient.ping();
    }

    public PagamentoHealthResponse health() {
        return pagamentoApiClient.health();
    }

    public ConfirmacaoPagamento realizarPagamento(Long pedidoId, BigDecimal valorTotal) {
        PagamentoResponse response = pagamentoApiClient.realizarPagamento(new PagamentoRequest(pedidoId, valorTotal));
        return new ConfirmacaoPagamento(response.aprovado(), response.protocolo(), response.authenticatedAs());
    }

    public record ConfirmacaoPagamento(boolean aprovado, String protocolo, String authenticatedAs) {
    }
}

