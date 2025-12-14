package com.mint.caizhitong.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Mint
 */
@Data
public class ProjectExpenseVO {
    @ExcelProperty("发生时间")
    private LocalDateTime createTime;

    @ExcelProperty("项目号")
    private String projectNo;

    @ExcelProperty("材料名称")
    private String itemName;

    @ExcelProperty("领用数量")
    private BigDecimal qty;

    @ExcelProperty("单价")
    private BigDecimal unitPrice;

    @ExcelProperty("金额")
    private BigDecimal amount;

    @ExcelProperty("用途")
    private String usage;
}
