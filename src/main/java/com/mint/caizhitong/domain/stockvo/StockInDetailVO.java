package com.mint.caizhitong.domain.stockvo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StockInDetailVO {
    // --- 流水信息 ---
    private Long id;
    private String txnType;
    private BigDecimal qty;
    private Long operatorId;
    private String contractNo;
    private String usageInfo;
    private LocalDateTime createTime;

    // --- 批次信息 ---
    private String batchCode;
    private LocalDate expireDate;
    private String barcode;
    private BigDecimal stockQuantity;

    // --- 物料信息 ---
    private Long itemId;
    private String materialName; // 对应 MaterialItem.name
    private String materialCode; // 【新增】对应 MaterialItem.code
    private String brand;
    private String model;
    private String spec;

    // 如果您还需要展示“单位”（如个、kg），因为 MaterialItem 里没有 unit，
    // 您可能还需要查 Category，或者后续也在 Item 里冗余 unit 字段。
    // private String unit;
}
