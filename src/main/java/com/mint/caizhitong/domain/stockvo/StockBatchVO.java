package com.mint.caizhitong.domain.stockvo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StockBatchVO {
    private Long id;
    private Long itemId;
    private String batchCode;  // 批次号
    private BigDecimal quantity; // 剩余数量
    private LocalDate expireDate; // 过期日
    private String barcode;    // 条码
}
