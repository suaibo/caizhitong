package com.mint.caizhitong.domain.stockrequest;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class StockTransactionPageReq {
    private Integer page = 1;
    private Integer pageSize = 10;

    private Long itemId;      // 核心过滤：材料ID
    private Long userId;      // 过滤：操作人
    private String txnType;   // 过滤：IN, OUT, RETURN, SCRAP
    private String projectNo; // 模糊匹配：项目号

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateTo;
}
