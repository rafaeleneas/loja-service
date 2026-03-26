package com.loja.lojaservice.service;

import com.loja.lojaservice.controller.dto.ProdutoRequestDTO;
import com.loja.lojaservice.controller.dto.ProdutoResponseDTO;
import com.loja.lojaservice.controller.mapper.ProdutoMapper;
import com.loja.lojaservice.controller.validator.ProdutoValidator;
import com.loja.lojaservice.entity.Produto;
import com.loja.lojaservice.exception.EntidadeNaoEncontradaException;
import com.loja.lojaservice.exception.LojaException;
import com.loja.lojaservice.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private ProdutoMapper produtoMapper;
    @Mock
    private ProdutoValidator produtoValidator;
    @Mock
    private ItemPedidoService itemPedidoService;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    void listarTodos_deveRetornarListaDeProdutos_quandoExistiremProdutos() {
        Produto produto1 = novoProdutoExistente(true);
        produto1.setId(1L);
        Produto produto2 = novoProdutoExistente(true);
        produto2.setId(2L);

        ProdutoResponseDTO response1 = novaResponse(1L, "Racao 1");
        ProdutoResponseDTO response2 = novaResponse(2L, "Racao 2");

        when(produtoRepository.findAll()).thenReturn(List.of(produto1, produto2));
        when(produtoMapper.toResponse(produto1)).thenReturn(response1);
        when(produtoMapper.toResponse(produto2)).thenReturn(response2);

        //litar Todos os produtos
        List<ProdutoResponseDTO> resultado = produtoService.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals("Racao 1", resultado.get(0).nome());
        assertEquals("Racao 2", resultado.get(1).nome());
    }

    @Test
    void buscarPorId_deveRetornarProduto_quandoProdutoExistir() {
        Produto produto = novoProdutoExistente(true);
        ProdutoResponseDTO response = novaResponse(1L, "Racao");

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoMapper.toResponse(produto)).thenReturn(response);

        ProdutoResponseDTO resultado = produtoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Racao", resultado.nome());
    }

    @Test
    void buscarPorId_deveLancarExcecao_quandoProdutoNaoExistir() {
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> produtoService.buscarPorId(999L));
    }

    @Test
    void criar_deveCriarProduto_quandoDadosForemValidos() {
        ProdutoRequestDTO request = new ProdutoRequestDTO(
                "Racao Nova",
                "Pacote 10kg",
                new BigDecimal("159.90"),
                15,
                true
        );
        Produto entity = novoProdutoExistente(true);
        ProdutoResponseDTO response = novaResponse(1L, "Racao Nova");

        doNothing().when(produtoValidator).validateCriacaoOuAtualizacao(request);
        when(produtoMapper.toEntity(request)).thenReturn(entity);
        when(produtoRepository.save(entity)).thenReturn(entity);
        when(produtoMapper.toResponse(entity)).thenReturn(response);

        ProdutoResponseDTO resultado = produtoService.criar(request);

        assertNotNull(resultado);
        assertEquals("Racao Nova", resultado.nome());
        verify(produtoValidator).validateCriacaoOuAtualizacao(request);
        verify(produtoRepository).save(entity);
    }

    @ParameterizedTest
    @MethodSource("atualizarCenariosInvalidosProvider")
    void atualizar_deveLancarExcecao_quandoDadosForemInvalidos(
            Long id,
            Optional<Produto> produtoEncontrado,
            ProdutoRequestDTO request,
            Consumer<ProdutoServiceTest> setupAdicional,
            Class<? extends Exception> tipoExcecao
    ) {
        doNothing().when(produtoValidator).validateCriacaoOuAtualizacao(request);
        when(produtoRepository.findById(id)).thenReturn(produtoEncontrado);
        if (setupAdicional != null) {
            setupAdicional.accept(this);
        }

        assertThrows(tipoExcecao, () -> produtoService.atualizar(id, request));
    }

    @Test
    void atualizar_deveAtualizarProduto_quandoDadosForemValidos() {
        Produto existente = novoProdutoExistente(true);
        ProdutoRequestDTO request = new ProdutoRequestDTO(
                "Racao Atualizada",
                "Pacote 15kg",
                new BigDecimal("249.90"),
                30,
                true
        );
        ProdutoResponseDTO response = new ProdutoResponseDTO(
                1L, "Racao Atualizada", "Pacote 15kg", new BigDecimal("249.90"), 30, true, null, null
        );

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(itemPedidoService.existeItemComProdutoEmPedidoPago(1L)).thenReturn(false);
        when(produtoRepository.save(any(Produto.class))).thenReturn(existente);
        when(produtoMapper.toResponse(existente)).thenReturn(response);
        doNothing().when(produtoValidator).validateCriacaoOuAtualizacao(request);
        doNothing().when(produtoMapper).updateEntity(existente, request);

        ProdutoResponseDTO resultado = produtoService.atualizar(1L, request);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Racao Atualizada", resultado.nome());
    }

    @Test
    void remover_deveExcluirProduto_quandoProdutoExistir() {
        Produto existente = novoProdutoExistente(true);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(existente));

        produtoService.remover(1L);

        verify(produtoRepository).delete(existente);
    }

    @Test
    void remover_deveLancarExcecao_quandoProdutoNaoExistir() {
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> produtoService.remover(999L));
    }

    private static Stream<Arguments> atualizarCenariosInvalidosProvider() {
        return Stream.of(
                Arguments.of(
                        999L,
                        Optional.empty(),
                        new ProdutoRequestDTO("Racao", "desc", new BigDecimal("10.00"), 1, true),
                        null,
                        EntidadeNaoEncontradaException.class
                ),
                Arguments.of(
                        1L,
                        Optional.of(novoProdutoExistente(true)),
                        new ProdutoRequestDTO("Racao", "desc", BigDecimal.ZERO, 1, true),
                        null,
                        LojaException.class
                ),
                Arguments.of(
                        1L,
                        Optional.of(novoProdutoExistente(false)),
                        new ProdutoRequestDTO("Racao", "desc", new BigDecimal("10.00"), 1, false),
                        null,
                        LojaException.class
                ),
                Arguments.of(
                        1L,
                        Optional.of(novoProdutoExistente(true)),
                        new ProdutoRequestDTO("Racao", "desc", new BigDecimal("10.00"), 1, true),
                        (Consumer<ProdutoServiceTest>) test -> when(test.itemPedidoService.existeItemComProdutoEmPedidoPago(1L)).thenReturn(true),
                        LojaException.class
                )
        );
    }

    private static Produto novoProdutoExistente(boolean ativo) {
        Produto produto = new Produto();
        produto.setId(1L);
        produto.setNome("Racao");
        produto.setDescricao("desc");
        produto.setPreco(new BigDecimal("10.00"));
        produto.setEstoque(10);
        produto.setAtivo(ativo);
        return produto;
    }

    private static ProdutoResponseDTO novaResponse(Long id, String nome) {
        return new ProdutoResponseDTO(
                id,
                nome,
                "desc",
                new BigDecimal("10.00"),
                10,
                true,
                null,
                null
        );
    }
}
