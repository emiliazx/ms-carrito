package com.costuras.carrito.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

 @Builder 
 @NoArgsConstructor 
 @AllArgsConstructor

public class ItemCarritoResponse {
    
    private Integer id;

     private String idProducto; 

     private String nombreProducto;

    private BigDecimal precio; 

    private Integer cantidad;

     private BigDecimal subtotal;
}
