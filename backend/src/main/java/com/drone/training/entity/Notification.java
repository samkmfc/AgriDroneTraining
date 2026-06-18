package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知
 *
 * @author 罗健 202308852
 */
@Data
@TableName("notification")
public class Notification implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** LICENSE_EXPIRING / LICENSE_EXPIRED / FLIGHT_APPROVED / FLIGHT_REJECTED / SYSTEM */
    private String type;

    private String title;

    private String content;

    private Long relatedId;

    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
