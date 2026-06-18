package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.Result;
import com.drone.training.entity.Regulation;
import com.drone.training.service.RegulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 法规 / 政策 / 资讯 后台管理控制器 (仅管理员)
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/regulations")
public class RegulationAdminController {

    private final RegulationService regulationService;

    /**
     * 分页列表 (全部状态)
     */
    @GetMapping
    public Result<Page<Regulation>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(required = false) String keyword) {
        Page<Regulation> page = regulationService.page(new Page<>(pageNum, pageSize),
                Wrappers.<Regulation>lambdaQuery()
                        .eq(type != null && !type.isEmpty(), Regulation::getType, type)
                        .like(keyword != null && !keyword.isEmpty(), Regulation::getTitle, keyword)
                        .orderByDesc(Regulation::getPublishTime));
        return Result.success(page);
    }

    /**
     * 新增: status=1 且 publishTime 为空时设为当前时间
     */
    @PostMapping
    public Result<Regulation> create(@RequestBody Regulation regulation) {
        regulation.setId(null);
        if (regulation.getViewCount() == null) {
            regulation.setViewCount(0);
        }
        if (regulation.getStatus() == null) {
            regulation.setStatus(1);
        }
        if (regulation.getStatus() != null && regulation.getStatus() == 1 && regulation.getPublishTime() == null) {
            regulation.setPublishTime(LocalDateTime.now());
        }
        regulationService.save(regulation);
        return Result.success(regulation);
    }

    /**
     * 修改: 发布(status=1)且 publishTime 为空时设为当前时间
     */
    @PutMapping("/{id}")
    public Result<Regulation> update(@PathVariable Long id, @RequestBody Regulation regulation) {
        regulation.setId(id);
        if (regulation.getStatus() != null && regulation.getStatus() == 1 && regulation.getPublishTime() == null) {
            regulation.setPublishTime(LocalDateTime.now());
        }
        regulationService.updateById(regulation);
        return Result.success(regulation);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        regulationService.removeById(id);
        return Result.success();
    }
}
