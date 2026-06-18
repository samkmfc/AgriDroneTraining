package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.Result;
import com.drone.training.entity.NoFlyZone;
import com.drone.training.service.NoFlyZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 禁飞区 后台管理控制器 (仅管理员)
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/no-fly-zones")
public class NoFlyZoneAdminController {

    private final NoFlyZoneService noFlyZoneService;

    /**
     * 分页列表
     */
    @GetMapping
    public Result<Page<NoFlyZone>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String province,
                                        @RequestParam(required = false) String city,
                                        @RequestParam(required = false) String level) {
        Page<NoFlyZone> page = noFlyZoneService.page(new Page<>(pageNum, pageSize),
                Wrappers.<NoFlyZone>lambdaQuery()
                        .eq(province != null && !province.isEmpty(), NoFlyZone::getProvince, province)
                        .eq(city != null && !city.isEmpty(), NoFlyZone::getCity, city)
                        .eq(level != null && !level.isEmpty(), NoFlyZone::getLevel, level)
                        .orderByDesc(NoFlyZone::getCreateTime));
        return Result.success(page);
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<NoFlyZone> create(@RequestBody NoFlyZone noFlyZone) {
        noFlyZone.setId(null);
        noFlyZoneService.save(noFlyZone);
        return Result.success(noFlyZone);
    }

    /**
     * 修改
     */
    @PutMapping("/{id}")
    public Result<NoFlyZone> update(@PathVariable Long id, @RequestBody NoFlyZone noFlyZone) {
        noFlyZone.setId(id);
        noFlyZoneService.updateById(noFlyZone);
        return Result.success(noFlyZone);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noFlyZoneService.removeById(id);
        return Result.success();
    }
}
