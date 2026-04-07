package com.loja.lojaservice.service;

import com.loja.lojaservice.entity.Cliente;
import com.loja.lojaservice.entity.ItemPedido;
import com.loja.lojaservice.entity.Pedido;
import com.loja.lojaservice.entity.Produto;
import com.loja.lojaservice.enums.StatusPedido;
import com.loja.lojaservice.exception.EntidadeNaoEncontradaException;
import com.loja.lojaservice.exception.LojaException;
import com.loja.lojaservice.messaging.SolicitacaoPagamentoPendenteEvent;
import com.loja.lojaservice.repository.ClienteRepository;
import com.loja.lojaservice.repository.PedidoRepository;
import com.loja.lojaservice.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void listarTodos_deveRetornarListaDePedidos() {
        Pedido pedido1 = novoPedidoComItem(1L, 1L, 10L, "Produto 1", new BigDecimal("10.00"), 2);
        Pedido pedido2 = novoPedidoComItem(2L, 2L, 11L, "Produto 2", new BigDecimal("15.00"), 1);
        when(pedidoRepository.findAll()).thenReturn(List.of(pedido1, pedido2));

        List<Pedido> resultado = pedidoService.listarTodos();

        assertEquals(2, resultado.size());
        assertSame(pedido1, resultado.get(0));
        assertSame(pedido2, resultado.get(1));
        verify(pedidoRepository).findAll();
    }

    @Test
    void buscarPorId_deveRetornarPedidoInicializado_quandoEncontrado() {
        Pedido pedido = novoPedidoComItem(1L, 1L, 20L, "Mouse", new BigDecimal("50.00"), 1);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        Pedido resultado = pedidoService.buscarPorId(1L);

        assertSame(pedido, resultado);
        assertEquals("Mouse", resultado.getItens().get(0).getProduto().getNome());
        verify(pedidoRepository).findById(1L);
    }

    @Test
    void buscarPorId_deveLancarExcecao_quandoNaoEncontrado() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> pedidoService.buscarPorId(99L));
    }

    @Test
    void listarPorCliente_deveRetornarPedidosDoCliente() {
        Pedido pedido = novoPedidoComItem(1L, 10L, 30L, "Teclado", new BigDecimal("70.00"), 1);
        when(pedidoRepository.findByClienteId(10L)).thenReturn(List.of(pedido));

        List<Pedido> resultado = pedidoService.listarPorCliente(10L);

        assertEquals(1, resultado.size());
        assertSame(pedido, resultado.get(0));
        verify(pedidoRepository).findByClienteId(10L);
    }

    @Test
    void criar_deveLancarExcecao_quandoIdempotencyKeyForNula() {
        LojaException ex = assertThrows(LojaException.class,
                () -> pedidoService.criar(1L, List.of(itemEntrada(1L, 1)), null));

        assertEquals("Header Idempotency-Key e obrigatorio.", ex.getMessage());
    }

    @Test
    void criar_deveLancarExcecao_quandoIdempotencyKeyForBlank() {
        LojaException ex = assertThrows(LojaException.class,
                () -> pedidoService.criar(1L, List.of(itemEntrada(1L, 1)), "   "));

        assertEquals("Header Idempotency-Key e obrigatorio.", ex.getMessage());
    }

    @Test
    void criar_deveLancarExcecao_quandoListaItensForNula() {
        LojaException ex = assertThrows(LojaException.class,
                () -> pedidoService.criar(1L, null, "idem-1"));

        assertEquals("Pedido deve possuir ao menos um item.", ex.getMessage());
    }

    @Test
    void criar_deveLancarExcecao_quandoListaItensForVazia() {
        LojaException ex = assertThrows(LojaException.class,
                () -> pedidoService.criar(1L, List.of(), "idem-1"));

        assertEquals("Pedido deve possuir ao menos um item.", ex.getMessage());
    }

    @Test
    void criar_deveRetornarPedidoExistente_quandoIdempotencyKeyJaExistir() {
        Pedido existente = novoPedidoComItem(5L, 1L, 60L, "Cadeira", new BigDecimal("100.00"), 1);
        when(pedidoRepository.findByIdempotencyKey("idem-ja-existe")).thenReturn(Optional.of(existente));

        Pedido resultado = pedidoService.criar(1L, List.of(itemEntrada(60L, 1)), "idem-ja-existe");

        assertSame(existente, resultado);
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(clienteRepository, never()).findById(any());
    }

    @Test
    void criar_deveLancarExcecao_quandoClienteNaoEncontrado() {
        when(pedidoRepository.findByIdempotencyKey("idem-cliente")).thenReturn(Optional.empty());
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> pedidoService.criar(99L, List.of(itemEntrada(1L, 1)), "idem-cliente"));
    }

    @Test
    void criar_deveLancarExcecao_quandoQuantidadeNula() {
        when(pedidoRepository.findByIdempotencyKey("idem-qtd-nula")).thenReturn(Optional.empty());
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(novoCliente(1L)));

        LojaException ex = assertThrows(LojaException.class,
                () -> pedidoService.criar(1L, List.of(itemEntrada(1L, null)), "idem-qtd-nula"));

        assertEquals("Quantidade do item deve ser maior que zero.", ex.getMessage());
    }

    @Test
    void criar_deveLancarExcecao_quandoQuantidadeMenorOuIgualAZero() {
        when(pedidoRepository.findByIdempotencyKey("idem-qtd-zero")).thenReturn(Optional.empty());
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(novoCliente(1L)));

        LojaException ex = assertThrows(LojaException.class,
                () -> pedidoService.criar(1L, List.of(itemEntrada(1L, 0)), "idem-qtd-zero"));

        assertEquals("Quantidade do item deve ser maior que zero.", ex.getMessage());
    }

    @Test
    void criar_deveLancarExcecao_quandoProdutoNaoEncontrado() {
        when(pedidoRepository.findByIdempotencyKey("idem-produto-nao-encontrado")).thenReturn(Optional.empty());
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(novoCliente(1L)));
        when(produtoRepository.findById(555L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> pedidoService.criar(1L, List.of(itemEntrada(555L, 2)), "idem-produto-nao-encontrado"));
    }

    @Test
    void criar_deveLancarExcecao_quandoProdutoInativo() {
        Produto produtoInativo = novoProduto(777L, "Monitor", new BigDecimal("900.00"), false);
        when(pedidoRepository.findByIdempotencyKey("idem-produto-inativo")).thenReturn(Optional.empty());
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(novoCliente(1L)));
        when(produtoRepository.findById(777L)).thenReturn(Optional.of(produtoInativo));

        LojaException ex = assertThrows(LojaException.class,
                () -> pedidoService.criar(1L, List.of(itemEntrada(777L, 1)), "idem-produto-inativo"));

        assertEquals("Produto 777 esta inativo e nao pode compor pedido.", ex.getMessage());
    }

    @Test
    void criar_deveLancarExcecao_quandoEstoqueInsuficiente() {
        Produto produtoAtivo = novoProduto(888L, "Notebook", new BigDecimal("3500.00"), true);
        when(pedidoRepository.findByIdempotencyKey("idem-sem-estoque")).thenReturn(Optional.empty());
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(novoCliente(1L)));
        when(produtoRepository.findById(888L)).thenReturn(Optional.of(produtoAtivo));
        when(produtoRepository.decrementarEstoqueSeDisponivel(eq(888L), eq(1), any())).thenReturn(0);

        LojaException ex = assertThrows(LojaException.class,
                () -> pedidoService.criar(1L, List.of(itemEntrada(888L, 1)), "idem-sem-estoque"));

        assertEquals("Estoque insuficiente para o produto 888.", ex.getMessage());
    }

    @Test
    void criar_deveCriarPedido_quandoDadosValidos() {
        Produto produto1 = novoProduto(10L, "Fone", new BigDecimal("100.00"), true);
        Produto produto2 = novoProduto(11L, "Webcam", new BigDecimal("150.00"), true);
        Cliente cliente = novoCliente(1L);

        when(pedidoRepository.findByIdempotencyKey("idem-ok")).thenReturn(Optional.empty());
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto1));
        when(produtoRepository.findById(11L)).thenReturn(Optional.of(produto2));
        when(produtoRepository.decrementarEstoqueSeDisponivel(eq(10L), eq(2), any())).thenReturn(1);
        when(produtoRepository.decrementarEstoqueSeDisponivel(eq(11L), eq(1), any())).thenReturn(1);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido salvo = invocation.getArgument(0);
            salvo.setId(99L);
            return salvo;
        });

        Pedido resultado = pedidoService.criar(
                1L,
                List.of(itemEntrada(10L, 2), itemEntrada(11L, 1)),
                "idem-ok"
        );

        assertNotNull(resultado);
        assertEquals(99L, resultado.getId());
        assertEquals(cliente.getId(), resultado.getCliente().getId());
        assertEquals(2, resultado.getItens().size());
        assertEquals(new BigDecimal("350.00"), resultado.getValorTotal());

        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(pedidoCaptor.capture());
        assertEquals("idem-ok", pedidoCaptor.getValue().getIdempotencyKey());
    }

    @Test
    void criar_deveRetornarPedidoExistente_quandoSaveLancarDataIntegrityEIdempotenciaEncontrar() {
        Produto produto = novoProduto(70L, "Headset", new BigDecimal("200.00"), true);
        Cliente cliente = novoCliente(1L);
        Pedido existente = novoPedidoComItem(200L, 1L, 70L, "Headset", new BigDecimal("200.00"), 1);

        when(pedidoRepository.findByIdempotencyKey("idem-race")).thenReturn(Optional.empty(), Optional.of(existente));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(70L)).thenReturn(Optional.of(produto));
        when(produtoRepository.decrementarEstoqueSeDisponivel(eq(70L), eq(1), any())).thenReturn(1);
        when(pedidoRepository.save(any(Pedido.class))).thenThrow(new DataIntegrityViolationException("duplicado"));

        Pedido resultado = pedidoService.criar(1L, List.of(itemEntrada(70L, 1)), "idem-race");

        assertSame(existente, resultado);
    }

    @Test
    void criar_devePropagarDataIntegrity_quandoSaveLancarEIdempotenciaNaoEncontrarPedido() {
        Produto produto = novoProduto(71L, "Microfone", new BigDecimal("300.00"), true);
        Cliente cliente = novoCliente(1L);

        when(pedidoRepository.findByIdempotencyKey("idem-race-sem-registro")).thenReturn(Optional.empty(), Optional.empty());
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(71L)).thenReturn(Optional.of(produto));
        when(produtoRepository.decrementarEstoqueSeDisponivel(eq(71L), eq(1), any())).thenReturn(1);
        when(pedidoRepository.save(any(Pedido.class))).thenThrow(new DataIntegrityViolationException("duplicado"));

        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class,
                () -> pedidoService.criar(1L, List.of(itemEntrada(71L, 1)), "idem-race-sem-registro"));

        assertTrue(ex.getMessage().contains("duplicado"));
    }

    @Test
    void solicitarPagamento_deveLancarExcecao_quandoPedidoNaoEncontrado() {
        when(pedidoRepository.findById(700L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> pedidoService.solicitarPagamento(700L));
    }

    @Test
    void solicitarPagamento_deveLancarExcecao_quandoPedidoJaPago() {
        Pedido pedido = novoPedidoComItem(701L, 1L, 10L, "Produto", new BigDecimal("10.00"), 1);
        pedido.setStatus(StatusPedido.PAGO);
        when(pedidoRepository.findById(701L)).thenReturn(Optional.of(pedido));

        LojaException ex = assertThrows(LojaException.class, () -> pedidoService.solicitarPagamento(701L));

        assertEquals("Pedido 701 ja foi pago.", ex.getMessage());
    }

    @Test
    void solicitarPagamento_deveLancarExcecao_quandoPedidoJaAguardandoPagamento() {
        Pedido pedido = novoPedidoComItem(702L, 1L, 10L, "Produto", new BigDecimal("10.00"), 1);
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        when(pedidoRepository.findById(702L)).thenReturn(Optional.of(pedido));

        LojaException ex = assertThrows(LojaException.class, () -> pedidoService.solicitarPagamento(702L));

        assertEquals("Pedido 702 ja esta aguardando pagamento.", ex.getMessage());
    }

    @Test
    void solicitarPagamento_devePublicarEventoERetornarConfirmacao() {
        Pedido pedido = novoPedidoComItem(703L, 1L, 10L, "Produto", new BigDecimal("10.00"), 1);
        pedido.setStatus(StatusPedido.CRIADO);
        when(pedidoRepository.findById(703L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        PedidoService.ConfirmacaoPagamentoPedido resultado = pedidoService.solicitarPagamento(703L);

        assertNotNull(resultado);
        assertEquals(703L, resultado.pedidoId());
        assertTrue(resultado.processando());
        assertEquals(StatusPedido.AGUARDANDO_PAGAMENTO, resultado.status());
        verify(applicationEventPublisher, times(1)).publishEvent(any(SolicitacaoPagamentoPendenteEvent.class));
    }

    @Test
    void cancelar_deveLancarExcecao_quandoPedidoNaoEncontrado() {
        when(pedidoRepository.findById(800L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> pedidoService.cancelar(800L));
    }

    @Test
    void cancelar_deveLancarExcecao_quandoPedidoJaCancelado() {
        Pedido pedido = novoPedidoComItem(801L, 1L, 10L, "Produto", new BigDecimal("10.00"), 1);
        pedido.setStatus(StatusPedido.CANCELADO);
        when(pedidoRepository.findById(801L)).thenReturn(Optional.of(pedido));

        LojaException ex = assertThrows(LojaException.class, () -> pedidoService.cancelar(801L));

        assertEquals("Pedido 801 ja esta cancelado.", ex.getMessage());
    }

    @Test
    void cancelar_deveLancarExcecao_quandoPedidoPago() {
        Pedido pedido = novoPedidoComItem(802L, 1L, 10L, "Produto", new BigDecimal("10.00"), 1);
        pedido.setStatus(StatusPedido.PAGO);
        when(pedidoRepository.findById(802L)).thenReturn(Optional.of(pedido));

        LojaException ex = assertThrows(LojaException.class, () -> pedidoService.cancelar(802L));

        assertEquals("Pedido 802 esta pago e nao pode ser cancelado.", ex.getMessage());
    }

    @Test
    void cancelar_deveLancarExcecao_quandoFalharAoDevolverEstoque() {
        Pedido pedido = novoPedidoComItem(803L, 1L, 10L, "Produto", new BigDecimal("10.00"), 1);
        pedido.setStatus(StatusPedido.CRIADO);
        when(pedidoRepository.findById(803L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.incrementarEstoque(eq(10L), eq(1), any())).thenReturn(0);

        LojaException ex = assertThrows(LojaException.class, () -> pedidoService.cancelar(803L));

        assertEquals("Falha ao devolver estoque do produto 10.", ex.getMessage());
    }

    @Test
    void cancelar_deveCancelarPedido_quandoDadosValidos() {
        Pedido pedido = novoPedidoComItem(804L, 1L, 10L, "Produto", new BigDecimal("10.00"), 1);
        pedido.setStatus(StatusPedido.CRIADO);

        when(pedidoRepository.findById(804L)).thenReturn(Optional.of(pedido));
        when(produtoRepository.incrementarEstoque(eq(10L), eq(1), any())).thenReturn(1);
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        Pedido resultado = pedidoService.cancelar(804L);

        assertNotNull(resultado);
        assertEquals(StatusPedido.CANCELADO, resultado.getStatus());
        verify(pedidoRepository).save(pedido);
    }

    private static PedidoService.ItemEntrada itemEntrada(Long produtoId, Integer quantidade) {
        return new PedidoService.ItemEntrada(produtoId, quantidade);
    }

    private static Cliente novoCliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNomeCompleto("Cliente Teste");
        cliente.setEmail("cliente@teste.com");
        return cliente;
    }

    private static Produto novoProduto(Long id, String nome, BigDecimal preco, boolean ativo) {
        Produto produto = new Produto();
        produto.setId(id);
        produto.setNome(nome);
        produto.setPreco(preco);
        produto.setAtivo(ativo);
        return produto;
    }

    private static Pedido novoPedidoComItem(
            Long pedidoId,
            Long clienteId,
            Long produtoId,
            String nomeProduto,
            BigDecimal preco,
            int quantidade
    ) {
        Cliente cliente = novoCliente(clienteId);
        Produto produto = novoProduto(produtoId, nomeProduto, preco, true);

        ItemPedido item = new ItemPedido();
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(preco);
        item.setTotalLinha(preco.multiply(BigDecimal.valueOf(quantidade)));

        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setCliente(cliente);
        pedido.setIdempotencyKey("idem-" + pedidoId);
        item.setPedido(pedido);
        pedido.setItens(List.of(item));
        pedido.setValorTotal(item.getTotalLinha());
        return pedido;
    }
}
