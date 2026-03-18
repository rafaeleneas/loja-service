package com.loja.lojaservice.service;

import com.loja.lojaservice.entity.Pedido;
import com.loja.lojaservice.enums.StatusPedido;
import com.loja.lojaservice.exception.EntidadeNaoEncontradaException;
import com.loja.lojaservice.messaging.SolicitacaoPagamentoEvent;
import com.loja.lojaservice.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitacaoPagamentoService {

    private final PedidoRepository pedidoRepository;

    @Transactional
    public void processar(SolicitacaoPagamentoEvent event) {
        Pedido pedido = pedidoRepository.findById(event.pedidoId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pedido de id " + event.pedidoId() + " nao encontrado."));

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            log.warn(
                    "Solicitacao de pagamento ignorada: pedidoId={}, statusAtual={}, clienteId={}, correlationId={}",
                    pedido.getId(),
                    pedido.getStatus(),
                    event.clienteId(),
                    event.correlationId()
            );
            return;
        }

        pedido.setStatus(StatusPedido.PAGO);
        pedidoRepository.save(pedido);

        log.info(
                "Solicitacao de pagamento processada: pedidoId={}, clienteId={}, valorTotal={}, correlationId={}, status={}",
                event.pedidoId(),
                event.clienteId(),
                event.valorTotal(),
                event.correlationId(),
                pedido.getStatus()
        );
    }
}

