package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.Result;
import com.drone.training.entity.Regulation;
import com.drone.training.service.RegulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 法规 / 政策 / 资讯 控制器
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
public class RegulationController {

    private final RegulationService regulationService;

    /**
     * 分页列表 (公开端: 仅 status=1)
     */
    @GetMapping("/regulations")
    public Result<Page<Regulation>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(required = false) String keyword) {
        Page<Regulation> page = regulationService.page(new Page<>(pageNum, pageSize),
                Wrappers.<Regulation>lambdaQuery()
                        .eq(Regulation::getStatus, 1)
                        .eq(type != null && !type.isEmpty(), Regulation::getType, type)
                        .like(keyword != null && !keyword.isEmpty(), Regulation::getTitle, keyword)
                        .orderByDesc(Regulation::getPublishTime));
        return Result.success(page);
    }

    /**
     * 详情: viewCount + 1
     */
    @GetMapping("/regulations/{id}")
    public Result<Regulation> detail(@PathVariable Long id) {
        Regulation regulation = regulationService.getById(id);
        if (regulation == null) {
            return Result.error("内容不存在");
        }
        Regulation update = new Regulation();
        update.setId(id);
        update.setViewCount((regulation.getViewCount() == null ? 0 : regulation.getViewCount()) + 1);
        regulationService.updateById(update);
        regulation.setViewCount(update.getViewCount());
        return Result.success(regulation);
    }
}
