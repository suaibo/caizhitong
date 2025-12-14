package com.mint.caizhitong.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 搴撳瓨浜嬪姟鏄庣粏琛
 * </p>
 *
 * @author mint
 * @since 2025-11-25
 */
@Getter
@Setter
@TableName("stock_transaction")
public class StockTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 涓婚敭ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 鎵规?ID
     */
    @TableField("batch_id")
    private Long batchId;

    /**
     * 浜嬪姟绫诲瀷
     */
    @TableField("txn_type")
    private String txnType;

    /**
     * 鏁伴噺锛堟?鏁板叆搴?璐熸暟鍑哄簱锛
     */
    @TableField("qty")
    private BigDecimal qty;

    /**
     * 鎿嶄綔浜篒D
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 椤圭洰鍙
     */
    @TableField("project_no")
    private String projectNo;

    /**
     * 鐢ㄩ?璇存槑
     */
    @TableField("usage")
    private String usage;

    /**
     * 鍒涘缓鏃堕棿
     */
    @TableField("create_time")
    private LocalDateTime createTime;


}
