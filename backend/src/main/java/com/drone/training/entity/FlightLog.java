package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 飞行作业日志 (人工审核防伪)
 *
 * @author 罗健 202308852
 */
@Data
@TableName("flight_log")
public class FlightLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long checklistId;

    private String droneModel;

    private String location;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String cropType;

    private BigDecimal area;

    private String pesticideUsed;

    private String weather;

    private Integer flightDuration;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 现场照片 JSON 数组 */
    private String images;

    private String description;

    /** PENDING / APPROVED / REJECTED */
    private String auditStatus;

    private String auditReason;

    private Long auditorId;

    private LocalDateTime auditTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String realName;
}
