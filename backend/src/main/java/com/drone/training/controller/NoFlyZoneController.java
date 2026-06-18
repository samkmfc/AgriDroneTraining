package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.drone.training.common.Result;
import com.drone.training.entity.NoFlyZone;
import com.drone.training.service.NoFlyZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 禁飞区 控制器 (地图数据)
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
public class NoFlyZoneController {

    private final NoFlyZoneService noFlyZoneService;

    /**
     * 全量列表 (地图渲染, 不分页), 可按 keyword(区域名/省/市模糊) 或 province / city / level 过滤
     */
    @GetMapping("/no-fly-zones")
    public Result<List<NoFlyZone>> list(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String province,
                                        @RequestParam(required = false) String city,
                                        @RequestParam(required = false) String level) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        List<NoFlyZone> list = noFlyZoneService.list(
                Wrappers.<NoFlyZone>lambdaQuery()
                        .and(hasKeyword, w -> w
                                .like(NoFlyZone::getName, keyword.trim())
                                .or().like(NoFlyZone::getProvince, keyword.trim())
                                .or().like(NoFlyZone::getCity, keyword.trim()))
                        .like(province != null && !province.isEmpty(), NoFlyZone::getProvince, province)
                        .like(city != null && !city.isEmpty(), NoFlyZone::getCity, city)
                        .eq(level != null && !level.isEmpty(), NoFlyZone::getLevel, level));
        return Result.success(list);
    }
}
