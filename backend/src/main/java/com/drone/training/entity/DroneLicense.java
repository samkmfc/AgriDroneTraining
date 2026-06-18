package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 无人机执照 (有效期动态监控 / 临期预警)
 *
 * @author 罗健 202308852
 */
@Data
@TableName("drone_license")
public class DroneLicense implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String licenseNo;

    private String licenseType;

    private String droneModel;

    private String issuingAuthority;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    /** NORMAL / EXPIRING / EXPIRED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段: 学员姓名(联表展示) */
    @TableField(exist = false)
    private String realName;

    /** 非数据库字段: 距到期天数 */
    @TableField(exist = false)
    private Long remainingDays;
}
