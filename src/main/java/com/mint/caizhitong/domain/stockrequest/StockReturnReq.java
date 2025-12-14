package com.mint.caizhitong.domain.stockrequest;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockReturnReq {
    @NotNull(message = "原始领用记录ID不能为空")
    private Long originTransactionId; // 关联的出库流水ID

    @NotNull(message = "归还数量不能为空")
    private BigDecimal qty;           // 归还数量 (正数)

    @NotNull(message = "归还人不能为空")
    private Long userId;

    private String remark;            // 归还说明 (如：完好如新)
}
