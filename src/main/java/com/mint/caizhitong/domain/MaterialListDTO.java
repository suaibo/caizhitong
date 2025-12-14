package com.mint.caizhitong.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaterialListDTO {
    private Long id;
    private String code;
    private String name;
    private Long categoryId;
    private String brand;
    private String model;
    private String spec;
    private String unit;        // <-- 来自 material_category
    private BigDecimal safeStock; // <-- 来自 material_category
    private BigDecimal totalStock; // <-- 来自 stock_batch (SUM 聚合)
    private String currency;
    private BigDecimal unitPrice;
}
