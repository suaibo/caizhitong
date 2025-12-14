package com.mint.caizhitong.domain.stockvo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockAlertVO {
    private Long itemId;
    private String name;         // 材料名称
    private String categoryName; // 类目名称
    private BigDecimal totalStock; // 当前库存
    private BigDecimal safeStock;  // 安全库存
}