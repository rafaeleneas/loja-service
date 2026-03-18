package com.loja.lojaservice.controller;

import com.loja.lojaservice.controller.dto.ConfirmacaoPagamentoResponseDTO;
import com.loja.lojaservice.controller.dto.PedidoItemResponseDTO;
import com.loja.lojaservice.controller.dto.PedidoRequestDTO;
import com.loja.lojaservice.controller.dto.PedidoResponseDTO;
import com.loja.lojaservice.entity.ItemPedido;
import com.loja.lojaservice.entity.Pedido;
import com.loja.lojaservice.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponseDTO criar(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PedidoRequestDTO request
    ) {
        List<PedidoService.ItemEntrada> itens = request.itens()
                .stream()
                .map(item -> new PedidoService.ItemEntrada(item.produtoId(), item.quantidade()))
                .toList();

        Pedido pedido = pedidoService.criar(request.clienteId(), itens, idempotencyKey);
        return toResponse(pedido);
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO buscarPorId(@PathVariable Long id) {
        Pedido pedido = pedidoService.buscarPorId(id);
        return toResponse(pedido);
    }

    @PostMapping("/{id}/solicitar-pagamento")
    public ConfirmacaoPagamentoResponseDTO solicitarPagamento(@PathVariable Long id) {
        PedidoService.ConfirmacaoPagamentoPedido confirmacao = pedidoService.solicitarPagamento(id);
        return new ConfirmacaoPagamentoResponseDTO(
                confirmacao.pedidoId(),
                confirmacao.processando(),
                confirmacao.protocolo(),
                confirmacao.status()
        );
    }

    @PostMapping("/{id}/cancelar")
    public PedidoResponseDTO cancelar(@PathVariable Long id) {
        Pedido pedido = pedidoService.cancelar(id);
        return toResponse(pedido);
    }

    private PedidoResponseDTO toResponse(Pedido pedido) {
        List<PedidoItemResponseDTO> itens = pedido.getItens()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getCliente().getId(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getCriadoEm(),
                pedido.getAtualizadoEm(),
                itens
        );
    }

    private PedidoItemResponseDTO toItemResponse(ItemPedido item) {
        return new PedidoItemResponseDTO(
                item.getId(),
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getTotalLinha()
        );
    }
}

