package com.loja.lojaservice.repository;

import com.loja.lojaservice.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByAtivoTrue();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Produto p
               set p.estoque = p.estoque - :quantidade,
                   p.atualizadoEm = CURRENT_TIMESTAMP
             where p.id = :produtoId
               and p.estoque >= :quantidade
            """)
    int decrementarEstoqueSeDisponivel(@Param("produtoId") Long produtoId, @Param("quantidade") Integer quantidade);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Produto p
               set p.estoque = p.estoque + :quantidade,
                   p.atualizadoEm = CURRENT_TIMESTAMP
             where p.id = :produtoId
            """)
    int incrementarEstoque(@Param("produtoId") Long produtoId, @Param("quantidade") Integer quantidade);
}

