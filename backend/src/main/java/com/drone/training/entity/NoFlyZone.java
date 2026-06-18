package com.drone.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 禁飞区 (地图展示)
 *
 * @author 罗健 202308852
 */
@Data
@TableName("no_fly_zone")
public class NoFlyZone implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String province;

    private String city;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer radius;

    /** STRICT / LIMIT */
    private String level;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
