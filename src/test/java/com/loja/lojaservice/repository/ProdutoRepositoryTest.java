package com.loja.lojaservice.repository;

import com.loja.lojaservice.entity.Produto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class ProdutoRepositoryTest {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    void findByAtivoTrue_naoDeveRetornarProdutos_quandoNaoExistiremAtivos() {
        produtoRepository.save(novoProduto("Produto Inativo 1", false));
        produtoRepository.save(novoProduto("Produto Inativo 2", false));

        List<Produto> ativos = produtoRepository.findByAtivoTrue();

        assertEquals(0, ativos.size());
    }

    @Test
    void findByAtivoTrue_deveRetornarSomenteProdutosAtivos_quandoExistiremAtivosEInativos() {
        produtoRepository.save(novoProduto("Produto Ativo 1", true));
        produtoRepository.save(novoProduto("Produto Ativo 2", true));
        produtoRepository.save(novoProduto("Produto Inativo", false));

        List<Produto> ativos = produtoRepository.findByAtivoTrue();

        assertEquals(2, ativos.size());
    }  

    private Produto novoProduto(String nome, boolean ativo) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao("Descricao " + nome);
        produto.setPreco(new BigDecimal("10.00"));
        produto.setEstoque(5);
        produto.setAtivo(ativo);
        return produto;
    }
}

