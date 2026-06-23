package com.costuras.carrito.service;




import com.costuras.carrito.dto.ActualizarCantidadRequest;
import com.costuras.carrito.dto.AgregarItemRequest;
import com.costuras.carrito.dto.CarritoResponse;
import com.costuras.carrito.dto.ItemCarritoResponse;
import com.costuras.carrito.model.ItemCarrito;
import com.costuras.carrito.repository.ItemCarritoRepository;
import com.costuras.carrito.security.UsuarioPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final ItemCarritoRepository itemCarritoRepository;

  
    public CarritoResponse getCarrito(UsuarioPrincipal principal) {
        List<ItemCarrito> items = itemCarritoRepository.findByIdUsuario(principal.getId());
        return buildCarritoResponse(principal.getId(), items);
    }

    
    public CarritoResponse agregarItem(AgregarItemRequest request, UsuarioPrincipal principal) {
        itemCarritoRepository
                .findByIdUsuarioAndIdProducto(principal.getId(), request.getIdProducto())
                .ifPresentOrElse(
                        item -> {
                            item.setCantidad(item.getCantidad() + request.getCantidad());
                            itemCarritoRepository.save(item);
                        },
                        () -> itemCarritoRepository.save(ItemCarrito.builder()
                                .idUsuario(principal.getId())
                                .idProducto(request.getIdProducto())
                                .nombreProducto(request.getNombreProducto())
                                .precio(request.getPrecio())
                                .cantidad(request.getCantidad())
                                .build())
                );

        return getCarrito(principal);
    }

   
    public CarritoResponse actualizarCantidad(
            Integer idItem,
            ActualizarCantidadRequest request,
            UsuarioPrincipal principal
    ) {
        ItemCarrito item = itemCarritoRepository.findById(idItem)
                .orElseThrow(() -> new RuntimeException("Item no encontrado: " + idItem));

        if (!item.getIdUsuario().equals(principal.getId())) {
            throw new RuntimeException("No tienes permiso para modificar este item");
        }

        item.setCantidad(request.getCantidad());
        itemCarritoRepository.save(item);
        return getCarrito(principal);
    }

   
    public CarritoResponse eliminarItem(Integer idItem, UsuarioPrincipal principal) {
        ItemCarrito item = itemCarritoRepository.findById(idItem)
                .orElseThrow(() -> new RuntimeException("Item no encontrado: " + idItem));

        if (!item.getIdUsuario().equals(principal.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar este item");
        }

        itemCarritoRepository.delete(item);
        return getCarrito(principal);
    }

    
    @Transactional
    public void vaciarCarrito(UsuarioPrincipal principal) {
        itemCarritoRepository.deleteByIdUsuario(principal.getId());
    }

   

    private CarritoResponse buildCarritoResponse(Integer idUsuario, List<ItemCarrito> items) {
        List<ItemCarritoResponse> itemsResponse = items.stream()
                .map(i -> ItemCarritoResponse.builder()
                        .id(i.getId())
                        .idProducto(i.getIdProducto())
                        .nombreProducto(i.getNombreProducto())
                        .precio(i.getPrecio())
                        .cantidad(i.getCantidad())
                        .subtotal(i.getPrecio().multiply(BigDecimal.valueOf(i.getCantidad())))
                        .build())
                .toList();

        BigDecimal total = itemsResponse.stream()
                .map(ItemCarritoResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CarritoResponse.builder()
                .idUsuario(idUsuario)
                .items(itemsResponse)
                .total(total)
                .cantidadItems(itemsResponse.size())
                .build();
    }
}
