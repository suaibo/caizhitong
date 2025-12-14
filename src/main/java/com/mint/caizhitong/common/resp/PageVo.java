package com.mint.caizhitong.common.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 王彬权
 * 分页实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVo<T> {
    //总数
    private long total;
    //当前页集合
    private List<T> list;

    public static <T> PageVo<T> of(List<T> list, Long total) {
        PageVo<T> result = new PageVo<>();
        result.setList(list);
        result.setTotal(total);
        return result;
    }
}
