package com.mint.caizhitong.mapper;

import com.mint.caizhitong.model.StockBatch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * <p>
 * 搴撳瓨鎵规?琛 Mapper 接口
 * </p>
 *
 * @author mint
 * @since 2025-11-25
 */
public interface StockBatchMapper extends BaseMapper<StockBatch> {

    @Select("SELECT SUM(quantity) FROM stock_batch WHERE item_id = #{id}")
    BigDecimal selectTotalQuantityByItemId(Long id);
}
