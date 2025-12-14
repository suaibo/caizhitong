package com.mint.caizhitong.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Mint
 */
@Data
public class DashboardOverviewVO {
    private Long totalMaterialKinds;      // 材料种类总数
    private Integer warningItemCount;     // 预警材料数
    private BigDecimal semesterConsumeCount; // 学期消耗总数量 (注意：这里用 BigDecimal 防止小数)
    private BigDecimal semesterConsumeAmount;// 学期消耗总金额
}