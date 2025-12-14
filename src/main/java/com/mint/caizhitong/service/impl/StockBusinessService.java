package com.mint.caizhitong.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mint.caizhitong.common.exception.BusinessConflictException;
import com.mint.caizhitong.domain.StockRequest.StockInReq;
import com.mint.caizhitong.domain.StockRequest.StockPageReq;
import com.mint.caizhitong.model.StockBatch;
import com.mint.caizhitong.model.StockTransaction;
import com.mint.caizhitong.service.IStockBatchService;
import com.mint.caizhitong.service.IStockTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StockBusinessService {

    private final IStockBatchService stockBatchService;
    private final IStockTransactionService stockTransactionService;

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
}
