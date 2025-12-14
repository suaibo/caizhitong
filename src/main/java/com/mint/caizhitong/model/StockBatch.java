package com.mint.caizhitong.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 搴撳瓨鎵规?琛
 * </p>
 *
 * @author mint
 * @since 2025-11-25
 */
@Getter
@Setter
@TableName("stock_batch")
public class StockBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 涓婚敭ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 鏉愭枡ID
     */
    @TableField("item_id")
    private Long itemId;

    /**
     * 鎵规?鍙
     */
    @TableField("batch_code")
    private String batchCode;

    /**
     * 搴撳瓨鏁伴噺
     */
    @TableField("quantity")
    private BigDecimal quantity;

    /**
     * 杩囨湡鏃ユ湡
     */
    @TableField("expire_date")
    private LocalDate expireDate;

    /**
     * 鏉″舰鐮
     */
    @TableField("barcode")
    private String barcode;

    /**
     * 鍒涘缓浜篒D
     */
    @TableField("create_by")
    private Long createBy;

    /**
     * 鍒涘缓鏃堕棿
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 鏇存柊浜篒D
     */
    @TableField("update_by")
    private Long updateBy;

    /**
     * 鏇存柊鏃堕棿
     */
    @TableField("update_time")
    private LocalDateTime updateTime;


}
