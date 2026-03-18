package com.loja.lojaservice.service;

import com.loja.lojaservice.entity.ItemPedido;
import com.loja.lojaservice.enums.StatusPedido;
import com.loja.lojaservice.exception.EntidadeNaoEncontradaException;
import com.loja.lojaservice.repository.ItemPedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemPedidoService {

    private final ItemPedidoRepository itemPedidoRepository;

    @Transactional(readOnly = true)
    public List<ItemPedido> listarTodos() {
        return itemPedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ItemPedido buscarPorId(Long id) {
        return itemPedidoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("ItemPedido de id " + id + " nao encontrado."));
    }

    @Transactional(readOnly = true)
    public List<ItemPedido> listarPorPedido(Long pedidoId) {
        return itemPedidoRepository.findByPedidoId(pedidoId);
    }

    @Transactional(readOnly = true)
    public boolean existeItemComProdutoEmPedidoPago(Long produtoId) {
        return itemPedidoRepository.existsByProdutoIdAndPedidoStatus(produtoId, StatusPedido.PAGO);
    }
}

