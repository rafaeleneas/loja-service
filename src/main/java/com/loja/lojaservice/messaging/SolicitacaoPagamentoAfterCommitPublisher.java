package com.loja.lojaservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitacaoPagamentoAfterCommitPublisher {

    private final PagamentoEventPublisher pagamentoEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicar(SolicitacaoPagamentoPendenteEvent event) {
        SolicitacaoPagamentoEvent solicitacao = event.solicitacaoPagamentoEvent();
        pagamentoEventPublisher.publicarSolicitacaoPagamento(solicitacao);
        log.info(
                "Solicitacao de pagamento publicada apos commit: pedidoId={}, correlationId={}",
                solicitacao.pedidoId(),
                solicitacao.correlationId()
        );
    }
}

