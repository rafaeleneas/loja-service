package com.loja.lojaservice.repository;

import com.loja.lojaservice.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(Long clienteId);

    Optional<Pedido> findByIdempotencyKey(String idempotencyKey);
}
