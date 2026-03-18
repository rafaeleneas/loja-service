package com.loja.lojaservice.controller.validator;

import com.loja.lojaservice.controller.dto.ProdutoRequestDTO;
import com.loja.lojaservice.exception.LojaException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProdutoValidator {

    public void validateCriacaoOuAtualizacao(ProdutoRequestDTO dto) {
        if (dto == null) {
            throw new LojaException("Corpo da requisicao nao pode ser nulo.");
        }
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new LojaException("Nome do produto e obrigatorio.");
        }
        if (dto.preco() == null) {
            throw new LojaException("Preco do produto e obrigatorio.");
        }
        if (dto.preco().compareTo(BigDecimal.ZERO) < 0) {
            throw new LojaException("Preco do produto nao pode ser negativo.");
        }
        if (dto.estoque() == null) {
            throw new LojaException("Estoque do produto e obrigatorio.");
        }
        if (dto.estoque() < 0) {
            throw new LojaException("Estoque do produto nao pode ser negativo.");
        }
        if (dto.ativo() == null) {
            throw new LojaException("Indicador ativo do produto e obrigatorio.");
        }
    }
}

