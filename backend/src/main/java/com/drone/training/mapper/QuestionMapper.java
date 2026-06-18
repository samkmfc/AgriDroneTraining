package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题库 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
