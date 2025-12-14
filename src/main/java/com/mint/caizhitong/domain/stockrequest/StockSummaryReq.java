package com.mint.caizhitong.domain.stockrequest;

import lombok.Data;

@Data
public class StockSummaryReq {
    private Long itemId;      // 指定材料
    private Long categoryId;  // 指定类目
    private Boolean warnOnly; // 是否仅看预警 (库存 < 安全库存)
}
