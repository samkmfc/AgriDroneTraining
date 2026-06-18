package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 智慧农情 AI 诊断记录 (VisualGLM)
 *
 * @author 罗健 202308852
 */
@Data
@TableName("ai_diagnosis")
public class AiDiagnosis implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String imageUrl;

    private String cropType;

    private String question;

    private String recognitionResult;

    private String recommendPesticide;

    /** 推荐飞行参数 JSON: {speed,height,flow} */
    private String recommendParams;

    private BigDecimal confidence;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
