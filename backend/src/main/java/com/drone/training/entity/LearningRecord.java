package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学习记录
 *
 * @author 罗健 202308852
 */
@Data
@TableName("learning_record")
public class LearningRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long courseId;

    private Integer progress;

    private Integer finished;

    private LocalDateTime lastStudyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
