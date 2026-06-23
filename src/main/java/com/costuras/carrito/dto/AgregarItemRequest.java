package com.costuras.carrito.dto;

import lombok.Data;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
@Data
public class AgregarItemRequest {
    @NotBlank(message = "idProducto obligatorio") 
    private String idProducto;

    @NotBlank(message = "nombreProducto obligatorio")
     private String nombreProducto;


    @NotNull 
    @Positive(message = "precio mayor a 0") 
    private BigDecimal precio;
    
    @NotNull 
    @Positive(message = "cantidad mayor a 0") 
    private Integer cantidad;
}
