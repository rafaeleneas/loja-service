package com.loja.lojaservice.repository;

import com.loja.lojaservice.entity.ItemPedido;
import com.loja.lojaservice.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
    List<ItemPedido> findByPedidoId(Long pedidoId);

    boolean existsByProdutoIdAndPedidoStatus(Long produtoId, StatusPedido status);
}

