package com.mint.caizhitong.controller;

import com.mint.caizhitong.common.resp.Result;
import com.mint.caizhitong.domain.vo.DashboardOverviewVO;
import com.mint.caizhitong.domain.vo.TopConsumeVO;
import com.mint.caizhitong.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 7.1 仪表盘总览
     */
    @GetMapping("/overview")
    public Result<DashboardOverviewVO> getOverview(@RequestParam(required = false) String term) {
        // 前端没传 term，计算出来的就是"历史总消耗"。
        // 需要默认"2025春"，建议前端传参，或者后端在这里写死默认值。
        DashboardOverviewVO vo = dashboardService.getOverview(term);
        return Result.success(vo);
    }

    /**
     * 7.2 耗材 TOP10
     */
    @GetMapping("/top-consume")
    public Result<List<TopConsumeVO>> getTopConsume(
            @RequestParam(required = false) String term,
            @RequestParam(defaultValue = "10") int limit) {

        List<TopConsumeVO> list = dashboardService.getTopConsume(term, limit);
        return Result.success(list);
    }
}
