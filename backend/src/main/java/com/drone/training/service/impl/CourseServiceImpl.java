package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.Course;
import com.drone.training.mapper.CourseMapper;
import com.drone.training.service.CourseService;
import org.springframework.stereotype.Service;

/**
 * 课程 Service 实现
 *
 * @author 罗健 202308852
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
}
