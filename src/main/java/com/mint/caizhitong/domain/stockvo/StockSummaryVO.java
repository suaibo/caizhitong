package com.mint.caizhitong.domain.stockvo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockSummaryVO {
    private Long itemId;
    private String materialName; // name
    private String materialCode; // code
    private String spec;         // 规格
    private String unit;         // 单位 (来自 Category)
    private String categoryName; // 类目名称

    private BigDecimal totalStock; // 当前总库存 (计算得出)
    private BigDecimal safeStock;  // 安全库存 (来自 Category)
    private boolean isWarning;     // 是否预警 (total < safe)
}