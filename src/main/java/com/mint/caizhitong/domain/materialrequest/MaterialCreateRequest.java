package com.mint.caizhitong.domain.materialrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaterialCreateRequest {
    @NotBlank
    private String code;
    @NotBlank private String name;
    @NotNull
    private Long categoryId;
    private String brand;
    private String model;
    private String spec;
    private String unit; // 仅用于校验/冗余
    private BigDecimal safeStock; // 仅用于校验/冗余
    private BigDecimal unitPrice;
    private String currency;
    private String remark;
}
