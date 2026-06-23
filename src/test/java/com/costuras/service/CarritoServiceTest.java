package com.costuras.service;

import com.costuras.carrito.dto.ActualizarCantidadRequest;
import com.costuras.carrito.dto.AgregarItemRequest;
import com.costuras.carrito.dto.CarritoResponse;
import com.costuras.carrito.model.ItemCarrito;
import com.costuras.carrito.repository.ItemCarritoRepository;
import com.costuras.carrito.security.UsuarioPrincipal;
import com.costuras.carrito.service.CarritoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @InjectMocks
    private CarritoService carritoService;

    private UsuarioPrincipal principal;
    private ItemCarrito item;

    @BeforeEach
    void setUp() {
        principal = UsuarioPrincipal.builder()
                .id(10)
                .username("usuario1")
                .role("CLIENTE")
                .build();

        item = ItemCarrito.builder()
                .id(1)
                .idUsuario(10)
                .idProducto("PROD-1")
                .nombreProducto("Tela algodón")
                .precio(new BigDecimal("100.00"))
                .cantidad(2)
                .build();
    }

    // ---------- getCarrito ----------

    @Test
    void getCarrito_conItems_debeCalcularTotalYCantidad() {
        when(itemCarritoRepository.findByIdUsuario(10)).thenReturn(List.of(item));

        CarritoResponse resultado = carritoService.getCarrito(principal);

        assertThat(resultado.getIdUsuario()).isEqualTo(10);
        assertThat(resultado.getItems()).hasSize(1);
        assertThat(resultado.getTotal()).isEqualTo(new BigDecimal("200.00"));
        assertThat(resultado.getCantidadItems()).isEqualTo(1);
        assertThat(resultado.getItems().get(0).getSubtotal()).isEqualTo(new BigDecimal("200.00"));
    }

    @Test
    void getCarrito_vacio_debeRetornarTotalCero() {
        when(itemCarritoRepository.findByIdUsuario(10)).thenReturn(List.of());

        CarritoResponse resultado = carritoService.getCarrito(principal);

        assertThat(resultado.getItems()).isEmpty();
        assertThat(resultado.getTotal()).isEqualTo(BigDecimal.ZERO);
        assertThat(resultado.getCantidadItems()).isZero();
    }

    @Test
    void getCarrito_conMultiplesItems_debeSumarTodosLosSubtotales() {
        ItemCarrito item2 = ItemCarrito.builder()
                .id(2)
                .idUsuario(10)
                .idProducto("PROD-2")
                .nombreProducto("Hilo")
                .precio(new BigDecimal("5.50"))
                .cantidad(4)
                .build();

        when(itemCarritoRepository.findByIdUsuario(10)).thenReturn(List.of(item, item2));

        CarritoResponse resultado = carritoService.getCarrito(principal);

        assertThat(resultado.getCantidadItems()).isEqualTo(2);
        assertThat(resultado.getTotal()).isEqualTo(new BigDecimal("222.00")); // 200.00 + 22.00
    }

    // ---------- agregarItem ----------

    @Test
    void agregarItem_productoNuevo_debeCrearItem() {
        AgregarItemRequest request = new AgregarItemRequest();
        request.setIdProducto("PROD-9");
        request.setNombreProducto("Botón");
        request.setPrecio(new BigDecimal("1.50"));
        request.setCantidad(3);

        when(itemCarritoRepository.findByIdUsuarioAndIdProducto(10, "PROD-9"))
                .thenReturn(Optional.empty());
        when(itemCarritoRepository.findByIdUsuario(10)).thenReturn(List.of());

        carritoService.agregarItem(request, principal);

        verify(itemCarritoRepository).save(argThat(saved ->
                saved.getIdUsuario().equals(10) &&
                saved.getIdProducto().equals("PROD-9") &&
                saved.getCantidad().equals(3)
        ));
    }

    @Test
    void agregarItem_productoExistente_debeSumarCantidad() {
        AgregarItemRequest request = new AgregarItemRequest();
        request.setIdProducto("PROD-1");
        request.setNombreProducto("Tela algodón");
        request.setPrecio(new BigDecimal("100.00"));
        request.setCantidad(5);

        when(itemCarritoRepository.findByIdUsuarioAndIdProducto(10, "PROD-1"))
                .thenReturn(Optional.of(item));
        when(itemCarritoRepository.findByIdUsuario(10)).thenReturn(List.of(item));

        carritoService.agregarItem(request, principal);

        assertThat(item.getCantidad()).isEqualTo(7); // 2 + 5
        verify(itemCarritoRepository).save(item);
    }

    // ---------- actualizarCantidad ----------

    @Test
    void actualizarCantidad_itemPropio_debeActualizar() {
        ActualizarCantidadRequest request = new ActualizarCantidadRequest();
        request.setCantidad(10);

        when(itemCarritoRepository.findById(1)).thenReturn(Optional.of(item));
        when(itemCarritoRepository.findByIdUsuario(10)).thenReturn(List.of(item));

        carritoService.actualizarCantidad(1, request, principal);

        assertThat(item.getCantidad()).isEqualTo(10);
        verify(itemCarritoRepository).save(item);
    }

    @Test
    void actualizarCantidad_itemInexistente_debeLanzarExcepcion() {
        ActualizarCantidadRequest request = new ActualizarCantidadRequest();
        request.setCantidad(10);

        when(itemCarritoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.actualizarCantidad(99, request, principal))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item no encontrado");

        verify(itemCarritoRepository, never()).save(any());
    }

    @Test
    void actualizarCantidad_itemDeOtroUsuario_debeLanzarExcepcion() {
        ActualizarCantidadRequest request = new ActualizarCantidadRequest();
        request.setCantidad(10);

        ItemCarrito itemAjeno = ItemCarrito.builder()
                .id(1).idUsuario(99).idProducto("PROD-1")
                .nombreProducto("Tela").precio(BigDecimal.TEN).cantidad(1)
                .build();

        when(itemCarritoRepository.findById(1)).thenReturn(Optional.of(itemAjeno));

        assertThatThrownBy(() -> carritoService.actualizarCantidad(1, request, principal))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tienes permiso");

        verify(itemCarritoRepository, never()).save(any());
    }

    // ---------- eliminarItem ----------

    @Test
    void eliminarItem_itemPropio_debeEliminar() {
        when(itemCarritoRepository.findById(1)).thenReturn(Optional.of(item));
        when(itemCarritoRepository.findByIdUsuario(10)).thenReturn(List.of());

        carritoService.eliminarItem(1, principal);

        verify(itemCarritoRepository).delete(item);
    }

    @Test
    void eliminarItem_itemInexistente_debeLanzarExcepcion() {
        when(itemCarritoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.eliminarItem(99, principal))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item no encontrado");

        verify(itemCarritoRepository, never()).delete(any());
    }

    @Test
    void eliminarItem_itemDeOtroUsuario_debeLanzarExcepcion() {
        ItemCarrito itemAjeno = ItemCarrito.builder()
                .id(1).idUsuario(99).idProducto("PROD-1")
                .nombreProducto("Tela").precio(BigDecimal.TEN).cantidad(1)
                .build();

        when(itemCarritoRepository.findById(1)).thenReturn(Optional.of(itemAjeno));

        assertThatThrownBy(() -> carritoService.eliminarItem(1, principal))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tienes permiso");

        verify(itemCarritoRepository, never()).delete(any());
    }

    // ---------- vaciarCarrito ----------

    @Test
    void vaciarCarrito_debeLlamarDeleteByIdUsuario() {
        carritoService.vaciarCarrito(principal);

        verify(itemCarritoRepository).deleteByIdUsuario(10);
    }
}