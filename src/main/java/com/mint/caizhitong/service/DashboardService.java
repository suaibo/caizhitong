package com.mint.caizhitong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mint.caizhitong.domain.stockrequest.StockSummaryReq;
import com.mint.caizhitong.domain.stockvo.StockSummaryVO;
import com.mint.caizhitong.domain.vo.DashboardOverviewVO;
import com.mint.caizhitong.domain.vo.TopConsumeVO;
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

/**
 * @author Mint
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IMaterialItemService materialItemService;
    private final IStockTransactionService stockTransactionService;
    private final IStockBatchService stockBatchService;
    private final IMaterialCategoryService materialCategoryService;
    private final StockBusinessService stockBusinessService; // 复用预警逻辑

    /**
     * 7.1 仪表盘总览
     * @param term 学期 (可选，用于过滤消耗统计)
     */
    public DashboardOverviewVO getOverview(String term) {
        DashboardOverviewVO vo = new DashboardOverviewVO();

        // 1. 材料种类总数 (直接查库)
        long totalKinds = materialItemService.count();
        vo.setTotalMaterialKinds(totalKinds);

        // 2. 预警材料数 (复用 BusinessService 的逻辑)
        StockSummaryReq summaryReq = new StockSummaryReq();
        summaryReq.setWarnOnly(true);
        List<StockSummaryVO> warningList = stockBusinessService.getStockSummary(summaryReq);
        vo.setWarningItemCount(warningList.size());

        // 3. 计算消耗统计 (查流水 OUT)
        // 获取所有出库流水
        LambdaQueryWrapper<StockTransaction> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StockTransaction::getTxnType, "OUT");
        if (StringUtils.hasText(term)) {
            wrapper.like(StockTransaction::getUsage, "学期:" + term);
        }
        List<StockTransaction> txns = stockTransactionService.list(wrapper);

        // 如果没有流水，直接返回 0
        if (txns.isEmpty()) {
            vo.setSemesterConsumeCount(BigDecimal.ZERO);
            vo.setSemesterConsumeAmount(BigDecimal.ZERO);
            return vo;
        }

        // 统计总数量 (所有 txns 的 qty 绝对值之和)
        BigDecimal totalQty = txns.stream()
                .map(t -> t.getQty().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setSemesterConsumeCount(totalQty);

        // 统计总金额 (需要关联 Batch -> Item 获取单价)
        BigDecimal totalAmount = calculateTotalAmount(txns);
        vo.setSemesterConsumeAmount(totalAmount);

        return vo;
    }

    /**
     * 7.2 耗材 TOP10
     */
    public List<TopConsumeVO> getTopConsume(String term, int limit) {
        // 1. 获取出库流水
        LambdaQueryWrapper<StockTransaction> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StockTransaction::getTxnType, "OUT");
        if (StringUtils.hasText(term)) {
            wrapper.like(StockTransaction::getUsage, "学期:" + term);
        }
        List<StockTransaction> txns = stockTransactionService.list(wrapper);

        if (txns.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 准备基础数据 Map (Batch, Item, Category)
        List<Long> batchIds = txns.stream().map(StockTransaction::getBatchId).toList();
        Map<Long, StockBatch> batchMap = stockBatchService.listByIds(batchIds).stream()
                .collect(Collectors.toMap(StockBatch::getId, Function.identity()));

        Set<Long> itemIds = batchMap.values().stream().map(StockBatch::getItemId).collect(Collectors.toSet());
        Map<Long, MaterialItem> itemMap = materialItemService.listByIds(itemIds).stream()
                .collect(Collectors.toMap(MaterialItem::getId, Function.identity()));

        // 查 Category 获取单位
        Set<Long> categoryIds = itemMap.values().stream().map(MaterialItem::getCategoryId).collect(Collectors.toSet());
        Map<Long, MaterialCategory> categoryMap = materialCategoryService.listByIds(categoryIds).stream()
                .collect(Collectors.toMap(MaterialCategory::getId, Function.identity()));

        // 3. 按 ItemId 聚合计算 (TotalQty, TotalAmount)
        // Map<ItemId, TopConsumeVO>
        Map<Long, TopConsumeVO> aggregateMap = new HashMap<>();

        for (StockTransaction txn : txns) {
            StockBatch batch = batchMap.get(txn.getBatchId());
            if (batch == null) {
                continue;
            }
            MaterialItem item = itemMap.get(batch.getItemId());
            if (item == null) {
                continue;
            }

            TopConsumeVO vo = aggregateMap.getOrDefault(item.getId(), new TopConsumeVO());
            if (vo.getItemId() == null) {
                // 初始化 VO 信息
                vo.setItemId(item.getId());
                vo.setItemName(item.getName());
                vo.setItemCode(item.getCode());
                vo.setTotalQty(BigDecimal.ZERO);
                vo.setTotalAmount(BigDecimal.ZERO);

                MaterialCategory cat = categoryMap.get(item.getCategoryId());
                if (cat != null) {
                    vo.setCategoryName(cat.getName());
                    vo.setUnit(cat.getUnit());
                }
            }

            // 累加
            BigDecimal absQty = txn.getQty().abs();
            vo.setTotalQty(vo.getTotalQty().add(absQty));

            BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            vo.setTotalAmount(vo.getTotalAmount().add(absQty.multiply(price)));

            aggregateMap.put(item.getId(), vo);
        }

        // 4. 排序并截取 (按消耗数量 TotalQty 倒序，也可以按金额 TotalAmount)
        return aggregateMap.values().stream()
                .sorted(Comparator.comparing(TopConsumeVO::getTotalQty).reversed()) // 数量倒序
                .limit(limit)
                .collect(Collectors.toList());
    }

    // --- 辅助方法：计算流水总金额 ---
    private BigDecimal calculateTotalAmount(List<StockTransaction> txns) {
        // 为了性能，先批量查出所有涉及的 Batch 和 Item
        List<Long> batchIds = txns.stream().map(StockTransaction::getBatchId).distinct().toList();
        if (batchIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Map<Long, StockBatch> batchMap = stockBatchService.listByIds(batchIds).stream()
                .collect(Collectors.toMap(StockBatch::getId, Function.identity()));

        Set<Long> itemIds = batchMap.values().stream().map(StockBatch::getItemId).collect(Collectors.toSet());
        Map<Long, MaterialItem> itemMap = materialItemService.listByIds(itemIds).stream()
                .collect(Collectors.toMap(MaterialItem::getId, Function.identity()));

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (StockTransaction txn : txns) {
            StockBatch batch = batchMap.get(txn.getBatchId());
            if (batch != null) {
                MaterialItem item = itemMap.get(batch.getItemId());
                if (item != null && item.getUnitPrice() != null) {
                    BigDecimal txnAmount = txn.getQty().abs().multiply(item.getUnitPrice());
                    totalAmount = totalAmount.add(txnAmount);
                }
            }
        }
        return totalAmount;
    }
}
