package com.mint.caizhitong.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.mint.caizhitong.common.constant.RoleConst;
import com.mint.caizhitong.common.resp.Result;
import com.mint.caizhitong.domain.req.ProjectExpenseReq;
import com.mint.caizhitong.domain.req.TermConsumeReq;
import com.mint.caizhitong.domain.req.UserConsumeReq;
import com.mint.caizhitong.domain.vo.ProjectExpenseVO;
import com.mint.caizhitong.domain.vo.TermConsumeVO;
import com.mint.caizhitong.domain.vo.UserConsumeVO;
import com.mint.caizhitong.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // --- 6.1 学期耗材汇总 ---
    @SaCheckRole(mode = SaMode.OR, value = {RoleConst.LAB, RoleConst.PURCHASER, RoleConst.ADMIN})
    @GetMapping("/term-consume")
    public Object getTermConsume(TermConsumeReq req, HttpServletResponse response) throws IOException {
        List<TermConsumeVO> list = reportService.getTermConsumeReport(req);

        if ("excel".equalsIgnoreCase(req.getExport())) {
            exportExcel(response, list, TermConsumeVO.class, "学期耗材汇总");
            return null; // 导出文件时不返回 JSON
        }
        return Result.success(Map.of("rows", list));
    }

    // --- 6.2 经费对应表 ---
    @SaCheckRole(mode = SaMode.OR, value = {RoleConst.LAB, RoleConst.PURCHASER, RoleConst.ADMIN})
    @GetMapping("/project-expense")
    public Object getProjectExpense(ProjectExpenseReq req, HttpServletResponse response) throws IOException {
        List<ProjectExpenseVO> list = reportService.getProjectExpenseReport(req);

        if ("excel".equalsIgnoreCase(req.getExport())) {
            exportExcel(response, list, ProjectExpenseVO.class, "经费支出明细");
            return null;
        }
        return Result.success(list);
    }

    // --- 6.3 个人领用明细 ---
    @GetMapping("/user-consume")
    public Object getUserConsume(UserConsumeReq req, HttpServletResponse response) throws IOException {
        // 【安全控制】
        // 如果是学生，强制只能看自己的
        if (StpUtil.hasRole(RoleConst.STUDENT) && !StpUtil.hasRole(RoleConst.ADMIN)) {
            req.setUserId(StpUtil.getLoginIdAsLong());
        }
        List<UserConsumeVO> list = reportService.getUserConsumeReport(req);

        if ("excel".equalsIgnoreCase(req.getExport())) {
            exportExcel(response, list, UserConsumeVO.class, "个人领用明细");
            return null;
        }
        return Result.success(list);
    }

    /**
     * 通用 Excel 导出方法 (依赖 EasyExcel)
     */
    private void exportExcel(HttpServletResponse response, List<?> data, Class<?> head, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), head)
                .sheet("Sheet1")
                .doWrite(data);
    }
}
