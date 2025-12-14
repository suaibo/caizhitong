package com.mint.caizhitong.domain.stockrequest;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockOutReq {
    private String barcode;    // 条码 (可选，扫码枪场景)
    private Long itemId;       // 材料ID (可选，如果传了batchId)

    @NotNull(message = "批次ID不能为空")
    private Long batchId;      // 明确指定扣减哪个批次

    @NotNull(message = "数量不能为空")
    private BigDecimal qty;    // 领用数量 (正数)

    @NotNull(message = "领用人不能为空")
    private Long userId;       // 领用人ID

    private String projectNo;  // 项目号
    private String usage;      // 用途说明
    private String courseTerm; // 学期 (如 2025春)
    private String courseName; // 课程名
}
