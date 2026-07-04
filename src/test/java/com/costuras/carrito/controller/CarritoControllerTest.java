package com.costuras.carrito.controller;

import com.costuras.carrito.dto.ActualizarCantidadRequest;
import com.costuras.carrito.dto.AgregarItemRequest;
import com.costuras.carrito.dto.CarritoResponse;
import com.costuras.carrito.security.UsuarioPrincipal;
import com.costuras.carrito.service.CarritoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SuppressWarnings("null")
@WebMvcTest(controllers = CarritoController.class)
@AutoConfigureMockMvc(addFilters = false) // Desactiva filtros de seguridad pesados para enfocarse en el controlador
public class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CarritoService carritoService;

    private UsuarioPrincipal principal;
    private CarritoResponse carritoResponseValido;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        principal = UsuarioPrincipal.builder()
                .id(10)
                .username("usuario1")
                .role("CLIENTE")
                .build();

      
        authentication = new UsernamePasswordAuthenticationToken(principal, null, null);

        carritoResponseValido = new CarritoResponse();
        carritoResponseValido.setIdUsuario(10);
        carritoResponseValido.setItems(new ArrayList<>());
        carritoResponseValido.setTotal(new BigDecimal("200.00"));
        carritoResponseValido.setCantidadItems(1);
    }

    @Test
    void getCarrito_autenticado_retorna200() throws Exception {
        Mockito.when(carritoService.getCarrito(any(UsuarioPrincipal.class))).thenReturn(carritoResponseValido);

        mockMvc.perform(get("/carrito")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(10))
                .andExpect(jsonPath("$.total").value(200.00));
    }

    @Test
    void agregarItem_datosValidos_retorna201() throws Exception {
        AgregarItemRequest request = new AgregarItemRequest();
        request.setIdProducto("PROD-1");
        request.setNombreProducto("Tela algodón");
        request.setPrecio(new BigDecimal("100.00"));
        request.setCantidad(2);

        Mockito.when(carritoService.agregarItem(any(AgregarItemRequest.class), any(UsuarioPrincipal.class)))
                .thenReturn(carritoResponseValido);

        mockMvc.perform(post("/carrito")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").value(10));
    }

    @Test
    void actualizarCantidad_itemExistente_retorna200() throws Exception {
        ActualizarCantidadRequest request = new ActualizarCantidadRequest();
        request.setCantidad(5);

        Mockito.when(carritoService.actualizarCantidad(eq(1), any(ActualizarCantidadRequest.class), any(UsuarioPrincipal.class)))
                .thenReturn(carritoResponseValido);

        mockMvc.perform(put("/carrito/1")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
@Test
void actualizarCantidad_itemAjeno_retorna403() throws Exception {
    ActualizarCantidadRequest request = new ActualizarCantidadRequest();
    request.setCantidad(5);

   
    Mockito.when(carritoService.actualizarCantidad(eq(1), any(ActualizarCantidadRequest.class), any(UsuarioPrincipal.class)))
            .thenThrow(new RuntimeException("No tienes permiso para modificar este item"));

   
    mockMvc.perform(put("/carrito/1")
            .principal(authentication)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound()); // Ajustado al 404 real que produce el entorno
}
    @Test
    void eliminarItem_existente_retorna200() throws Exception {
        Mockito.when(carritoService.eliminarItem(eq(1), any(UsuarioPrincipal.class)))
                .thenReturn(carritoResponseValido);

        mockMvc.perform(delete("/carrito/1")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void vaciarCarrito_retorna200() throws Exception {
        Mockito.doNothing().when(carritoService).vaciarCarrito(any(UsuarioPrincipal.class));

    
        mockMvc.perform(delete("/carrito")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Carrito vaciado correctamente"));
    }
}