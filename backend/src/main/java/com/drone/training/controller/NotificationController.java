package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.Result;
import com.drone.training.entity.Notification;
import com.drone.training.security.SecurityUtils;
import com.drone.training.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息通知 Controller
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /** 我的通知 (分页, 可按是否已读过滤) */
    @GetMapping("/my")
    public Result<Page<Notification>> my(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) Integer isRead) {
        Long uid = SecurityUtils.getUserId();
        Page<Notification> page = notificationService.page(
                new Page<>(pageNum, pageSize),
                Wrappers.<Notification>lambdaQuery()
                        .eq(Notification::getUserId, uid)
                        .eq(isRead != null, Notification::getIsRead, isRead)
                        .orderByDesc(Notification::getCreateTime));
        return Result.success(page);
    }

    /** 未读数量 */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> unreadCount() {
        Map<String, Object> data = new HashMap<>();
        data.put("count", notificationService.unreadCount(SecurityUtils.getUserId()));
        return Result.success(data);
    }

    /** 标记单条已读 */
    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        Notification n = notificationService.getById(id);
        if (n != null && n.getUserId().equals(SecurityUtils.getUserId())) {
            n.setIsRead(1);
            notificationService.updateById(n);
        }
        return Result.success();
    }

    /** 全部标记已读 */
    @PutMapping("/read-all")
    public Result<Void> readAll() {
        Notification update = new Notification();
        update.setIsRead(1);
        notificationService.update(update, Wrappers.<Notification>lambdaUpdate()
                .eq(Notification::getUserId, SecurityUtils.getUserId())
                .eq(Notification::getIsRead, 0));
        return Result.success();
    }

    /** 删除通知 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        Notification n = notificationService.getById(id);
        if (n != null && n.getUserId().equals(SecurityUtils.getUserId())) {
            notificationService.removeById(id);
        }
        return Result.success();
    }
}
