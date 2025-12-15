package com.mint.caizhitong.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mint.caizhitong.common.constant.RoleConst;
import com.mint.caizhitong.common.resp.Result;
import com.mint.caizhitong.domain.stockvo.StockAlertVO;
import com.mint.caizhitong.service.StockBusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final StockBusinessService stockBusinessService;

    /**
     * 5.1 获取当前库存预警列表
     */
    @SaCheckRole(mode = SaMode.OR, value = {RoleConst.PURCHASER, RoleConst.LAB, RoleConst.ADMIN})
    @GetMapping("/stock")
    public Result<Page<StockAlertVO>> getStockAlerts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<StockAlertVO> result = stockBusinessService.getAlertStockPage(page, pageSize);
        return Result.success(result);
    }
}
