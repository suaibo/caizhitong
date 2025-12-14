package com.mint.caizhitong.domain;

import lombok.Data;

/**
 * @author Mint
 */
@Data
public class MaterialItemQueryDTO {
    private Integer page = 1;
    private Integer pageSize = 10;
    private String keyword; // 名称/编码/品牌/型号模糊查询
    private Long categoryId; // 类目过滤
    private Boolean warnOnly; // 仅查看低于安全库存的材料
}
