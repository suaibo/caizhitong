package com.mint.caizhitong.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Mint
 */
@Data
public class TopConsumeVO {
    private Long itemId;
    private String itemName;
    private String itemCode;
    private String categoryName;
    private String unit;
    private BigDecimal totalQty;    // 消耗数量
    private BigDecimal totalAmount; // 消耗金额
}
