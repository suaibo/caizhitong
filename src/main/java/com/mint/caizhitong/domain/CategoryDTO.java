package com.mint.caizhitong.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Mint
 */
@Data
public class CategoryDTO {
    // 父类目ID，允许为 null
    private Long parentId;

    // 类目名称，不能为空
    @NotBlank(message = "类目名称不能为空")
    private String name;

    private String unit;
    private BigDecimal safeStock;
}
