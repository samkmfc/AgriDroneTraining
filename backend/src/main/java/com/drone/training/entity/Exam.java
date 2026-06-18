package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 试卷 / 场景化测试
 *
 * @author 罗健 202308852
 */
@Data
@TableName("exam")
public class Exam implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String category;

    private String description;

    private Integer totalScore;

    private Integer passScore;

    private Integer duration;

    /** 题目ID集合 JSON: [1,2,3] */
    private String questionIds;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段: 题目数量 */
    @TableField(exist = false)
    private Integer questionCount;
}
