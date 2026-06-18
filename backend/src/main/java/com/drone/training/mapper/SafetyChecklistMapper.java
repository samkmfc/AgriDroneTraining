package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.SafetyChecklist;
import org.apache.ibatis.annotations.Mapper;

/**
 * 飞前安全检查单 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface SafetyChecklistMapper extends BaseMapper<SafetyChecklist> {
}
