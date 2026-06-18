package com.drone.training.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.Notification;
import com.drone.training.mapper.NotificationMapper;
import com.drone.training.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 消息通知 Service 实现
 *
 * @author 罗健 202308852
 */
@Slf4j
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    public void notify(Long userId, String type, String title, String content, Long relatedId) {
        if (userId == null) {
            return;
        }
        try {
            Notification n = new Notification();
            n.setUserId(userId);
            n.setType(type);
            n.setTitle(title);
            n.setContent(content);
            n.setRelatedId(relatedId);
            n.setIsRead(0);
            save(n);
        } catch (Exception e) {
            log.warn("发送通知失败: {}", e.getMessage());
        }
    }

    @Override
    public long unreadCount(Long userId) {
        return count(Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }
}
