package com.mint.caizhitong.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 鏉愭枡鏄庣粏琛
 * </p>
 *
 * @author mint
 * @since 2025-11-25
 */
@Getter
@Setter
@Data
@TableName("material_item")
public class MaterialItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 涓婚敭ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 绫荤洰ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 鍝佺墝
     */
    @TableField("brand")
    private String brand;

    /**
     * 鍨嬪彿
     */
    @TableField("model")
    private String model;

    /**
     * 瑙勬牸
     */
    @TableField("spec")
    private String spec;

    /**
     * 鍗曚环
     */
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 璐у竵
     */
    @TableField("currency")
    private String currency;

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

    @TableField("name")
    private String name;

    @TableField("code")
    private String code;
}
