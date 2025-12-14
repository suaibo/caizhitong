package com.mint.caizhitong.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 鐢ㄦ埛琛
 * </p>
 *
 * @author mint
 * @since 2025-11-25
 */
@Getter
@Setter
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 涓婚敭ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 濮撳悕
     */
    @TableField("name")
    private String name;

    @TableField("password")
    private String password;

    @TableField("phone")
    private String phone;
    /**
     * 瑙掕壊
     */
    @TableField("role")
    private String role;

    /**
     * 部门
     */
    @TableField("dept")
    private String dept;

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
