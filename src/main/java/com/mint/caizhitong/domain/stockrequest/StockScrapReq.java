package com.mint.caizhitong.domain.stockrequest;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StockScrapReq {
    private Long batchId;
    private Long itemId;
    private BigDecimal qty;
    private Long userId;
    private String reason;
    private List<String> photoUrls; // 图片链接列表
}
