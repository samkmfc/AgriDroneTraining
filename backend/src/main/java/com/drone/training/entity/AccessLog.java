package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 访问日志 (平台访问量统计)
 *
 * @author 罗健 202308852
 */
@Data
@TableName("access_log")
public class AccessLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String ip;

    private String module;

    private String action;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
