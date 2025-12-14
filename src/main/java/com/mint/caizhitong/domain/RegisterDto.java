package com.mint.caizhitong.domain;

import lombok.Data;

@Data
public class RegisterDto {

    private String name;

    private String role;

    private String password;

    /**
     * 部门
     */
    private String dept;

}
