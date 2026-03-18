package com.loja.lojaservice.controller;

import com.loja.lojaservice.entity.Produto;
import com.loja.lojaservice.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ProdutoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProdutoRepository produtoRepository;

    @BeforeEach
    void setup() {
        produtoRepository.deleteAll();
    }

    @Test
    void listarTodos_deveRetornarProdutos_quandoExistiremRegistros() throws Exception {
        produtoRepository.save(novoProduto("Racao 1", true, new BigDecimal("100.00")));
        produtoRepository.save(novoProduto("Racao 2", true, new BigDecimal("200.00")));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void criar_deveRetornarCreated_quandoPayloadValido() throws Exception {
        String payload = objectMapper.writeValueAsString(new ProdutoPayload(
                "Racao Nova",
                "Pacote 10kg",
                new BigDecimal("159.90"),
                15,
                true
        ));

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Racao Nova"));
    }

    @Test
    void buscarPorId_deveRetornarOk_quandoProdutoExistir() throws Exception {
        Produto salvo = produtoRepository.save(novoProduto("Racao Detalhe", true, new BigDecimal("130.00")));

        mockMvc.perform(get("/produtos/{id}", salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.getId()))
                .andExpect(jsonPath("$.nome").value("Racao Detalhe"));
    }

    @Test
    void atualizar_deveRetornarOk_quandoPayloadValido() throws Exception {
        Produto salvo = produtoRepository.save(novoProduto("Racao Base", true, new BigDecimal("120.00")));

        String payload = objectMapper.writeValueAsString(new ProdutoPayload(
                "Racao Atualizada",
                "Pacote 15kg",
                new BigDecimal("180.00"),
                20,
                true
        ));

        mockMvc.perform(put("/produtos/{id}", salvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.getId()))
                .andExpect(jsonPath("$.nome").value("Racao Atualizada"));
    }

    @Test
    void remover_deveRetornarNoContent_quandoProdutoExistir() throws Exception {
        Produto salvo = produtoRepository.save(novoProduto("Racao Delete", true, new BigDecimal("90.00")));

        mockMvc.perform(delete("/produtos/{id}", salvo.getId()))
                .andExpect(status().isNoContent());
    }

    private Produto novoProduto(String nome, boolean ativo, BigDecimal preco) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao("Descricao " + nome);
        produto.setPreco(preco);
        produto.setEstoque(10);
        produto.setAtivo(ativo);
        return produto;
    }

    private record ProdutoPayload(
            String nome,
            String descricao,
            BigDecimal preco,
            Integer estoque,
            Boolean ativo
    ) {
    }
}

