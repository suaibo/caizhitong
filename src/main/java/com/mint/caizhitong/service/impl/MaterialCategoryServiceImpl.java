package com.mint.caizhitong.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mint.caizhitong.common.exception.BusinessConflictException;
import com.mint.caizhitong.common.exception.ResourceNotFoundException;
import com.mint.caizhitong.domain.CategoryDTO;
import com.mint.caizhitong.domain.CategoryTreeDTO;
import com.mint.caizhitong.mapper.MaterialItemMapper;
import com.mint.caizhitong.model.MaterialCategory;
import com.mint.caizhitong.mapper.MaterialCategoryMapper;
import com.mint.caizhitong.service.IMaterialCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 鏉愭枡绫荤洰鏍 服务实现类
 * </p>
 *
 * @author mint
 * @since 2025-11-25
 */
@Service
public class MaterialCategoryServiceImpl extends ServiceImpl<MaterialCategoryMapper, MaterialCategory> implements IMaterialCategoryService {

    private MaterialItemMapper itemMapper;

    public MaterialCategoryServiceImpl(MaterialItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    @Override
    public List<CategoryTreeDTO> getCategoryTree() {
        // 1. 查询所有类目数据（扁平列表）
        List<MaterialCategory> allCategories = baseMapper.selectList(null);

        // 转换为 DTO 列表，方便后续操作
        List<CategoryTreeDTO> allCategoryDTOs = allCategories.stream()
                .map(CategoryTreeDTO::fromEntity)
                .toList();

        // 2. 将扁平列表转换为 Map，Key 为 Category ID，方便快速查找
        Map<Long, CategoryTreeDTO> categoryMap = allCategoryDTOs.stream()
                .collect(Collectors.toMap(CategoryTreeDTO::getId, Function.identity()));

        // 3. 构建树形结构：找出根节点并挂载子节点
        List<CategoryTreeDTO> rootNodes = new ArrayList<>();

        for (CategoryTreeDTO current : allCategoryDTOs) {
            // 获取当前节点的父ID
            Long parentId = current.getParentId();

            if (parentId == null) {
                // 3a. 如果 parentId 为 NULL，则为根节点
                rootNodes.add(current);
            } else {
                // 3b. 查找父节点
                CategoryTreeDTO parent = categoryMap.get(parentId);

                if (parent != null) {
                    // 3c. 将当前节点作为子节点挂载到父节点的 children 列表中
                    parent.getChildren().add(current);
                }
                // 注意：如果 parent == null，说明数据有脏数据（父节点不存在），
                // 此时 current 节点会被跳过，不会出现在最终树中。
            }
        }

        // 4. 返回根节点列表
        return rootNodes;
    }
    // 假设您有一个获取当前登录用户ID的工具
    private Long getCurrentUserId() {
        // 实际应用中通过 Sa-Token 或 Spring Security 获取
        return StpUtil.getLoginIdAsLong();
    }

    @Transactional
    @Override
    public void saveNewCategory(CategoryDTO categoryDTO) {

        // --- 1. 校验父类目是否存在（如果 parentId 不为空）---
        if (categoryDTO.getParentId() != null) {
            MaterialCategory parent = baseMapper.selectById(categoryDTO.getParentId());
            if (parent == null) {
                // 抛出异常，让全局异常处理器返回 Result.error40004()
                throw new ResourceNotFoundException("父类目ID [" + categoryDTO.getParentId() + "] 不存在。");
            }
        }

        // --- 2. 校验唯一约束：parent_id + name 是否重复 ---
        // 对应您表中的 UNIQUE KEY uk_parent_name (parent_id, name)
        boolean exists = this.lambdaQuery()
                .eq(MaterialCategory::getParentId, categoryDTO.getParentId())
                .eq(MaterialCategory::getName, categoryDTO.getName())
                .exists();

        if (exists) {
            // 抛出业务异常，让全局异常处理器返回自定义错误码
            throw new BusinessConflictException("类目名称 [" + categoryDTO.getName() + "] 在当前父类目下已存在。");
        }

        // --- 3. DTO 转 实体，并补全字段 ---
        MaterialCategory entity = convertToEntity(categoryDTO);
        Long currentUserId = getCurrentUserId();

        entity.setCreateBy(currentUserId);
        entity.setUpdateBy(currentUserId);
        // create_time 和 update_time 由数据库 DEFAULT/ON UPDATE 自动设置

        // --- 4. 数据库持久化 ---
        // 使用 MyBatis-Plus 的 save 方法
        boolean success = this.save(entity);

        if (!success) {
            throw new RuntimeException("数据库保存失败，请联系管理员。");
        }
    }

    // 示例转换方法
    private MaterialCategory convertToEntity(CategoryDTO request) {
        MaterialCategory entity = new MaterialCategory();
        entity.setParentId(request.getParentId());
        entity.setName(request.getName());
        entity.setUnit(request.getUnit());
        entity.setSafeStock(request.getSafeStock());
        return entity;
    }

    @Transactional
    @Override
    public void updateCategory(Long id, CategoryDTO request) {

        // ========== 1. 查询旧数据 ==========
        MaterialCategory old = baseMapper.selectById(id);
        if (old == null) {
            throw new ResourceNotFoundException("类目ID [" + id + "] 不存在");
        }

        // ========== 2. 计算新字段值（合并） ==========
        Long newParentId = request.getParentId() != null ? request.getParentId() : old.getParentId();
        String newName     = request.getName() != null ? request.getName() : old.getName();
        String newUnit     = request.getUnit() != null ? request.getUnit() : old.getUnit();
        BigDecimal newSafeStock = request.getSafeStock() != null ? request.getSafeStock() : old.getSafeStock();

        // ========== 3. 校验父类目是否存在 ==========
        if (newParentId != null) {
            if (baseMapper.selectById(newParentId) == null) {
                throw new BusinessConflictException("父类目 [" + newParentId + "] 不存在");
            }
        }

        // ========== 4. 校验唯一性（parent_id + name） ==========
        boolean parentOrNameChanged =
                !Objects.equals(newParentId, old.getParentId()) ||
                        !Objects.equals(newName, old.getName());

        if (parentOrNameChanged) {
            boolean exists = lambdaQuery()
                    .eq(MaterialCategory::getParentId, newParentId)
                    .eq(MaterialCategory::getName, newName)
                    .ne(MaterialCategory::getId, id)
                    .exists();

            if (exists) {
                throw new BusinessConflictException(
                        "同一父类目下名称 [" + newName + "] 已存在");
            }
        }

        // ========== 5. 校验循环依赖 ==========
        if (!Objects.equals(newParentId, old.getParentId())) {
            if (isDescendant(newParentId, id)) {
                throw new BusinessConflictException("不能将类目设置为自己的子类目（循环依赖）");
            }
        }

        // ========== 6. 构造更新对象（只更新变化的字段） ==========
        MaterialCategory update = new MaterialCategory();
        update.setId(id);  // 必须有

        if (!Objects.equals(newParentId, old.getParentId())) {
            update.setParentId(newParentId);
        }
        if (!Objects.equals(newName, old.getName())) {
            update.setName(newName);
        }
        if (!Objects.equals(newUnit, old.getUnit())) {
            update.setUnit(newUnit);
        }
        if (!Objects.equals(newSafeStock, old.getSafeStock())) {
            update.setSafeStock(newSafeStock);
        }

        update.setUpdateBy(getCurrentUserId());

        // ========== 7. 执行更新 ==========
        this.updateById(update);
    }

    /**
     * 判断 nodeId 是否是 ancestorId 的子孙节点
     * 即：沿着 nodeId 的 parentId 逐层向上查，是否能找到 ancestorId
     */
    private boolean isDescendant(Long nodeId, Long ancestorId) {
        if (nodeId == null) {
            return false;
        }

        Long current = nodeId;

        while (current != null) {
            if (current.equals(ancestorId)) {
                return true;
            }

            MaterialCategory mc = baseMapper.selectById(current);
            if (mc == null) {
                return false;
            }
            current = mc.getParentId();
        }
        return false;
    }

    @Transactional
    @Override
    public void removeCategory(Long id) {

        // 1. 是否存在
        MaterialCategory old = baseMapper.selectById(id);
        if (old == null) {
            throw new ResourceNotFoundException("类目ID [" + id + "] 不存在。");
        }

        // 2. 是否有子类目（必须阻止删除）
        boolean hasChildren = lambdaQuery()
                .eq(MaterialCategory::getParentId, id)
                .exists();

        if (hasChildren) {
            throw new BusinessConflictException(
                    "类目 [" + id + "] 下存在子类目，无法删除。请先删除子类目。"
            );
        }

        // 3. 是否有关联物料（可提前校验，可选）
        boolean hasItems = itemMapper.existsByCategoryId(id);
        if (hasItems) {
            throw new BusinessConflictException(
                    "类目 [" + id + "] 下存在物料，请先删除相关物料。"
            );
        }

        // 4. 执行删除
        try {
            baseMapper.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // 数据库由于 ON DELETE RESTRICT 抛出的异常
            throw new BusinessConflictException(
                    "类目 [" + id + "] 下存在物料，无法删除。请先删除相关物料呀。"
            );
        }
    }

}
