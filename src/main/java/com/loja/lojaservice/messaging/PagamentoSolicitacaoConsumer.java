package com.loja.lojaservice.messaging;

import com.loja.lojaservice.service.SolicitacaoPagamentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoSolicitacaoConsumer {

    private final SolicitacaoPagamentoService solicitacaoPagamentoService;

    @Transactional
    @KafkaListener(topics = "${app.messaging.topics.solicitacao-pagamento}")
    public void consumir(SolicitacaoPagamentoEvent event) {
        log.info(
                "Consumo de solicitacao de pagamento: pedidoId={}, clienteId={}, valorTotal={}, solicitadoEm={}, correlationId={}",
                event.pedidoId(),
                event.clienteId(),
                event.valorTotal(),
                event.solicitadoEm(),
                event.correlationId()
        );

        solicitacaoPagamentoService.processar(event);
    }
}

