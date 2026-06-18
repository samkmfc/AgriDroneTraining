package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程 (在线学习)
 *
 * @author 罗健 202308852
 */
@Data
@TableName("course")
public class Course implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String category;

    private String cover;

    private String intro;

    private String content;

    private String videoUrl;

    private String instructor;

    private Integer duration;

    private Integer viewCount;

    private Integer sort;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段: 当前用户学习进度 */
    @TableField(exist = false)
    private Integer progress;
}
