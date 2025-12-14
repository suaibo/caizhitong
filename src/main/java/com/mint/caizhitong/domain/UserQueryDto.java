package com.mint.caizhitong.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Mint
 */
@Data
public class UserQueryDto {

    @NotNull(message = "页码不能为空")
    @Min(value = 1,message = "页码必须从1开始")
    private Long pageNum;

    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小不能小于 1")
    @Max(value = 100, message = "每页大小不能超过 100")
    private Long pageSize;
    //一下字段为可选条件
    private String name;
    private String role;
    private String dept;
}
