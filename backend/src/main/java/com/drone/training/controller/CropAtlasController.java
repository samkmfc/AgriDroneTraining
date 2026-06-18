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
 * 作物识别图谱 Controller (植保知识库)
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
public class CropAtlasController {

    private final CropAtlasService cropAtlasService;

    /**
     * 作物图谱分页查询
     */
    @GetMapping("/crops")
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
     * 作物图谱详情
     */
    @GetMapping("/crops/{id}")
    public Result<CropAtlas> detail(@PathVariable Long id) {
        return Result.success(cropAtlasService.getById(id));
    }
}
