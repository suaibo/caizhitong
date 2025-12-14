package com.mint.caizhitong.domain.req;

import lombok.Data;

/**
 * @author Mint
 */
@Data
public class TermConsumeReq {
    private String term;       // 学期 (必填)
    private String courseName; // 课程名
    private Long teacherId;    // 教师ID (对应 stock_transaction.user_id)
    private String export;     // excel / pdf / null
}
