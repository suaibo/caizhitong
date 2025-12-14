package com.mint.caizhitong.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Mint
 */
@Data
public class TermConsumeVO {
    @ExcelProperty("材料编码")
    private String itemCode;

    @ExcelProperty("材料名称")
    private String itemName;

    @ExcelProperty("规格")
    private String spec;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("消耗总量")
    private BigDecimal totalQty;

    @ExcelProperty("单价")
    private BigDecimal unitPrice;

    @ExcelProperty("总金额")
    private BigDecimal totalAmount;
}
