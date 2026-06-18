package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.Exam;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface ExamMapper extends BaseMapper<Exam> {
}
