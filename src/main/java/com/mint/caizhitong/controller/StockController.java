package com.mint.caizhitong.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mint.caizhitong.common.resp.Result;
import com.mint.caizhitong.domain.StockRequest.StockInReq;
import com.mint.caizhitong.domain.StockRequest.StockPageReq;
import com.mint.caizhitong.model.StockTransaction;
import com.mint.caizhitong.service.impl.StockBusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private StockBusinessService stockBusinessService;
    /**
     * 4.1.1 新建入库单
     */
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
}
