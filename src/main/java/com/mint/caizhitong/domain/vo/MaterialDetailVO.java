package com.mint.caizhitong.domain.vo;

import com.mint.caizhitong.domain.stockvo.StockBatchVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Mint
 */
@Data
public class MaterialDetailVO {
    // --- 基础信息 (来自 MaterialItem) ---
    private Long id;
    private String code;
    private String name;
    private String brand;
    private String model;
    private String spec;
    private BigDecimal unitPrice;
    private String currency;
    //实际并没有这个字段，我后面可能会加
    private String remark;

    // --- 类目信息 (来自 MaterialCategory) ---
    private Long categoryId;
    private String categoryName;
    private String unit;
    private BigDecimal safeStock;

    // --- 库存信息 (统计得出) ---
    private BigDecimal totalStock;

    // --- 批次列表 (明细) ---
    private List<StockBatchVO> batches;
}