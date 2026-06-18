package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.Result;
import com.drone.training.entity.PesticideRatio;
import com.drone.training.service.PesticideRatioService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 农药配比 管理端 Controller
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/pesticides")
public class AdminPesticideRatioController {

    private final PesticideRatioService pesticideRatioService;

    /**
     * 分页查询
     */
    @GetMapping
    public Result<Page<PesticideRatio>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) String cropName,
                                             @RequestParam(required = false) String keyword) {
        Page<PesticideRatio> page = pesticideRatioService.page(new Page<>(pageNum, pageSize),
                Wrappers.<PesticideRatio>lambdaQuery()
                        .eq(StringUtils.hasText(cropName), PesticideRatio::getCropName, cropName)
                        .and(StringUtils.hasText(keyword), w -> w
                                .like(PesticideRatio::getPesticideName, keyword)
                                .or()
                                .like(PesticideRatio::getPestName, keyword))
                        .orderByDesc(PesticideRatio::getCreateTime));
        return Result.success(page);
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<PesticideRatio> save(@RequestBody PesticideRatio pesticideRatio) {
        pesticideRatioService.save(pesticideRatio);
        return Result.success(pesticideRatio);
    }

    /**
     * 修改
     */
    @PutMapping("/{id}")
    public Result<PesticideRatio> update(@PathVariable Long id, @RequestBody PesticideRatio pesticideRatio) {
        pesticideRatio.setId(id);
        pesticideRatioService.updateById(pesticideRatio);
        return Result.success(pesticideRatio);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        pesticideRatioService.removeById(id);
        return Result.success();
    }
}
