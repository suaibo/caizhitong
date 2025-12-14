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
 * 鏉愭枡绫荤洰鏍
 * </p>
 *
 * @author mint
 * @since 2025-11-25
 */
@Getter
@Setter
@TableName("material_category")
public class MaterialCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 涓婚敭ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 鐖剁被鐩甀D
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 绫荤洰鍚嶇О
     */
    @TableField("name")
    private String name;

    /**
     * 鍗曚綅
     */
    @TableField("unit")
    private String unit;

    /**
     * 瀹夊叏搴撳瓨
     */
    @TableField("safe_stock")
    private BigDecimal safeStock;

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
