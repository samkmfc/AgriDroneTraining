package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.BusinessException;
import com.drone.training.common.Result;
import com.drone.training.entity.SafetyChecklist;
import com.drone.training.security.SecurityUtils;
import com.drone.training.service.SafetyChecklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 飞前安全检查单 Controller (学员强制填报)
 *
 * @author 罗健 202308852
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/safety-checklists")
public class SafetyChecklistController {

    private final SafetyChecklistService safetyChecklistService;

    /**
     * 学员提交飞前安全检查单。7 项检查全部通过 (==1) 时 passed=1, 否则 passed=0。
     */
    @PostMapping
    public Result<SafetyChecklist> submit(@RequestBody SafetyChecklist checklist) {
        Long userId = SecurityUtils.getUserId();
        checklist.setId(null);
        checklist.setUserId(userId);
        checklist.setCheckTime(LocalDateTime.now());

        boolean allPassed = isChecked(checklist.getBatteryCheck())
                && isChecked(checklist.getPropellerCheck())
                && isChecked(checklist.getNozzleCheck())
                && isChecked(checklist.getWeatherCheck())
                && isChecked(checklist.getAirspaceCheck())
                && isChecked(checklist.getBodyCheck())
                && isChecked(checklist.getSignalCheck());
        checklist.setPassed(allPassed ? 1 : 0);

        safetyChecklistService.save(checklist);
        log.info("用户[{}]提交飞前安全检查单, id={}, passed={}", userId, checklist.getId(), checklist.getPassed());
        return Result.success(checklist);
    }

    /**
     * 当前用户的检查单列表 (分页, 最新优先)。
     */
    @GetMapping("/my")
    public Result<Page<SafetyChecklist>> my(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtils.getUserId();
        Page<SafetyChecklist> page = safetyChecklistService.page(
                new Page<>(pageNum, pageSize),
                Wrappers.<SafetyChecklist>lambdaQuery()
                        .eq(SafetyChecklist::getUserId, userId)
                        .orderByDesc(SafetyChecklist::getId));
        return Result.success(page);
    }

    /**
     * 检查单详情 (本人或管理员可见)。
     */
    @GetMapping("/{id}")
    public Result<SafetyChecklist> detail(@PathVariable Long id) {
        SafetyChecklist checklist = safetyChecklistService.getById(id);
        if (checklist == null) {
            throw new BusinessException("检查单不存在");
        }
        if (!SecurityUtils.isAdmin() && !checklist.getUserId().equals(SecurityUtils.getUserId())) {
            throw new BusinessException("无权查看该检查单");
        }
        return Result.success(checklist);
    }

    private boolean isChecked(Integer value) {
        return value != null && value == 1;
    }
}
