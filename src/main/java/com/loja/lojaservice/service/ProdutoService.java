package com.loja.lojaservice.service;

import com.loja.lojaservice.controller.dto.ProdutoRequestDTO;
import com.loja.lojaservice.controller.dto.ProdutoResponseDTO;
import com.loja.lojaservice.controller.mapper.ProdutoMapper;
import com.loja.lojaservice.controller.validator.ProdutoValidator;
import com.loja.lojaservice.entity.Produto;
import com.loja.lojaservice.exception.EntidadeNaoEncontradaException;
import com.loja.lojaservice.exception.LojaException;
import com.loja.lojaservice.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;
    private final ProdutoValidator produtoValidator;
    private final ItemPedidoService itemPedidoService;

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listarTodos() {
        return produtoRepository.findAll()
                .stream()
                .map(produtoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto produto = findByIdOrThrow(id);
        return produtoMapper.toResponse(produto);
    }

    @Transactional
    public ProdutoResponseDTO criar(ProdutoRequestDTO request) {
        produtoValidator.validateCriacaoOuAtualizacao(request);
        Produto produto = produtoMapper.toEntity(request);
        Produto salvo = produtoRepository.save(produto);
        return produtoMapper.toResponse(salvo);
    }

    @Transactional
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO request) {
        produtoValidator.validateCriacaoOuAtualizacao(request);
        Produto existente = findByIdOrThrow(id);
        //Se preco=0, exigir desativar o mesmo
        if(request.preco().compareTo(BigDecimal.ZERO) == 0 && request.ativo()) {
            throw new LojaException("Preco do produto igual zero voÃ§Ãª deve desativa-lo.");
        }
        
        //validar o produto existente, se estiver inativo, nao pode ser atualizado
        if (!existente.getAtivo() && !request.ativo()) {
            throw new LojaException("Produto inativo, voÃ§Ãª ativa-lo para atualizar.");
        }

        //Proibir atualizaÃ§Ã£o quando produto jÃ¡ estÃ¡ vinculado a pedido pago (regra de catÃ¡logo histÃ³rico).
        if(itemPedidoService.existeItemComProdutoEmPedidoPago(existente.getId())){
            throw new LojaException("Produto vinculado a pedido pago, voÃ§Ãª nÃ£o pode atualizar.");
        }

        produtoMapper.updateEntity(existente, request);
        Produto atualizado = produtoRepository.save(existente);
        return produtoMapper.toResponse(atualizado);
    }

    @Transactional
    public void remover(Long id) {
        Produto existente = findByIdOrThrow(id);
        produtoRepository.delete(existente);
    }

    private Produto findByIdOrThrow(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Produto de id " + id + " nao encontrado."));
    }
}

