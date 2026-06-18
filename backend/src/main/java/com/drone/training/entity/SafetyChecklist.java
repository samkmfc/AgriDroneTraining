package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 飞前安全检查单 (强制填报)
 *
 * @author 罗健 202308852
 */
@Data
@TableName("safety_checklist")
public class SafetyChecklist implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String location;

    private String weather;

    private String windSpeed;

    private Integer batteryCheck;

    private Integer propellerCheck;

    private Integer nozzleCheck;

    private Integer weatherCheck;

    private Integer airspaceCheck;

    private Integer bodyCheck;

    private Integer signalCheck;

    /** 检查项明细扩展 JSON */
    private String checkItems;

    private Integer passed;

    private String remark;

    private LocalDateTime checkTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String realName;
}
