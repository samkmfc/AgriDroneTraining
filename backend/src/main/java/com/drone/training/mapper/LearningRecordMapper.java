package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.LearningRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习记录 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface LearningRecordMapper extends BaseMapper<LearningRecord> {
}
