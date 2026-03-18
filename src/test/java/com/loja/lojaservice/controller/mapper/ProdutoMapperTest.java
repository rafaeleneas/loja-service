package com.loja.lojaservice.controller.mapper;

import com.loja.lojaservice.controller.dto.ProdutoRequestDTO;
import com.loja.lojaservice.controller.dto.ProdutoResponseDTO;
import com.loja.lojaservice.entity.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProdutoMapperTest {

    private final ProdutoMapper mapper = new ProdutoMapper();

    @Test
    void toEntity_deveMapearCampos_quandoProdutoRequestDTOValido() {
        ProdutoRequestDTO request = new ProdutoRequestDTO(
                "Racao Premium",
                "Pacote 10kg",
                new BigDecimal("199.90"),
                12,
                true
        );

        Produto entity = mapper.toEntity(request);

        assertEquals("Racao Premium", entity.getNome());
        assertEquals("Pacote 10kg", entity.getDescricao());
        assertEquals(new BigDecimal("199.90"), entity.getPreco());
        assertEquals(12, entity.getEstoque());
        assertEquals(true, entity.getAtivo());
    }

    @Test
    void updateEntity_deveAtualizarCampos_quandoEntityERequestValidos() {
        Produto entity = new Produto();
        entity.setNome("Racao Antiga");
        entity.setDescricao("Descricao antiga");
        entity.setPreco(new BigDecimal("99.90"));
        entity.setEstoque(5);
        entity.setAtivo(false);

        ProdutoRequestDTO request = new ProdutoRequestDTO(
                "Racao Nova",
                "Descricao nova",
                new BigDecimal("149.90"),
                20,
                true
        );

        mapper.updateEntity(entity, request);

        assertEquals("Racao Nova", entity.getNome());
        assertEquals("Descricao nova", entity.getDescricao());
        assertEquals(new BigDecimal("149.90"), entity.getPreco());
        assertEquals(20, entity.getEstoque());
        assertEquals(true, entity.getAtivo());
    }

    @Test
    void toResponse_deveMapearCampos_quandoEntityValida() {
        Produto entity = new Produto();
        entity.setId(10L);
        entity.setNome("Racao Senior");
        entity.setDescricao("Pacote 15kg");
        entity.setPreco(new BigDecimal("239.90"));
        entity.setEstoque(8);
        entity.setAtivo(true);
        entity.setCriadoEm(OffsetDateTime.parse("2026-03-04T10:00:00Z"));
        entity.setAtualizadoEm(OffsetDateTime.parse("2026-03-04T11:00:00Z"));

        ProdutoResponseDTO response = mapper.toResponse(entity);

        assertEquals(10L, response.id());
        assertEquals("Racao Senior", response.nome());
        assertEquals("Pacote 15kg", response.descricao());
        assertEquals(new BigDecimal("239.90"), response.preco());
        assertEquals(8, response.estoque());
        assertEquals(true, response.ativo());
        assertEquals(OffsetDateTime.parse("2026-03-04T10:00:00Z"), response.criadoEm());
        assertEquals(OffsetDateTime.parse("2026-03-04T11:00:00Z"), response.atualizadoEm());
    }
}

