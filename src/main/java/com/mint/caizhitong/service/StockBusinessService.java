package com.mint.caizhitong.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mint.caizhitong.common.exception.BusinessConflictException;
import com.mint.caizhitong.common.exception.ResourceNotFoundException;
import com.mint.caizhitong.domain.stockrequest.*;
import com.mint.caizhitong.domain.stockvo.*;
import com.mint.caizhitong.model.MaterialCategory;
import com.mint.caizhitong.model.MaterialItem;
import com.mint.caizhitong.model.StockBatch;
import com.mint.caizhitong.model.StockTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockBusinessService {

    private final IStockBatchService stockBatchService;
    private final IStockTransactionService stockTransactionService;
    private final IMaterialItemService materialItemService;
    private final IMaterialCategoryService materialCategoryService;

    @Transactional(rollbackFor = Exception.class)
    public Long processStockIn(StockInReq req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BusinessConflictException("入库明细不能为空");
        }

        Long lastTxnId = 0L;

        for (StockInReq.StockInItemReq item : req.getItems()) {
            // 1. 校验批次是否存在 (使用生成的 Service 提供的 count 方法)
            long count = stockBatchService.count(Wrappers.<StockBatch>lambdaQuery()
                    .eq(StockBatch::getItemId, item.getItemId())
                    .eq(StockBatch::getBatchCode, item.getBatchCode()));

            if (count > 0) {
                throw new BusinessConflictException("批次号冲突: " + item.getBatchCode());
            }

            // 2. 组装批次对象
            StockBatch batch = new StockBatch();
            batch.setItemId(item.getItemId());
            batch.setBatchCode(item.getBatchCode());
            batch.setQuantity(item.getQuantity());
            batch.setExpireDate(item.getExpireDate());
            batch.setBarcode(item.getBarcode());

            // 使用生成的 Service 保存
            stockBatchService.save(batch);

            // 3. 组装流水对象
            StockTransaction txn = new StockTransaction();
            txn.setBatchId(batch.getId()); // 获取回填的ID
            txn.setTxnType("IN");
            txn.setQty(item.getQuantity());
            txn.setUserId(req.getOperatorId());
            txn.setProjectNo(req.getContractNo());

            // 拼接 Usage
            String usageInfo = String.format("采购入库 | 供应商:%s | 库位:%s | 备注:%s",
                    req.getSupplier(),
                    item.getLocation() != null ? item.getLocation() : "",
                    req.getRemark());
            txn.setUsage(usageInfo.length() > 500 ? usageInfo.substring(0, 500) : usageInfo);
            txn.setCreateTime(LocalDateTime.now());

            // 使用生成的 Service 保存
            stockTransactionService.save(txn);

            lastTxnId = txn.getId();
        }

        return lastTxnId;
    }

    public Page<StockTransaction> pageStockIn(StockPageReq req) {
        // 1. 构建分页对象
        Page<StockTransaction> page = new Page<>(req.getPage(), req.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<StockTransaction> wrapper = Wrappers.lambdaQuery();

        // 核心条件：只查“入库”类型
        wrapper.eq(StockTransaction::getTxnType, "IN");

        // 动态条件：经办人
        wrapper.eq(req.getOperatorId() != null, StockTransaction::getUserId, req.getOperatorId());

        // 动态条件：时间范围 (create_time >= dateFrom 00:00:00)
        if (req.getDateFrom() != null) {
            wrapper.ge(StockTransaction::getCreateTime, req.getDateFrom().atStartOfDay());
        }
        // 动态条件：时间范围 (create_time <= dateTo 23:59:59)
        if (req.getDateTo() != null) {
            wrapper.le(StockTransaction::getCreateTime, req.getDateTo().atTime(23, 59, 59));
        }

        // 动态条件：供应商
        // 因为供应商存放在 usage 字段中，所以这里使用 like 查询
        if (StringUtils.hasText(req.getSupplier())) {
            wrapper.like(StockTransaction::getUsage, req.getSupplier());
        }

        // 排序：按时间倒序
        wrapper.orderByDesc(StockTransaction::getCreateTime);

        // 3. 执行查询 (直接调用 MP 生成的 Service 即可)
        return stockTransactionService.page(page, wrapper);
    }

    /**
     * 4.1.3 查看入库单详情
     */
    public StockInDetailVO getStockInDetail(Long id) {
        // 1. 查流水
        StockTransaction txn = stockTransactionService.getById(id);
        if (txn == null) {
            throw new ResourceNotFoundException("未找到入库记录");
        }

        // 2. 查批次
        StockBatch batch = stockBatchService.getById(txn.getBatchId());

        // 3. 查物料 (直接查 MaterialItem 即可拿到 Name 和 Code)
        MaterialItem item = null;
        if (batch != null) {
            item = materialItemService.getById(batch.getItemId());
        }

        // 4. 组装 VO
        StockInDetailVO vo = new StockInDetailVO();

        // 4.1 流水数据
        vo.setId(txn.getId());
        vo.setTxnType(txn.getTxnType());
        vo.setQty(txn.getQty());
        vo.setOperatorId(txn.getUserId());
        vo.setContractNo(txn.getProjectNo());
        vo.setUsageInfo(txn.getUsage());
        vo.setCreateTime(txn.getCreateTime());

        // 4.2 批次数据
        if (batch != null) {
            vo.setBatchCode(batch.getBatchCode());
            vo.setExpireDate(batch.getExpireDate());
            vo.setBarcode(batch.getBarcode());
            vo.setStockQuantity(batch.getQuantity());
        }

        // 4.3 物料数据
        if (item != null) {
            vo.setItemId(item.getId());
            vo.setMaterialName(item.getName()); // 【直接获取】
            vo.setMaterialCode(item.getCode()); // 【直接获取】
            vo.setBrand(item.getBrand());
            vo.setModel(item.getModel());
            vo.setSpec(item.getSpec());
        }

        return vo;
    }
    /**
     * 4.2.1 按材料汇总库存 (含预警逻辑)
     */
    public List<StockSummaryVO> getStockSummary(StockSummaryReq req) {
        // 1. 查询符合条件的材料列表 (MaterialItem)
        LambdaQueryWrapper<MaterialItem> itemWrapper = Wrappers.lambdaQuery();
        itemWrapper.eq(req.getItemId() != null, MaterialItem::getId, req.getItemId());
        itemWrapper.eq(req.getCategoryId() != null, MaterialItem::getCategoryId, req.getCategoryId());
        List<MaterialItem> items = materialItemService.list(itemWrapper);

        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取所有 itemId 和 categoryId，用于批量查询后续数据
        List<Long> itemIds = items.stream().map(MaterialItem::getId).toList();
        Set<Long> categoryIds = items.stream().map(MaterialItem::getCategoryId).collect(Collectors.toSet());

        // 2. 批量查询所有相关库存批次，并在内存中分组求和
        // 查询 stock_batch 表中所有属于这些 item 的记录
        List<StockBatch> allBatches = stockBatchService.list(Wrappers.<StockBatch>lambdaQuery()
                .in(StockBatch::getItemId, itemIds));

        // Map<ItemId, TotalQuantity>
        Map<Long, BigDecimal> stockMap = allBatches.stream()
                .collect(Collectors.groupingBy(
                        StockBatch::getItemId,
                        Collectors.reducing(BigDecimal.ZERO, StockBatch::getQuantity, BigDecimal::add)
                ));

        // 3. 批量查询类目信息 (获取安全库存 safe_stock 和 单位 unit)
        List<MaterialCategory> categories = materialCategoryService.listByIds(categoryIds);
        Map<Long, MaterialCategory> categoryMap = categories.stream()
                .collect(Collectors.toMap(MaterialCategory::getId, Function.identity()));

        // 4. 组装 VO 并处理预警逻辑
        List<StockSummaryVO> resultList = new ArrayList<>();

        for (MaterialItem item : items) {
            StockSummaryVO vo = new StockSummaryVO();
            vo.setItemId(item.getId());
            vo.setMaterialName(item.getName());
            vo.setMaterialCode(item.getCode());
            vo.setSpec(item.getSpec());

            // 填充类目信息 (安全库存、单位)
            MaterialCategory cat = categoryMap.get(item.getCategoryId());
            BigDecimal safeStock = BigDecimal.ZERO;
            if (cat != null) {
                vo.setCategoryName(cat.getName());
                vo.setUnit(cat.getUnit());
                safeStock = cat.getSafeStock() != null ? cat.getSafeStock() : BigDecimal.ZERO;
                vo.setSafeStock(safeStock);
            }

            // 获取当前总库存 (如果没有批次，默认为 0)
            BigDecimal total = stockMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            vo.setTotalStock(total);

            // 判断预警: 总库存 < 安全库存
            boolean isWarning = total.compareTo(safeStock) < 0;
            vo.setWarning(isWarning);

            // 过滤逻辑: 如果前端只传了 warnOnly=true，则只添加预警的条目
            if (Boolean.TRUE.equals(req.getWarnOnly()) && !isWarning) {
                continue; // 跳过非预警的
            }

            resultList.add(vo);
        }

        return resultList;
    }

    /**
     * 4.2.2 查询某材料的所有批次
     */
    public List<StockBatchVO> getStockBatches(Long itemId) {
        // 查询该材料的所有批次，按过期时间升序排列 (FEFO: First Expired First Out)
        List<StockBatch> batches = stockBatchService.list(Wrappers.<StockBatch>lambdaQuery()
                .eq(StockBatch::getItemId, itemId)
                .gt(StockBatch::getQuantity, BigDecimal.ZERO) // 通常只看库存 > 0 的批次
                .orderByAsc(StockBatch::getExpireDate));

        // 转换为 VO
        return batches.stream().map(batch -> {
            StockBatchVO vo = new StockBatchVO();
            BeanUtils.copyProperties(batch, vo); // 属性拷贝
            return vo;
        }).collect(Collectors.toList());
    }
    /**
     * 4.3.1 领用出库
     */
    @Transactional(rollbackFor = Exception.class)
    public Long processStockOut(StockOutReq req) {
        // 1. 检查库存是否充足，利用数据库 CAS 机制或直接 Update 时的 Where 条件
        // 这里为了给用户明确提示，我们先查询
        StockBatch batch = stockBatchService.getById(req.getBatchId());
        if (batch == null) {
            throw new ResourceNotFoundException("找不到指定批次: " + req.getBatchId());
        }
        if (batch.getQuantity().compareTo(req.getQty()) < 0) {
            throw new BusinessConflictException("库存不足！当前剩余: " + batch.getQuantity());
        }

        // TODO: 在此处添加“个人月度领用上限”校验逻辑 (需要查询历史流水统计)

        // 2. 扣减库存 (并发安全写法)
        // UPDATE stock_batch SET quantity = quantity - req.qty WHERE id = batchId AND quantity >= req.qty
        boolean updateSuccess = stockBatchService.update(Wrappers.<StockBatch>lambdaUpdate()
                .setSql("quantity = quantity - " + req.getQty()) // SQL 片段
                .eq(StockBatch::getId, req.getBatchId())
                .ge(StockBatch::getQuantity, req.getQty())); // 双重保障，确保不扣成负数

        if (!updateSuccess) {
            throw new BusinessConflictException("库存扣减失败，可能库存不足或并发冲突");
        }

        // 3. 记录流水 (OUT)
        StockTransaction txn = new StockTransaction();
        txn.setBatchId(req.getBatchId());
        txn.setTxnType("OUT"); // 类型：出库
        txn.setQty(req.getQty().negate()); // 【关键】：出库记为负数
        txn.setUserId(req.getUserId());
        txn.setProjectNo(req.getProjectNo());

        // 拼接课程信息到 usage
        StringBuilder usageBuilder = new StringBuilder();
        if (req.getUsage() != null) {
            usageBuilder.append(req.getUsage());
        }
        if (req.getCourseTerm() != null) {
            usageBuilder.append(" | 学期:").append(req.getCourseTerm());
        }
        if (req.getCourseName() != null) {
            usageBuilder.append(" | 课程:").append(req.getCourseName());
        }
        String usageStr = usageBuilder.toString();
        txn.setUsage(usageStr.length() > 500 ? usageStr.substring(0, 500) : usageStr);

        txn.setCreateTime(LocalDateTime.now());

        stockTransactionService.save(txn);
        return txn.getId();
    }

    /**
     * 4.3.2 归还
     */
    @Transactional(rollbackFor = Exception.class)
    public void processStockReturn(StockReturnReq req) {
        // 1. 查询原始出库记录 (校验是否合法)
        StockTransaction originTxn = stockTransactionService.getById(req.getOriginTransactionId());
        if (originTxn == null) {
            throw new ResourceNotFoundException("原始领用记录不存在");
        }
        if (!"OUT".equals(originTxn.getTxnType())) {
            throw new BusinessConflictException("关联的记录不是出库记录，无法归还");
        }

        // 2. 增加库存
        // 归还通常回到原批次 (如果批次过期了逻辑可能不同，这里暂定回原批次)
        stockBatchService.update(Wrappers.<StockBatch>lambdaUpdate()
                .setSql("quantity = quantity + " + req.getQty())
                .eq(StockBatch::getId, originTxn.getBatchId()));

        // 3. 记录流水 (RETURN)
        StockTransaction txn = new StockTransaction();
        txn.setBatchId(originTxn.getBatchId());
        txn.setTxnType("RETURN");
        txn.setQty(req.getQty()); // 归还记为正数
        txn.setUserId(req.getUserId());
        txn.setProjectNo(originTxn.getProjectNo()); // 沿用原项目号
        txn.setUsage("归还操作 | 原流水ID:" + originTxn.getId() + " | 备注:" + req.getRemark());
        txn.setCreateTime(LocalDateTime.now());

        stockTransactionService.save(txn);
    }

    /**
     * 4.3.3 报废/损耗
     */
    @Transactional(rollbackFor = Exception.class)
    public void processStockScrap(StockScrapReq req) {
        // 1. 检查库存
        StockBatch batch = stockBatchService.getById(req.getBatchId());
        if (batch == null || batch.getQuantity().compareTo(req.getQty()) < 0) {
            throw new BusinessConflictException("库存不足，无法报损");
        }

        // 2. 扣减库存
        boolean updateSuccess = stockBatchService.update(Wrappers.<StockBatch>lambdaUpdate()
                .setSql("quantity = quantity - " + req.getQty())
                .eq(StockBatch::getId, req.getBatchId())
                .ge(StockBatch::getQuantity, req.getQty()));

        if (!updateSuccess) {
            throw new BusinessConflictException("报损失败");
        }

        // 3. 记录流水 (SCRAP)
        StockTransaction txn = new StockTransaction();
        txn.setBatchId(req.getBatchId());
        txn.setTxnType("SCRAP");
        txn.setQty(req.getQty().negate()); // 报损记为负数
        txn.setUserId(req.getUserId());

        // 拼接原因和图片链接
        StringBuilder sb = new StringBuilder();
        sb.append("报损原因:").append(req.getReason());
        if (req.getPhotoUrls() != null && !req.getPhotoUrls().isEmpty()) {
            sb.append(" | 图片:").append(String.join(",", req.getPhotoUrls()));
        }
        String usageStr = sb.toString();
        txn.setUsage(usageStr.length() > 500 ? usageStr.substring(0, 500) : usageStr);

        txn.setCreateTime(LocalDateTime.now());

        stockTransactionService.save(txn);
    }
    /**
     * 4.4.1 分页查询事务流水
     */
    public Page<StockTransactionVO> pageStockTransactions(StockTransactionPageReq req) {
        // --- 1. 预处理 itemId 过滤 ---
        List<Long> filterBatchIds = null;
        if (req.getItemId() != null) {
            // 如果指定了材料，先找出该材料所有的批次ID
            List<StockBatch> batches = stockBatchService.list(Wrappers.<StockBatch>lambdaQuery()
                    .select(StockBatch::getId) // 只查ID优化性能
                    .eq(StockBatch::getItemId, req.getItemId()));

            if (batches.isEmpty()) {
                // 该材料没有任何批次，自然也就没有流水，直接返回空页
                return new Page<>(req.getPage(), req.getPageSize());
            }
            filterBatchIds = batches.stream().map(StockBatch::getId).toList();
        }

        // --- 2. 构建流水查询条件 ---
        Page<StockTransaction> page = new Page<>(req.getPage(), req.getPageSize());
        LambdaQueryWrapper<StockTransaction> wrapper = Wrappers.lambdaQuery();

        // 批次ID过滤 (如果 filterBatchIds 不为空)
        if (filterBatchIds != null) {
            wrapper.in(StockTransaction::getBatchId, filterBatchIds);
        }

        // 其他常规过滤
        wrapper.eq(req.getUserId() != null, StockTransaction::getUserId, req.getUserId());
        wrapper.eq(StringUtils.hasText(req.getTxnType()), StockTransaction::getTxnType, req.getTxnType());
        wrapper.like(StringUtils.hasText(req.getProjectNo()), StockTransaction::getProjectNo, req.getProjectNo());

        // 时间范围
        if (req.getDateFrom() != null) {
            wrapper.ge(StockTransaction::getCreateTime, req.getDateFrom().atStartOfDay());
        }
        if (req.getDateTo() != null) {
            wrapper.le(StockTransaction::getCreateTime, req.getDateTo().atTime(23, 59, 59));
        }

        wrapper.orderByDesc(StockTransaction::getCreateTime);

        // 执行查询
        Page<StockTransaction> txnPage = stockTransactionService.page(page, wrapper);
        if (txnPage.getRecords().isEmpty()) {
            return new Page<>(req.getPage(), req.getPageSize());
        }

        // --- 3. 组装 VO (填充 itemId, batchCode, MaterialName) ---

        // 3.1 收集所有涉及的 batchId
        List<Long> batchIds = txnPage.getRecords().stream()
                .map(StockTransaction::getBatchId)
                .distinct()
                .toList();

        // 3.2 批量查询 Batch 信息
        Map<Long, StockBatch> batchMap = stockBatchService.listByIds(batchIds).stream()
                .collect(Collectors.toMap(StockBatch::getId, Function.identity()));

        // 3.3 收集所有涉及的 itemId
        Set<Long> itemIds = batchMap.values().stream()
                .map(StockBatch::getItemId)
                .collect(Collectors.toSet());

        // 3.4 批量查询 Item 信息
        Map<Long, MaterialItem> itemMap = Collections.emptyMap();
        if (!itemIds.isEmpty()) {
            itemMap = materialItemService.listByIds(itemIds).stream()
                    .collect(Collectors.toMap(MaterialItem::getId, Function.identity()));
        }

        // 3.5 转换结果列表
        Map<Long, MaterialItem> finalItemMap = itemMap; // 供 lambda 使用
        List<StockTransactionVO> voList = txnPage.getRecords().stream().map(txn -> {
            StockTransactionVO vo = new StockTransactionVO();
            BeanUtils.copyProperties(txn, vo); // 复制基础字段 (id, qty, usage, etc.)

            // 填充批次相关
            StockBatch batch = batchMap.get(txn.getBatchId());
            if (batch != null) {
                vo.setBatchCode(batch.getBatchCode());
                vo.setItemId(batch.getItemId());

                // 填充材料相关
                MaterialItem item = finalItemMap.get(batch.getItemId());
                if (item != null) {
                    vo.setMaterialName(item.getName());
                    vo.setMaterialCode(item.getCode());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        // 构造返回的 Page 对象
        Page<StockTransactionVO> resultPage = new Page<>(txnPage.getCurrent(), txnPage.getSize(), txnPage.getTotal());
        resultPage.setRecords(voList);

        return resultPage;
    }


    /**
     * 5.1 获取当前库存预警列表 (分页)
     */
    public Page<StockAlertVO> getAlertStockPage(int pageNo, int pageSize) {
        // 1. 复用现有的库存汇总逻辑，查询所有"仅预警"的数据
        StockSummaryReq summaryReq = new StockSummaryReq();
        summaryReq.setWarnOnly(true); // 关键：复用之前的逻辑，只查 isWarning=true 的
        List<StockSummaryVO> allAlerts = getStockSummary(summaryReq);

        // 2. 内存分页处理
        int total = allAlerts.size();

        // 计算起始和结束索引
        int start = (pageNo - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        List<StockAlertVO> resultList;
        if (start > total) {
            resultList = Collections.emptyList();
        } else {
            // 3. 截取当前页数据并转换为 AlertVO
            List<StockSummaryVO> subList = allAlerts.subList(start, end);
            resultList = subList.stream().map(summary -> {
                StockAlertVO vo = new StockAlertVO();
                vo.setItemId(summary.getItemId());
                vo.setName(summary.getMaterialName()); // 映射名称
                vo.setCategoryName(summary.getCategoryName());
                vo.setTotalStock(summary.getTotalStock());
                vo.setSafeStock(summary.getSafeStock());
                return vo;
            }).collect(Collectors.toList());
        }

        // 4. 封装成 MyBatis-Plus 的 Page 对象返回
        Page<StockAlertVO> page = new Page<>(pageNo, pageSize);
        page.setTotal(total);
        page.setRecords(resultList);

        return page;
    }
}

