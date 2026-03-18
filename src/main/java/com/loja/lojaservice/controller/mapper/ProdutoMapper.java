package com.loja.lojaservice.controller.mapper;

import com.loja.lojaservice.controller.dto.ProdutoRequestDTO;
import com.loja.lojaservice.controller.dto.ProdutoResponseDTO;
import com.loja.lojaservice.entity.Produto;
import org.springframework.stereotype.Component;

@Component
public class ProdutoMapper {

    public Produto toEntity(ProdutoRequestDTO dto) {
        Produto produto = new Produto();
        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setEstoque(dto.estoque());
        produto.setAtivo(dto.ativo());
        return produto;
    }

    public void updateEntity(Produto entity, ProdutoRequestDTO dto) {
        entity.setNome(dto.nome());
        entity.setDescricao(dto.descricao());
        entity.setPreco(dto.preco());
        entity.setEstoque(dto.estoque());
        entity.setAtivo(dto.ativo());
    }

    public ProdutoResponseDTO toResponse(Produto entity) {
        return new ProdutoResponseDTO(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getPreco(),
                entity.getEstoque(),
                entity.getAtivo(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm()
        );
    }
}

