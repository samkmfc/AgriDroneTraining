package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.BusinessException;
import com.drone.training.common.Result;
import com.drone.training.entity.FlightLog;
import com.drone.training.entity.SafetyChecklist;
import com.drone.training.security.SecurityUtils;
import com.drone.training.service.FlightLogService;
import com.drone.training.service.SafetyChecklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 飞行作业日志 Controller (学员提交 / 查询)
 *
 * @author 罗健 202308852
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/flight-logs")
public class FlightLogController {

    private final FlightLogService flightLogService;
    private final SafetyChecklistService safetyChecklistService;

    /**
     * 学员提交飞行作业日志。若关联了检查单, 必须属于本人且已通过 (passed==1)。
     */
    @PostMapping
    public Result<FlightLog> submit(@RequestBody FlightLog flightLog) {
        Long userId = SecurityUtils.getUserId();
        flightLog.setId(null);
        flightLog.setUserId(userId);
        flightLog.setAuditStatus("PENDING");
        // 提交时清空审核相关字段, 防止伪造
        flightLog.setAuditReason(null);
        flightLog.setAuditorId(null);
        flightLog.setAuditTime(null);

        Long checklistId = flightLog.getChecklistId();
        if (checklistId != null) {
            SafetyChecklist checklist = safetyChecklistService.getById(checklistId);
            if (checklist == null || !checklist.getUserId().equals(userId)) {
                throw new BusinessException("关联的飞前安全检查单不存在");
            }
            if (checklist.getPassed() == null || checklist.getPassed() != 1) {
                throw new BusinessException("请先完成并通过飞前安全检查");
            }
        } else {
            log.warn("用户[{}]提交飞行作业日志未关联飞前安全检查单", userId);
        }

        flightLogService.save(flightLog);
        log.info("用户[{}]提交飞行作业日志, id={}", userId, flightLog.getId());
        return Result.success(flightLog);
    }

    /**
     * 当前用户的飞行日志 (分页, 可按审核状态过滤, 最新优先)。
     */
    @GetMapping("/my")
    public Result<Page<FlightLog>> my(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) String auditStatus) {
        Long userId = SecurityUtils.getUserId();
        Page<FlightLog> page = flightLogService.page(
                new Page<>(pageNum, pageSize),
                Wrappers.<FlightLog>lambdaQuery()
                        .eq(FlightLog::getUserId, userId)
                        .eq(StringUtils.hasText(auditStatus), FlightLog::getAuditStatus, auditStatus)
                        .orderByDesc(FlightLog::getId));
        return Result.success(page);
    }

    /**
     * 飞行日志详情 (本人或管理员可见)。
     */
    @GetMapping("/{id}")
    public Result<FlightLog> detail(@PathVariable Long id) {
        FlightLog flightLog = flightLogService.getById(id);
        if (flightLog == null) {
            throw new BusinessException("飞行日志不存在");
        }
        if (!SecurityUtils.isAdmin() && !flightLog.getUserId().equals(SecurityUtils.getUserId())) {
            throw new BusinessException("无权查看该飞行日志");
        }
        return Result.success(flightLog);
    }
}
