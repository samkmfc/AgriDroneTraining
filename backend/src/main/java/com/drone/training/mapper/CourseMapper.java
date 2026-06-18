package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.Course;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
