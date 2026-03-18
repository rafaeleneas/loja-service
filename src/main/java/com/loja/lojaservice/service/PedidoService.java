package com.loja.lojaservice.service;

import com.loja.lojaservice.entity.Cliente;
import com.loja.lojaservice.entity.ItemPedido;
import com.loja.lojaservice.entity.Pedido;
import com.loja.lojaservice.entity.Produto;
import com.loja.lojaservice.enums.StatusPedido;
import com.loja.lojaservice.exception.EntidadeNaoEncontradaException;
import com.loja.lojaservice.exception.LojaException;
import com.loja.lojaservice.messaging.SolicitacaoPagamentoEvent;
import com.loja.lojaservice.messaging.SolicitacaoPagamentoPendenteEvent;
import com.loja.lojaservice.repository.ClienteRepository;
import com.loja.lojaservice.repository.PedidoRepository;
import com.loja.lojaservice.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        Pedido pedido = findByIdOrThrow(id);
        inicializarAssociacoes(pedido);
        return pedido;
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    @Transactional
    public Pedido criar(Long clienteId, List<ItemEntrada> itensEntrada, String idempotencyKey) {
        validarCriacaoPedido(itensEntrada, idempotencyKey);
        Pedido existente = buscarPorIdempotencyKey(idempotencyKey);
        if (existente != null) {
            return existente;
        }

        Pedido pedido = inicializarPedido(clienteId, idempotencyKey);
        List<ItemPedido> itens = criarItensPedido(pedido, itensEntrada);

        pedido.setItens(itens);
        pedido.setValorTotal(calcularValorTotal(itens));
        Pedido salvo = salvarOuBuscarExistente(pedido, idempotencyKey);
        inicializarAssociacoes(salvo);
        return salvo;
    }

    @Transactional
    public ConfirmacaoPagamentoPedido solicitarPagamento(Long pedidoId) {
        Pedido pedido = findByIdOrThrow(pedidoId);
        validarSolicitacaoPagamento(pedido);

        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedidoRepository.save(pedido);

        SolicitacaoPagamentoEvent event = new SolicitacaoPagamentoEvent(
                pedido.getId(),
                pedido.getCliente().getId(),
                pedido.getValorTotal(),
                OffsetDateTime.now(),
                pedido.getIdempotencyKey()
        );
        applicationEventPublisher.publishEvent(new SolicitacaoPagamentoPendenteEvent(event));

        log.info("Solicitacao de pagamento {} no valor {} ", pedidoId, pedido.getValorTotal());

        return new ConfirmacaoPagamentoPedido(
                pedido.getId(),
                true,
                null,
                StatusPedido.AGUARDANDO_PAGAMENTO
        );
    }

    @Transactional
    public Pedido cancelar(Long pedidoId) {
        Pedido pedido = findByIdOrThrow(pedidoId);
        validarCancelamento(pedido);
        devolverEstoque(pedido);

        pedido.setStatus(StatusPedido.CANCELADO);
        Pedido salvo = pedidoRepository.save(pedido);
        inicializarAssociacoes(salvo);
        return salvo;
    }

    private Pedido findByIdOrThrow(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Pedido de id " + id + " nao encontrado."));
    }

    private void validarCriacaoPedido(List<ItemEntrada> itensEntrada, String idempotencyKey) {
        validarIdempotencyKey(idempotencyKey);

        if (itensEntrada == null || itensEntrada.isEmpty()) {
            throw new LojaException("Pedido deve possuir ao menos um item.");
        }
    }

    private void validarIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new LojaException("Header Idempotency-Key e obrigatorio.");
        }
    }

    private Pedido inicializarPedido(Long clienteId, String idempotencyKey) {
        Cliente cliente = buscarClientePorId(clienteId);

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setIdempotencyKey(idempotencyKey);
        return pedido;
    }

    private Cliente buscarClientePorId(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente de id " + clienteId + " nao encontrado."));
    }

    private Pedido buscarPorIdempotencyKey(String idempotencyKey) {
        return pedidoRepository.findByIdempotencyKey(idempotencyKey)
                .map(pedido -> {
                    inicializarAssociacoes(pedido);
                    return pedido;
                })
                .orElse(null);
    }

    private List<ItemPedido> criarItensPedido(Pedido pedido, List<ItemEntrada> itensEntrada) {
        List<ItemPedido> itens = new ArrayList<>();

        for (ItemEntrada itemEntrada : itensEntrada) {
            validarQuantidadeItem(itemEntrada);

            Produto produto = buscarProdutoAtivo(itemEntrada.produtoId());
            reservarEstoque(produto.getId(), itemEntrada.quantidade());

            itens.add(criarItemPedido(pedido, produto, itemEntrada.quantidade()));
        }

        return itens;
    }

    private void validarQuantidadeItem(ItemEntrada itemEntrada) {
        if (itemEntrada.quantidade() == null || itemEntrada.quantidade() <= 0) {
            throw new LojaException("Quantidade do item deve ser maior que zero.");
        }
    }

    private Produto buscarProdutoAtivo(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Produto de id " + produtoId + " nao encontrado."));

        if (!Boolean.TRUE.equals(produto.getAtivo())) {
            throw new LojaException("Produto " + produto.getId() + " esta inativo e nao pode compor pedido.");
        }

        return produto;
    }

    private void reservarEstoque(Long produtoId, Integer quantidade) {
        int linhasAtualizadas = produtoRepository.decrementarEstoqueSeDisponivel(produtoId, quantidade);
        if (linhasAtualizadas == 0) {
            throw new LojaException("Estoque insuficiente para o produto " + produtoId + ".");
        }
    }

    private ItemPedido criarItemPedido(Pedido pedido, Produto produto, Integer quantidade) {
        BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(quantidade));

        ItemPedido item = new ItemPedido();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(produto.getPreco());
        item.setTotalLinha(subtotal);
        return item;
    }

    private BigDecimal calcularValorTotal(List<ItemPedido> itens) {
        return itens.stream()
                .map(ItemPedido::getTotalLinha)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Pedido salvarOuBuscarExistente(Pedido pedido, String idempotencyKey) {
        try {
            return pedidoRepository.save(pedido);
        } catch (DataIntegrityViolationException ex) {
            Pedido existente = buscarPorIdempotencyKey(idempotencyKey);
            if (existente != null) {
                return existente;
            }
            throw ex;
        }
    }

    private void validarSolicitacaoPagamento(Pedido pedido) {
        if (pedido.getStatus() == StatusPedido.PAGO) {
            throw new LojaException("Pedido " + pedido.getId() + " ja foi pago.");
        }

        if (pedido.getStatus() == StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new LojaException("Pedido " + pedido.getId() + " ja esta aguardando pagamento.");
        }
    }

    private void validarCancelamento(Pedido pedido) {
        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new LojaException("Pedido " + pedido.getId() + " ja esta cancelado.");
        }

        if (pedido.getStatus() == StatusPedido.PAGO) {
            throw new LojaException("Pedido " + pedido.getId() + " esta pago e nao pode ser cancelado.");
        }
    }

    private void devolverEstoque(Pedido pedido) {
        pedido.getItens().forEach(item -> {
            int linhasAtualizadas = produtoRepository.incrementarEstoque(item.getProduto().getId(), item.getQuantidade());
            if (linhasAtualizadas == 0) {
                throw new LojaException("Falha ao devolver estoque do produto " + item.getProduto().getId() + ".");
            }
        });
    }

    private void inicializarAssociacoes(Pedido pedido) {      
        pedido.getItens().forEach(item -> {
            item.getProduto().getId();
            item.getProduto().getNome();
        });
    }

    public record ItemEntrada(Long produtoId, Integer quantidade) {
    }

    public record ConfirmacaoPagamentoPedido(
            Long pedidoId,
            boolean processando,
            String protocolo,
            StatusPedido status
    ) {
    }
}
