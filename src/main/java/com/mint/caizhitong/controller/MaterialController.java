package com.mint.caizhitong.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.mint.caizhitong.common.resp.PageVo;
import com.mint.caizhitong.common.resp.Result;
import com.mint.caizhitong.domain.*;
import com.mint.caizhitong.domain.MaterialRequest.MaterialCreateRequest;
import com.mint.caizhitong.domain.MaterialRequest.MaterialUpdateRequest;
import com.mint.caizhitong.model.MaterialCategory;
import com.mint.caizhitong.service.IMaterialCategoryService;
import com.mint.caizhitong.service.IMaterialItemService;
import com.mint.caizhitong.service.impl.MaterialItemServiceImpl;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 鏉愭枡绫荤洰鏍 前端控制器
 * </p>
 *
 * @author mint
 * @since 2025-11-25
 */
@RestController
@RequestMapping("/api/materials")
public class MaterialController {

    private IMaterialCategoryService materialCategoryService;

    private IMaterialItemService materialItemService;

    public MaterialController(IMaterialCategoryService materialCategoryService, IMaterialItemService materialItemService) {
        this.materialCategoryService = materialCategoryService;
        this.materialItemService = materialItemService;
    }

    //获取类目树
    @GetMapping("/categories/tree")
    public Result<List<CategoryTreeDTO>> getCategoryTree() {
        List<CategoryTreeDTO> tree = materialCategoryService.getCategoryTree();
        return Result.success(tree);
    }

    //创建类目
    @PostMapping("/categories")
    public Result addCategory(@RequestBody @Validated CategoryDTO categoryDTO) {
        materialCategoryService.saveNewCategory(categoryDTO);
        //返回成功结果
        return Result.success();
    }

//    更新类目
    @PutMapping("/categories/{id}")
    public Result updateCategory(@PathVariable Long id, @RequestBody @Validated CategoryDTO request) {
        materialCategoryService.updateCategory(id, request);
        return Result.success();
    }
//    删除类目
    @DeleteMapping("/categories/{id}")
    public Result deleteCategory(@PathVariable Long id) {
        materialCategoryService.removeCategory(id);
        return Result.success();
    }

    @GetMapping
    public Result<PageVo<MaterialListDTO>> getMaterialList(MaterialItemQueryDTO request) {
        // Spring Boot 会自动将 Query 参数绑定到 request 对象
        PageVo<MaterialListDTO> result = materialItemService.pageQueryMaterials(request);
        return Result.success(result);
    }

    @PostMapping
    public Result saveMaterials(@RequestBody @Validated MaterialCreateRequest request) {
        materialItemService.createMaterial(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result updateMaterials(@PathVariable Long id, @RequestBody @Validated MaterialUpdateRequest request) {
        materialItemService.updateMaterial(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result deleteMaterials(@PathVariable Long id) {
        materialItemService.removeMaterial(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result getMaterial(@PathVariable Long id) {
        return Result.error("功能尚未完成");
    }
}
