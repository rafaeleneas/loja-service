package com.loja.lojaservice.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PagamentoEventPublisher {

    private final KafkaTemplate<String, SolicitacaoPagamentoEvent> kafkaTemplate;
    private final String solicitacaoPagamentoTopic;

    public PagamentoEventPublisher(
            KafkaTemplate<String, SolicitacaoPagamentoEvent> kafkaTemplate,
            @Value("${app.messaging.topics.solicitacao-pagamento}") String solicitacaoPagamentoTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.solicitacaoPagamentoTopic = solicitacaoPagamentoTopic;
    }

    public void publicarSolicitacaoPagamento(SolicitacaoPagamentoEvent event) {
        kafkaTemplate.send(solicitacaoPagamentoTopic, String.valueOf(event.pedidoId()), event);
        log.info(
                "Publicando solicitacao de pagamento no topico {}: pedidoId={}, clienteId={}, valorTotal={}, solicitadoEm={}, correlationId={}",
                solicitacaoPagamentoTopic,
                event.pedidoId(),
                event.clienteId(),
                event.valorTotal(),
                event.solicitadoEm(),
                event.correlationId()
        );
    }
}

