package com.costuras.carrito.repository;

import com.costuras.carrito.model.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Integer> {

    List<ItemCarrito> findByIdUsuario(Integer idUsuario);
    Optional<ItemCarrito> findByIdUsuarioAndIdProducto(Integer idUsuario, String idProducto);
    void deleteByIdUsuario(Integer idUsuario);
}
