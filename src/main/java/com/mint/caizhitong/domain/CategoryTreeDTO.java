package com.mint.caizhitong.domain;

import com.mint.caizhitong.model.MaterialCategory;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mint
 */
@Data
public class CategoryTreeDTO {
    private Long id;
    private Long parentId;
    private String name;
    private String unit;
    private BigDecimal safeStock;

    // 递归包含子节点
    private List<CategoryTreeDTO> children = new ArrayList<>();

    // 静态方法：从实体类转换
    public static CategoryTreeDTO fromEntity(MaterialCategory entity) {
        CategoryTreeDTO dto = new CategoryTreeDTO();
        dto.setId(entity.getId());
        dto.setParentId(entity.getParentId());
        dto.setName(entity.getName());
        dto.setUnit(entity.getUnit());
        dto.setSafeStock(entity.getSafeStock());
        return dto;
    }
}
