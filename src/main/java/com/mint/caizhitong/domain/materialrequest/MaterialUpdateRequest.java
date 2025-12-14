package com.mint.caizhitong.domain.materialrequest;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaterialUpdateRequest {
    // 编码：更新时可能需要校验唯一性
    private String code;

    // 名称：更新时可能需要校验唯一性
    private String name;

    // 类目ID：如果更换类目，需要校验新类目是否存在
    private Long categoryId;

    // 品牌
    private String brand;

    // 型号
    private String model;

    // 规格
    private String spec;

    // 单价
    private BigDecimal unitPrice;

    // 货币
    private String currency;

    // 备注 (假设 material_item 表已新增此字段)
    private String remark;

    // --- 冗余自 Category 的字段（如果需要同步更新 Category）---

    // 单位（通常由 Category 决定，更新时应忽略或另作处理）
    private String unit;

    // 安全库存（通常由 Category 决定，更新时应忽略或另作处理）
    private BigDecimal safeStock;
}
