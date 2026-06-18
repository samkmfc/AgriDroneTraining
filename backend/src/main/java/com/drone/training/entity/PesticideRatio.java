package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 农药配比 (植保知识库)
 *
 * @author 罗健 202308852
 */
@Data
@TableName("pesticide_ratio")
public class PesticideRatio implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String pesticideName;

    private String cropName;

    private String pestName;

    private String dosage;

    private String waterRatio;

    private String sprayMethod;

    private String safetyInterval;

    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
