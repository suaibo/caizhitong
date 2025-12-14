package com.mint.caizhitong.domain.StockRequest;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Mint
 */
@Data
public class StockInReq {
    private String supplier;      // 供应商 (由于没有表头表，建议存入流水备注或扩展表)
    private String contractNo;    // 合同号 (建议存入流水的 project_no)
    private Long operatorId;      // 操作人ID
    private String remark;        // 备注
    private List<StockInItemReq> items; // 入库明细

    @Data
    public static class StockInItemReq {
        private Long itemId;        // 材料ID
        private String batchCode;   // 批次号
        private BigDecimal quantity;// 数量
        private BigDecimal unitPrice;// 单价 (注意：当前StockBatch表无单价字段，通常仅用于记录流水或更新材料均价)
        private LocalDate expireDate;// 过期日期
        private String barcode;     // 条码
        private String location;    // 库位 (当前DB无此字段，暂存备注或忽略)
    }
}
