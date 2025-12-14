package com.mint.caizhitong.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {

    @NotNull
    private String name;

    private String role;

    private String phone;
    /**
     * 部门
     */
    private String dept;


}
