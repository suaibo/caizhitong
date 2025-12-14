package com.mint.caizhitong.service;

import com.mint.caizhitong.domain.CategoryDTO;
import com.mint.caizhitong.domain.CategoryTreeDTO;
import com.mint.caizhitong.model.MaterialCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 鏉愭枡绫荤洰鏍 服务类
 * </p>
 *
 * @author mint
 * @since 2025-11-25
 */
public interface IMaterialCategoryService extends IService<MaterialCategory> {

    List<CategoryTreeDTO> getCategoryTree();

    @Transactional
    void saveNewCategory(CategoryDTO categoryDTO);

    @Transactional
    void updateCategory(Long id, CategoryDTO request);

    void removeCategory(Long id);

}
