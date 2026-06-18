package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.NoFlyZone;
import org.apache.ibatis.annotations.Mapper;

/**
 * 禁飞区 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface NoFlyZoneMapper extends BaseMapper<NoFlyZone> {
}
