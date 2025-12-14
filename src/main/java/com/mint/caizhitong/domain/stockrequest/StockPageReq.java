package com.mint.caizhitong.domain.stockrequest;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class StockPageReq {
    // 分页参数 (设置默认值，防止前端不传报错)
    private Integer page = 1;
    private Integer pageSize = 10;

    // 过滤条件
    private String supplier;   // 供应商 (匹配 usage 字段)
    private Long operatorId;   // 经办人

    // 日期范围查询
    // @DateTimeFormat 用于接收前端传来的 "YYYY-MM-DD" 字符串
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateTo;
}
