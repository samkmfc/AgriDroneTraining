package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学员资质档案
 *
 * @author 罗健 202308852
 */
@Data
@TableName("student_profile")
public class StudentProfile implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String idCard;

    private String address;

    private String emergencyContact;

    private String emergencyPhone;

    private String education;

    /** TRAINING / GRADUATED / SUSPENDED */
    private String trainingStatus;

    private LocalDate enrollDate;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
