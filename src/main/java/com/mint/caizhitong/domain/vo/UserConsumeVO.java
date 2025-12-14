package com.mint.caizhitong.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Mint
 */
@Data
public class UserConsumeVO {
    @ExcelProperty("领用时间")
    private LocalDateTime createTime;

    @ExcelProperty("材料名称")
    private String itemName;

    @ExcelProperty("规格")
    private String spec;

    @ExcelProperty("领用数量")
    private BigDecimal qty;

    @ExcelProperty("批次号")
    private String batchCode;

    @ExcelProperty("备注")
    private String usage;
}
