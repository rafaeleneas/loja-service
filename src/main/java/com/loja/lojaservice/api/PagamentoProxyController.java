package com.loja.lojaservice.api;

import com.loja.lojaservice.client.pagamento.dto.PagamentoHealthResponse;
import com.loja.lojaservice.client.pagamento.dto.PagamentoPingResponse;
import com.loja.lojaservice.service.PagamentoIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pagamentos")
@RequiredArgsConstructor
public class PagamentoProxyController {

    private final PagamentoIntegrationService pagamentoIntegrationService;

    @GetMapping("/public/ping")
    public PagamentoPingResponse ping() {
        return pagamentoIntegrationService.ping();
    }

    @GetMapping("/health")
    public PagamentoHealthResponse health() {
        return pagamentoIntegrationService.health();
    }
}

