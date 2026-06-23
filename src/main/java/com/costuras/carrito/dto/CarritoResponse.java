package com.costuras.carrito.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
 @Builder 
 @NoArgsConstructor 
 @AllArgsConstructor
 
public class CarritoResponse {
    private Integer idUsuario;

     private List<ItemCarritoResponse> items;

    private BigDecimal total; 

    private Integer cantidadItems;
}
