package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 作物识别图谱 (植保知识库)
 *
 * @author 罗健 202308852
 */
@Data
@TableName("crop_atlas")
public class CropAtlas implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String cropName;

    private String image;

    private String category;

    private String growthCycle;

    private String commonPests;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
