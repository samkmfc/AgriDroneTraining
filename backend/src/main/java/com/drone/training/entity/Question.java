package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题库 (多维度试题)
 *
 * @author 罗健 202308852
 */
@Data
@TableName("question")
public class Question implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** SINGLE / MULTI / JUDGE */
    private String type;

    private String category;

    private String content;

    /** 选项 JSON 字符串: [{"key":"A","value":"..."}] */
    private String options;

    /** 正确答案: A 或 A,B 或 T/F */
    private String answer;

    private String analysis;

    private Integer difficulty;

    private Integer score;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
