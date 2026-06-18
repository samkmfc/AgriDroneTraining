package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考试记录 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {
}
