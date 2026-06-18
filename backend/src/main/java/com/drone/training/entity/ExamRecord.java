package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考试记录 (通过率统计来源)
 *
 * @author 罗健 202308852
 */
@Data
@TableName("exam_record")
public class ExamRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examId;

    private Long userId;

    private Integer score;

    private Integer passed;

    /** 作答详情 JSON */
    private String answers;

    private Integer duration;

    private LocalDateTime startTime;

    private LocalDateTime submitTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 非数据库字段 */
    @TableField(exist = false)
    private String examTitle;

    @TableField(exist = false)
    private String realName;
}
