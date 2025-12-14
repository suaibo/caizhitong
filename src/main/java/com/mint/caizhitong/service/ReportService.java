package com.mint.caizhitong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mint.caizhitong.domain.req.ProjectExpenseReq;
import com.mint.caizhitong.domain.req.TermConsumeReq;
import com.mint.caizhitong.domain.req.UserConsumeReq;
import com.mint.caizhitong.domain.vo.ProjectExpenseVO;
import com.mint.caizhitong.domain.vo.TermConsumeVO;
import com.mint.caizhitong.domain.vo.UserConsumeVO;
import com.mint.caizhitong.model.MaterialCategory;
import com.mint.caizhitong.model.MaterialItem;
import com.mint.caizhitong.model.StockBatch;
import com.mint.caizhitong.model.StockTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final IStockTransactionService stockTransactionService;
    private final IStockBatchService stockBatchService;
    private final IMaterialItemService materialItemService;
    private final IMaterialCategoryService materialCategoryService;

    /**
     * 6.1 学期耗材汇总 (按课程/Item聚合)
     */
    public List<TermConsumeVO> getTermConsumeReport(TermConsumeReq req) {
        // 1. 查流水 (TxnType = OUT)
        LambdaQueryWrapper<StockTransaction> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StockTransaction::getTxnType, "OUT");

        // 筛选学期 (基于 Usage 字段模糊匹配，格式参考 4.3.1)
        if (StringUtils.hasText(req.getTerm())) {
            wrapper.like(StockTransaction::getUsage, "学期:" + req.getTerm());
        }
        // 筛选课程
        if (StringUtils.hasText(req.getCourseName())) {
            wrapper.like(StockTransaction::getUsage, "课程:" + req.getCourseName());
        }
        // 筛选教师
        if (req.getTeacherId() != null) {
            wrapper.eq(StockTransaction::getUserId, req.getTeacherId());
        }

        List<StockTransaction> txns = stockTransactionService.list(wrapper);
        if (txns.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 准备基础数据 (Batch -> Item)
        List<Long> batchIds = txns.stream().map(StockTransaction::getBatchId).toList();
        Map<Long, StockBatch> batchMap = stockBatchService.listByIds(batchIds).stream()
                .collect(Collectors.toMap(StockBatch::getId, Function.identity()));

        Set<Long> itemIds = batchMap.values().stream().map(StockBatch::getItemId).collect(Collectors.toSet());
        Map<Long, MaterialItem> itemMap = materialItemService.listByIds(itemIds).stream()
                .collect(Collectors.toMap(MaterialItem::getId, Function.identity()));

        // 还需要查 Category 获取单位
        Set<Long> categoryIds = itemMap.values().stream().map(MaterialItem::getCategoryId).collect(Collectors.toSet());
        Map<Long, MaterialCategory> categoryMap = materialCategoryService.listByIds(categoryIds).stream()
                .collect(Collectors.toMap(MaterialCategory::getId, Function.identity()));

        // 3. 内存聚合 (Group By ItemId)
        Map<Long, BigDecimal> qtySumMap = new HashMap<>();

        for (StockTransaction txn : txns) {
            StockBatch batch = batchMap.get(txn.getBatchId());
            if (batch == null) {
                continue;
            }

            // 注意：出库记录的 qty 是负数，统计消耗量取绝对值
            BigDecimal absQty = txn.getQty().abs();
            qtySumMap.merge(batch.getItemId(), absQty, BigDecimal::add);
        }

        // 4. 组装 VO
        List<TermConsumeVO> result = new ArrayList<>();
        qtySumMap.forEach((itemId, totalQty) -> {
            MaterialItem item = itemMap.get(itemId);
            if (item != null) {
                TermConsumeVO vo = new TermConsumeVO();
                vo.setItemCode(item.getCode());
                vo.setItemName(item.getName());
                vo.setSpec(item.getSpec());
                vo.setUnitPrice(item.getUnitPrice());
                vo.setTotalQty(totalQty);
                // 总金额 = 单价 * 数量
                vo.setTotalAmount(totalQty.multiply(item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO));

                MaterialCategory cat = categoryMap.get(item.getCategoryId());
                if (cat != null) {
                    vo.setUnit(cat.getUnit());
                }

                result.add(vo);
            }
        });

        return result;
    }

    /**
     * 6.2 经费对应表 (按项目号)
     */
    public List<ProjectExpenseVO> getProjectExpenseReport(ProjectExpenseReq req) {
        // 1. 查流水
        LambdaQueryWrapper<StockTransaction> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StockTransaction::getTxnType, "OUT");
        wrapper.eq(StockTransaction::getProjectNo, req.getProjectNo()); // 必填匹配

        if (req.getDateFrom() != null) {
            wrapper.ge(StockTransaction::getCreateTime, req.getDateFrom().atStartOfDay());
        }
        if (req.getDateTo() != null) {
            wrapper.le(StockTransaction::getCreateTime, req.getDateTo().atTime(23, 59, 59));
        }

        List<StockTransaction> txns = stockTransactionService.list(wrapper);
        if (txns.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 查关联数据
        List<Long> batchIds = txns.stream().map(StockTransaction::getBatchId).toList();
        Map<Long, StockBatch> batchMap = stockBatchService.listByIds(batchIds).stream()
                .collect(Collectors.toMap(StockBatch::getId, Function.identity()));

        Set<Long> itemIds = batchMap.values().stream().map(StockBatch::getItemId).collect(Collectors.toSet());
        Map<Long, MaterialItem> itemMap = materialItemService.listByIds(itemIds).stream()
                .collect(Collectors.toMap(MaterialItem::getId, Function.identity()));

        // 3. 组装 VO
        return txns.stream().map(txn -> {
            ProjectExpenseVO vo = new ProjectExpenseVO();
            vo.setCreateTime(txn.getCreateTime());
            vo.setProjectNo(txn.getProjectNo());
            vo.setUsage(txn.getUsage());
            vo.setQty(txn.getQty().abs()); // 取绝对值

            StockBatch batch = batchMap.get(txn.getBatchId());
            if (batch != null) {
                MaterialItem item = itemMap.get(batch.getItemId());
                if (item != null) {
                    vo.setItemName(item.getName());
                    vo.setUnitPrice(item.getUnitPrice());
                    // 计算该笔流水金额
                    vo.setAmount(vo.getQty().multiply(item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO));
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 6.3 个人领用明细
     */
    public List<UserConsumeVO> getUserConsumeReport(UserConsumeReq req) {
        // 1. 查流水
        LambdaQueryWrapper<StockTransaction> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StockTransaction::getTxnType, "OUT");
        wrapper.eq(StockTransaction::getUserId, req.getUserId());

        if (req.getDateFrom() != null) {
            wrapper.ge(StockTransaction::getCreateTime, req.getDateFrom().atStartOfDay());
        }
        if (req.getDateTo() != null) {
            wrapper.le(StockTransaction::getCreateTime, req.getDateTo().atTime(23, 59, 59));
        }

        wrapper.orderByDesc(StockTransaction::getCreateTime);

        List<StockTransaction> txns = stockTransactionService.list(wrapper);
        if (txns.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 关联查询 (同上，略微重复但结构清晰)
        List<Long> batchIds = txns.stream().map(StockTransaction::getBatchId).toList();
        Map<Long, StockBatch> batchMap = stockBatchService.listByIds(batchIds).stream()
                .collect(Collectors.toMap(StockBatch::getId, Function.identity()));

        Set<Long> itemIds = batchMap.values().stream().map(StockBatch::getItemId).collect(Collectors.toSet());
        Map<Long, MaterialItem> itemMap = materialItemService.listByIds(itemIds).stream()
                .collect(Collectors.toMap(MaterialItem::getId, Function.identity()));

        // 3. 组装
        return txns.stream().map(txn -> {
            UserConsumeVO vo = new UserConsumeVO();
            vo.setCreateTime(txn.getCreateTime());
            vo.setQty(txn.getQty().abs());
            vo.setUsage(txn.getUsage());

            StockBatch batch = batchMap.get(txn.getBatchId());
            if (batch != null) {
                vo.setBatchCode(batch.getBatchCode());
                MaterialItem item = itemMap.get(batch.getItemId());
                if (item != null) {
                    vo.setItemName(item.getName());
                    vo.setSpec(item.getSpec());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }
}
