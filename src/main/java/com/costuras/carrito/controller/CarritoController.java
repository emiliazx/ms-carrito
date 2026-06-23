package com.costuras.carrito.controller;

import com.costuras.carrito.dto.ActualizarCantidadRequest;
import com.costuras.carrito.dto.AgregarItemRequest;
import com.costuras.carrito.dto.CarritoResponse;
import com.costuras.carrito.security.UsuarioPrincipal;
import com.costuras.carrito.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/carrito")
@RequiredArgsConstructor
@Tag(name = "Carrito", description = "Gestión del carrito de compras del usuario")
public class CarritoController {

    private final CarritoService carritoService;

    @Operation(summary = "Ver carrito", description = "Obtiene el carrito del usuario autenticado con total y cantidad de items.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carrito obtenido correctamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping
    public ResponseEntity<CarritoResponse> getCarrito(Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(carritoService.getCarrito(principal));
    }

    @Operation(summary = "Agregar item al carrito",
               description = "Agrega un producto al carrito. Si ya existe, incrementa la cantidad.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Item agregado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping
    public ResponseEntity<CarritoResponse> agregarItem(
            @Valid @RequestBody AgregarItemRequest request, Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(carritoService.agregarItem(request, principal));
    }

    @Operation(summary = "Actualizar cantidad de item",
               description = "Modifica la cantidad de un item existente en el carrito.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cantidad actualizada correctamente"),
        @ApiResponse(responseCode = "403", description = "No tienes permiso para modificar este item"),
        @ApiResponse(responseCode = "404", description = "Item no encontrado")
    })
    @PutMapping("/{idItem}")
    public ResponseEntity<CarritoResponse> actualizarCantidad(
            @PathVariable Integer idItem,
            @Valid @RequestBody ActualizarCantidadRequest request,
            Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(carritoService.actualizarCantidad(idItem, request, principal));
    }

    @Operation(summary = "Eliminar item del carrito", description = "Elimina un producto específico del carrito.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item eliminado correctamente"),
        @ApiResponse(responseCode = "403", description = "No tienes permiso para eliminar este item"),
        @ApiResponse(responseCode = "404", description = "Item no encontrado")
    })
    @DeleteMapping("/{idItem}")
    public ResponseEntity<CarritoResponse> eliminarItem(
            @PathVariable Integer idItem, Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(carritoService.eliminarItem(idItem, principal));
    }

    @Operation(summary = "Vaciar carrito", description = "Elimina todos los items del carrito del usuario.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Carrito vaciado correctamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping
    public ResponseEntity<Map<String, String>> vaciarCarrito(Authentication auth) {
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        carritoService.vaciarCarrito(principal);
        return ResponseEntity.ok(Map.of("mensaje", "Carrito vaciado correctamente"));
    }
}