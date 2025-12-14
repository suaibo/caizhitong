package com.mint.caizhitong.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mint.caizhitong.domain.MaterialItemQueryDTO;
import com.mint.caizhitong.domain.MaterialListDTO;
import com.mint.caizhitong.model.MaterialItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 鏉愭枡鏄庣粏琛 Mapper 接口
 * </p>
 *
 * @author mint
 * @since 2025-11-25
 */
public interface MaterialItemMapper extends BaseMapper<MaterialItem> {

    @Select("SELECT 1 FROM material_item WHERE category_id = #{id} LIMIT 1")
    boolean existsByCategoryId(Long id);

    IPage<MaterialListDTO> selectPageQuery(IPage<MaterialListDTO> page, @Param("req") MaterialItemQueryDTO request);

}
