package com.mint.caizhitong.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mint.caizhitong.common.exception.BusinessConflictException;
import com.mint.caizhitong.common.exception.ResourceNotFoundException;
import com.mint.caizhitong.common.resp.PageVo;
import com.mint.caizhitong.domain.MaterialItemQueryDTO;
import com.mint.caizhitong.domain.MaterialListDTO;
import com.mint.caizhitong.domain.materialrequest.MaterialCreateRequest;
import com.mint.caizhitong.domain.materialrequest.MaterialUpdateRequest;
import com.mint.caizhitong.mapper.MaterialCategoryMapper;
import com.mint.caizhitong.mapper.StockBatchMapper;
import com.mint.caizhitong.model.MaterialItem;
import com.mint.caizhitong.mapper.MaterialItemMapper;
import com.mint.caizhitong.service.IMaterialItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * <p>
 * 鏉愭枡鏄庣粏琛 服务实现类
 * </p>
 *
 * @author mint
 * @since 2025-11-25
 */
@Service
public class MaterialItemServiceImpl extends ServiceImpl<MaterialItemMapper, MaterialItem> implements IMaterialItemService {

    private MaterialItemMapper materialItemMapper;

    private MaterialCategoryMapper categoryMapper;

    private StockBatchMapper stockBatchMapper;

    public MaterialItemServiceImpl(MaterialItemMapper materialItemMapper
            , MaterialCategoryMapper categoryMapper
            , StockBatchMapper stockBatchMapper) {
        this.materialItemMapper = materialItemMapper;
        this.categoryMapper = categoryMapper;
        this.stockBatchMapper = stockBatchMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public PageVo<MaterialListDTO> pageQueryMaterials(MaterialItemQueryDTO request) {

        // 1. 构造分页对象，泛型为 DTO
        IPage<MaterialListDTO> page = new Page<>(request.getPage(), request.getPageSize());

        // 2. 调用 Mapper 自定义分页查询方法
        // MyBatis-Plus 会自动将分页参数和 WHERE/HAVING 条件应用到 SQL 中
        IPage<MaterialListDTO> resultPage = this.baseMapper.selectPageQuery(page, request);

        // 3. 封装结果
        return PageVo.of(resultPage.getRecords(), resultPage.getTotal());
    }

    @Transactional
    @Override
    public void createMaterial(MaterialCreateRequest request) {
        // 1. 校验类目存在性
        if (categoryMapper.selectById(request.getCategoryId()) == null) {
            throw new ResourceNotFoundException("类目ID [" + request.getCategoryId() + "] 不存在。");
        }

        // 2. 校验编码唯一性
        boolean codeExists = this.lambdaQuery()
                .eq(MaterialItem::getCode, request.getCode())
                .exists();
        if (codeExists) {
            throw new BusinessConflictException("物料编码 [" + request.getCode() + "] 已存在。");
        }

        // 3. DTO 转 Entity，并设置创建人
        MaterialItem entity = new MaterialItem();
        BeanUtils.copyProperties(request, entity);
        entity.setCreateBy(StpUtil.getLoginIdAsLong());

        // 4. 保存
        this.save(entity);
    }

    @Transactional
    @Override
    public void updateMaterial(Long id, MaterialUpdateRequest request) {
        // 1. 资源存在性校验
        MaterialItem oldEntity = baseMapper.selectById(id);
        if (oldEntity == null) {
            throw new ResourceNotFoundException("物料ID [" + id + "] 不存在。");
        }

        // 2. 校验编码唯一性 (如果 code 被修改)
        if (request.getCode() != null && !request.getCode().equals(oldEntity.getCode())) {
            boolean codeExists = this.lambdaQuery()
                    .eq(MaterialItem::getCode, request.getCode())
                    .ne(MaterialItem::getId, id) // 排除自身
                    .exists();
            if (codeExists) {
                throw new BusinessConflictException("物料编码 [" + request.getCode() + "] 已被其他物料使用。");
            }
        }

        // 3. 校验 CategoryId 存在性 (如果 categoryId 被修改)
        if (request.getCategoryId() != null && !request.getCategoryId().equals(oldEntity.getCategoryId())) {
            if (categoryMapper.selectById(request.getCategoryId()) == null) {
                throw new ResourceNotFoundException("新的类目ID [" + request.getCategoryId() + "] 不存在。");
            }
        }

        // 4. 构造更新对象 (只设置非空字段)
        MaterialItem updateEntity = new MaterialItem();
        updateEntity.setId(id);

        BeanUtils.copyProperties(request, updateEntity);
        // 5. 设置更新人
        updateEntity.setUpdateBy(StpUtil.getLoginIdAsLong());

        // 6. 执行更新
        this.updateById(updateEntity);
    }

    @Transactional
    @Override
    public void removeMaterial(Long id) {
        // 1. 存在性校验
        if (baseMapper.selectById(id) == null) {
            throw new ResourceNotFoundException("物料ID [" + id + "] 不存在。");
        }

        // 2. 业务校验：检查是否存在未清零的库存批次 (核心)
        // SQL: SELECT SUM(quantity) FROM stock_batch WHERE item_id = #{id}
        BigDecimal totalStock = stockBatchMapper.selectTotalQuantityByItemId(id);

        if (totalStock != null && totalStock.compareTo(BigDecimal.ZERO) > 0) {
            // 存在库存
            throw new BusinessConflictException("物料 [" + id + "] 存在库存批次，不可删除。");
        }

        // 3. 执行删除
        // 如果存在批次记录但 quantity SUM = 0，则可以删除。
        // 由于 stock_batch 对 item_id 设置了 ON DELETE CASCADE，删除 item 也会删除 stock_batch 记录。
        this.removeById(id);
    }
}
