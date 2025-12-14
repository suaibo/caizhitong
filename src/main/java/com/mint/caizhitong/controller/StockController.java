package com.mint.caizhitong.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mint.caizhitong.common.constant.RoleConst;
import com.mint.caizhitong.common.resp.Result;
import com.mint.caizhitong.domain.stockrequest.*;
import com.mint.caizhitong.domain.stockvo.*;
import com.mint.caizhitong.model.StockTransaction;
import com.mint.caizhitong.service.StockBusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private StockBusinessService stockBusinessService;

    /**
     * 4.1.1 新建入库单
     */
    @SaCheckRole(mode = SaMode.OR, value = {RoleConst.LAB, RoleConst.ADMIN})
    @PostMapping("/in")
    public Result<Map<String, Long>> stockIn(@RequestBody @Validated StockInReq req) {
        Long stockInId = stockBusinessService.processStockIn(req);
        return Result.success(Map.of("stockInId", stockInId));
    }

    /**
     * 4.1.2 分页查询入库单 (流水)
     */
    @GetMapping("/in")
    public Result<Page<StockTransaction>> queryStockIn(StockPageReq req) {
        // Spring MVC 会自动把 URL 参数 (page=1&supplier=xx) 映射到 req 对象中
        Page<StockTransaction> result = stockBusinessService.pageStockIn(req);
        return Result.success(result);
    }

    /**
     * 4.1.3 查看入库单详情
     */
    @GetMapping("/in/{id}")
    public Result<StockInDetailVO> getStockInDetail(@PathVariable Long id) {
        StockInDetailVO vo = stockBusinessService.getStockInDetail(id);
        return Result.success(vo);
    }

    /**
     * 4.2.1 库存汇总 (按材料)
     */
    @GetMapping("/summary")
    public Result<List<StockSummaryVO>> getStockSummary(StockSummaryReq req) {
        // SpringMVC 会自动将 Query 参数映射到 req 对象中
        List<StockSummaryVO> list = stockBusinessService.getStockSummary(req);
        return Result.success(list);
    }

    /**
     * 4.2.2 查询某材料的所有批次
     */
    @GetMapping("/batches")
    public Result<List<StockBatchVO>> getStockBatches(@RequestParam Long itemId) {
        List<StockBatchVO> list = stockBusinessService.getStockBatches(itemId);
        return Result.success(list);
    }
    /**
     * 4.3.1 扫码领用（出库）
     */
    @SaCheckRole(mode = SaMode.OR, value = {RoleConst.STUDENT, RoleConst.LAB, RoleConst.ADMIN})
    @PostMapping("/out")
    public Result<Map<String, Long>> stockOut(@RequestBody @Validated StockOutReq req) {
        Long transactionId = stockBusinessService.processStockOut(req);
        return Result.success(Map.of("transactionId", transactionId));
    }

    /**
     * 4.3.2 归还
     */
    @SaCheckRole(mode = SaMode.OR, value = {RoleConst.LAB, RoleConst.ADMIN})
    @PostMapping("/return")
    public Result<String> stockReturn(@RequestBody @Validated StockReturnReq req) {
        stockBusinessService.processStockReturn(req);
        return Result.success("归还成功");
    }

    /**
     * 4.3.3 报废/损耗
     */
    @SaCheckRole(mode = SaMode.OR, value = {RoleConst.LAB, RoleConst.ADMIN})
    @PostMapping("/scrap")
    public Result<String> stockScrap(@RequestBody @Validated StockScrapReq req) {
        stockBusinessService.processStockScrap(req);
        return Result.success("报损登记成功");
    }

    /**
     * 4.4.1 分页查询事务流水
     */
    @GetMapping("/transactions")
    public Result<Page<StockTransactionVO>> queryStockTransactions(StockTransactionPageReq req) {
        // 获取当前用户ID和角色
        long loginId = StpUtil.getLoginIdAsLong();
        List<String> roles = StpUtil.getRoleList();
        // 【数据权限控制】
        // 如果只是学生，强制只能查自己的 (覆盖前端传来的 userId)
        if (roles.contains(RoleConst.STUDENT) && !roles.contains(RoleConst.LAB) && !roles.contains(RoleConst.ADMIN)) {
            req.setUserId(loginId);
        }

        // 实验员、采购员、管理员可以查所有 (保留前端传的 req.userId)

        Page<StockTransactionVO> result = stockBusinessService.pageStockTransactions(req);
        return Result.success(result);
    }


    /**
     * 5.1 获取当前库存预警列表
     */
}
