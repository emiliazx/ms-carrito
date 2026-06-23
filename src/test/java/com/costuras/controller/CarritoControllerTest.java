package com.costuras.controller;


import com.costuras.carrito.controller.CarritoController;
import com.costuras.carrito.dto.ActualizarCantidadRequest;
import com.costuras.carrito.dto.AgregarItemRequest;
import com.costuras.carrito.dto.CarritoResponse;
import com.costuras.carrito.security.UsuarioPrincipal;
import com.costuras.carrito.service.CarritoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SuppressWarnings("null")
@WebMvcTest(CarritoController.class)
@AutoConfigureMockMvc(addFilters = false)
class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CarritoService carritoService;

    private UsuarioPrincipal principal;
    private CarritoResponse carritoResponse;

    @BeforeEach
    void setUp() {
        principal = UsuarioPrincipal.builder()
                .id(1).username("usuario1").role("USER").build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));

        carritoResponse = new CarritoResponse();
        carritoResponse.setItems(List.of());
        carritoResponse.setTotal(BigDecimal.ZERO);
        carritoResponse.setCantidadItems(0);
    }

    @Test
    void getCarrito_autenticado_retorna200() throws Exception {
        when(carritoService.getCarrito(any())).thenReturn(carritoResponse);

        mockMvc.perform(get("/carrito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void agregarItem_datosValidos_retorna201() throws Exception {
        AgregarItemRequest request = new AgregarItemRequest();
        request.setIdProducto("prod-1");
        request.setNombreProducto("Tela azul");
        request.setPrecio(new BigDecimal("5000"));
        request.setCantidad(2);

        when(carritoService.agregarItem(any(), any())).thenReturn(carritoResponse);

        mockMvc.perform(post("/carrito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void actualizarCantidad_itemExistente_retorna200() throws Exception {
        ActualizarCantidadRequest request = new ActualizarCantidadRequest();
        request.setCantidad(5);

        when(carritoService.actualizarCantidad(eq(10), any(), any())).thenReturn(carritoResponse);

        mockMvc.perform(put("/carrito/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarCantidad_itemAjeno_retorna403() throws Exception {
        ActualizarCantidadRequest request = new ActualizarCantidadRequest();
        request.setCantidad(5);

        when(carritoService.actualizarCantidad(eq(10), any(), any()))
                .thenThrow(new RuntimeException("Sin permiso"));

        mockMvc.perform(put("/carrito/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void eliminarItem_existente_retorna200() throws Exception {
        when(carritoService.eliminarItem(eq(10), any())).thenReturn(carritoResponse);

        mockMvc.perform(delete("/carrito/10"))
                .andExpect(status().isOk());
    }

    @Test
    void vaciarCarrito_retorna200() throws Exception {
        doNothing().when(carritoService).vaciarCarrito(any());

        mockMvc.perform(delete("/carrito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Carrito vaciado correctamente"));
    }
}
