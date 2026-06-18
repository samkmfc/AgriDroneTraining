package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.Result;
import com.drone.training.entity.CropAtlas;
import com.drone.training.service.CropAtlasService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 作物识别图谱 管理端 Controller
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/crops")
public class AdminCropAtlasController {

    private final CropAtlasService cropAtlasService;

    /**
     * 分页查询
     */
    @GetMapping
    public Result<Page<CropAtlas>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String category) {
        Page<CropAtlas> page = cropAtlasService.page(new Page<>(pageNum, pageSize),
                Wrappers.<CropAtlas>lambdaQuery()
                        .like(StringUtils.hasText(keyword), CropAtlas::getCropName, keyword)
                        .eq(StringUtils.hasText(category), CropAtlas::getCategory, category)
                        .orderByDesc(CropAtlas::getCreateTime));
        return Result.success(page);
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<CropAtlas> save(@RequestBody CropAtlas cropAtlas) {
        cropAtlasService.save(cropAtlas);
        return Result.success(cropAtlas);
    }

    /**
     * 修改
     */
    @PutMapping("/{id}")
    public Result<CropAtlas> update(@PathVariable Long id, @RequestBody CropAtlas cropAtlas) {
        cropAtlas.setId(id);
        cropAtlasService.updateById(cropAtlas);
        return Result.success(cropAtlas);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cropAtlasService.removeById(id);
        return Result.success();
    }
}
