package com.loja.lojaservice.client.pagamento;

import com.loja.lojaservice.client.pagamento.dto.PagamentoHealthResponse;
import com.loja.lojaservice.client.pagamento.dto.PagamentoPingResponse;
import com.loja.lojaservice.client.pagamento.dto.PagamentoRequest;
import com.loja.lojaservice.client.pagamento.dto.PagamentoResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api")
public interface PagamentoApiClient {

    @GetExchange("/public/ping")
    PagamentoPingResponse ping();

    @GetExchange("/payments/health")
    PagamentoHealthResponse health();

    @PostExchange("/payments")
    PagamentoResponse realizarPagamento(@RequestBody PagamentoRequest request);
}

