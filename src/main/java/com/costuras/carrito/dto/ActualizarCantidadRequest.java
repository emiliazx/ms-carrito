package com.costuras.carrito.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
@Data public class ActualizarCantidadRequest {
    @NotNull 
    @Positive(message = "cantidad mayor a 0") private Integer cantidad;
}
