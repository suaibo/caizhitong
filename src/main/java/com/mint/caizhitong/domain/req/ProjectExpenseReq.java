package com.mint.caizhitong.domain.req;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * @author Mint
 */
@Data
public class ProjectExpenseReq {
    private String projectNo;  // 项目号 (必填)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFrom;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateTo;
    private String export;
}
