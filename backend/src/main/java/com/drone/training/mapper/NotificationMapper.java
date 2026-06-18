package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息通知 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
