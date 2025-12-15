package com.mint.caizhitong.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mint.caizhitong.common.exception.BusinessConflictException;
import com.mint.caizhitong.common.exception.ResourceNotFoundException;
import com.mint.caizhitong.common.resp.PageVo;
import com.mint.caizhitong.domain.MaterialItemQueryDTO;
import com.mint.caizhitong.domain.MaterialListDTO;
import com.mint.caizhitong.domain.materialrequest.MaterialCreateRequest;
import com.mint.caizhitong.domain.materialrequest.MaterialUpdateRequest;
import com.mint.caizhitong.domain.stockvo.StockBatchVO;
import com.mint.caizhitong.domain.vo.MaterialDetailVO;
import com.mint.caizhitong.mapper.MaterialCategoryMapper;
import com.mint.caizhitong.mapper.StockBatchMapper;
import com.mint.caizhitong.model.MaterialCategory;
import com.mint.caizhitong.model.MaterialItem;
import com.mint.caizhitong.mapper.MaterialItemMapper;
import com.mint.caizhitong.model.StockBatch;
import com.mint.caizhitong.service.IMaterialItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public MaterialDetailVO getMaterialDetail(Long id) {
        // 1. 查询材料基础信息
        MaterialItem item = baseMapper.selectById(id);
        if (item == null) {
            throw new ResourceNotFoundException("物料ID [" + id + "] 不存在。");
        }

        // 2. 查询所属类目信息 (获取单位、安全库存、类目名称)
        MaterialCategory category = categoryMapper.selectById(item.getCategoryId());

        // 3. 查询该材料下的所有库存批次 (按过期时间排序)
        List<StockBatch> batches = stockBatchMapper.selectList(
                Wrappers.<StockBatch>lambdaQuery()
                        .eq(StockBatch::getItemId, id)
                        .gt(StockBatch::getQuantity, BigDecimal.ZERO) // 仅展示有库存的批次，根据需求调整
                        .orderByAsc(StockBatch::getExpireDate)
        );

        // 4. 计算总库存
        BigDecimal totalStock = batches.stream()
                .map(StockBatch::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. 组装 VO
        MaterialDetailVO vo = new MaterialDetailVO();

        // 5.1 复制材料属性
        BeanUtils.copyProperties(item, vo);

        // 5.2 填充类目属性
        if (category != null) {
            vo.setCategoryName(category.getName());
            vo.setUnit(category.getUnit());
            vo.setSafeStock(category.getSafeStock());
        }

        // 5.3 填充库存统计
        vo.setTotalStock(totalStock);

        // 5.4 转换并填充批次列表
        List<StockBatchVO> batchVOs = batches.stream().map(batch -> {
            StockBatchVO batchVO = new StockBatchVO();
            BeanUtils.copyProperties(batch, batchVO);
            return batchVO;
        }).collect(Collectors.toList());

        vo.setBatches(batchVOs);

        return vo;
    }
}
