package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.AccessLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访问日志 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface AccessLogMapper extends BaseMapper<AccessLog> {
}
