package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.BusinessException;
import com.drone.training.common.Result;
import com.drone.training.dto.FlightLogAuditDTO;
import com.drone.training.entity.FlightLog;
import com.drone.training.entity.User;
import com.drone.training.mapper.UserMapper;
import com.drone.training.security.SecurityUtils;
import com.drone.training.service.FlightLogService;
import com.drone.training.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 飞行作业日志 管理端 Controller (人工审核防伪)
 *
 * @author 罗健 202308852
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/flight-logs")
public class FlightLogAdminController {

    private final FlightLogService flightLogService;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    /**
     * 分页查询全部飞行日志, 支持按审核状态及关键字 (地点/机型/作物) 过滤, 并填充学员真实姓名。
     */
    @GetMapping
    public Result<Page<FlightLog>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String auditStatus,
                                        @RequestParam(required = false) String keyword) {
        Page<FlightLog> page = flightLogService.page(
                new Page<>(pageNum, pageSize),
                Wrappers.<FlightLog>lambdaQuery()
                        .eq(StringUtils.hasText(auditStatus), FlightLog::getAuditStatus, auditStatus)
                        .and(StringUtils.hasText(keyword), w -> w
                                .like(FlightLog::getLocation, keyword)
                                .or().like(FlightLog::getDroneModel, keyword)
                                .or().like(FlightLog::getCropType, keyword))
                        .orderByDesc(FlightLog::getId));

        enrichRealName(page.getRecords());
        return Result.success(page);
    }

    /**
     * 审核飞行日志 (防伪驳回工作流)。auditStatus 必须为 APPROVED 或 REJECTED, 驳回时必须填写理由。
     */
    @PutMapping("/{id}/audit")
    public Result<FlightLog> audit(@PathVariable Long id, @RequestBody FlightLogAuditDTO dto) {
        FlightLog flightLog = flightLogService.getById(id);
        if (flightLog == null) {
            throw new BusinessException("飞行日志不存在");
        }

        String status = dto.getAuditStatus();
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new BusinessException("审核状态非法, 只能为 APPROVED 或 REJECTED");
        }
        if ("REJECTED".equals(status) && !StringUtils.hasText(dto.getAuditReason())) {
            throw new BusinessException("驳回时必须填写驳回理由");
        }

        flightLog.setAuditStatus(status);
        flightLog.setAuditReason(dto.getAuditReason());
        flightLog.setAuditorId(SecurityUtils.getUserId());
        flightLog.setAuditTime(LocalDateTime.now());
        flightLogService.updateById(flightLog);

        sendAuditNotification(flightLog, status);
        log.info("管理员[{}]审核飞行日志 id={}, 结果={}", SecurityUtils.getUserId(), id, status);
        return Result.success(flightLog);
    }

    /**
     * 批量审核飞行日志。body: { ids:[], auditStatus, auditReason }
     */
    @PutMapping("/batch-audit")
    public Result<Map<String, Object>> batchAudit(@RequestBody Map<String, Object> body) {
        String status = (String) body.get("auditStatus");
        String reason = (String) body.get("auditReason");
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new BusinessException("审核状态非法");
        }
        if ("REJECTED".equals(status) && !StringUtils.hasText(reason)) {
            throw new BusinessException("批量驳回时必须填写驳回理由");
        }
        @SuppressWarnings("unchecked")
        List<Object> rawIds = (List<Object>) body.get("ids");
        if (rawIds == null || rawIds.isEmpty()) {
            throw new BusinessException("请选择要审核的日志");
        }
        int count = 0;
        for (Object raw : rawIds) {
            Long id = Long.valueOf(String.valueOf(raw));
            FlightLog fl = flightLogService.getById(id);
            if (fl == null || !"PENDING".equals(fl.getAuditStatus())) {
                continue;
            }
            fl.setAuditStatus(status);
            fl.setAuditReason(reason);
            fl.setAuditorId(SecurityUtils.getUserId());
            fl.setAuditTime(LocalDateTime.now());
            flightLogService.updateById(fl);
            sendAuditNotification(fl, status);
            count++;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return Result.success("已审核 " + count + " 条", data);
    }

    /**
     * 审核结果通知学员
     */
    private void sendAuditNotification(FlightLog flightLog, String status) {
        boolean approved = "APPROVED".equals(status);
        String title = approved ? "飞行作业日志审核通过" : "飞行作业日志被驳回";
        String content = approved
                ? "您于 " + flightLog.getLocation() + " 的作业日志已通过审核。"
                : "您于 " + flightLog.getLocation() + " 的作业日志被驳回，原因：" + flightLog.getAuditReason();
        notificationService.notify(flightLog.getUserId(),
                approved ? "FLIGHT_APPROVED" : "FLIGHT_REJECTED", title, content, flightLog.getId());
    }

    /**
     * 批量填充学员真实姓名 (exist=false 字段)。
     */
    private void enrichRealName(List<FlightLog> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> userIds = records.stream()
                .map(FlightLog::getUserId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, String> idToName = new HashMap<>();
        List<User> users = userMapper.selectBatchIds(userIds);
        for (User user : users) {
            idToName.put(user.getId(), user.getRealName());
        }
        for (FlightLog log : records) {
            log.setRealName(idToName.get(log.getUserId()));
        }
    }
}
