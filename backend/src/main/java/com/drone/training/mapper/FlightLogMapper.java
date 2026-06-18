package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.FlightLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 飞行作业日志 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface FlightLogMapper extends BaseMapper<FlightLog> {
}
