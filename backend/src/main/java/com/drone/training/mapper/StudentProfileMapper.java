package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.StudentProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学员档案 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface StudentProfileMapper extends BaseMapper<StudentProfile> {
}
