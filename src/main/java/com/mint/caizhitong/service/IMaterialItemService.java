package com.mint.caizhitong.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.mint.caizhitong.common.resp.PageVo;
import com.mint.caizhitong.domain.MaterialItemDTO;
import com.mint.caizhitong.domain.MaterialItemQueryDTO;
import com.mint.caizhitong.domain.MaterialListDTO;
import com.mint.caizhitong.domain.MaterialRequest.MaterialCreateRequest;
import com.mint.caizhitong.domain.MaterialRequest.MaterialUpdateRequest;
import com.mint.caizhitong.model.MaterialItem;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 鏉愭枡鏄庣粏琛 服务类
 * </p>
 *
 * @author mint
 * @since 2025-11-25
 */
public interface IMaterialItemService extends IService<MaterialItem> {

    @Transactional(readOnly = true)
    PageVo<MaterialListDTO> pageQueryMaterials(MaterialItemQueryDTO request);

    @Transactional
    void createMaterial(MaterialCreateRequest request);

    @Transactional
    void updateMaterial(Long id, MaterialUpdateRequest request);

    @Transactional
    void removeMaterial(Long id);
}
