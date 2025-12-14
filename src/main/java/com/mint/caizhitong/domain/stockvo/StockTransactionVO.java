package com.mint.caizhitong.domain.stockvo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockTransactionVO {
    private Long id;
    private Long batchId;
    private String batchCode;   // 批次号

    private Long itemId;        // 从 batch 中获取
    private String materialName;// 材料名称
    private String materialCode;// 材料编码

    private String txnType;
    private BigDecimal qty;
    private Long userId;
    private String projectNo;
    private String usage;       // 用途/备注
    private LocalDateTime createTime;
}
