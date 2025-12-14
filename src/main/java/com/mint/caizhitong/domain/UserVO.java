package com.mint.caizhitong.domain;

import lombok.Data;

@Data
public class UserVO {

    private Long id;

    private String name;

    private String password;

    private String role;

    /**
     * 部门
     */
    private String dept;
}
