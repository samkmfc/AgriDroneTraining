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
 * 农药配比 Controller (植保知识库)
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
public class PesticideRatioController {

    private final PesticideRatioService pesticideRatioService;

    /**
     * 农药配比分页查询
     */
    @GetMapping("/pesticides")
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
}
