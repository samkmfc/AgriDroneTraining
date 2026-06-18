package com.drone.training.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drone.training.entity.Notification;

/**
 * 消息通知 Service
 *
 * @author 罗健 202308852
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 发送一条通知
     */
    void notify(Long userId, String type, String title, String content, Long relatedId);

    /**
     * 当前用户未读数量
     */
    long unreadCount(Long userId);
}
