package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 空域法规 / 政策 / 资讯
 *
 * @author 罗健 202308852
 */
@Data
@TableName("regulation")
public class Regulation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /** REGULATION / POLICY / NEWS */
    private String type;

    private String cover;

    private String summary;

    private String content;

    private String author;

    private Integer viewCount;

    private LocalDateTime publishTime;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
