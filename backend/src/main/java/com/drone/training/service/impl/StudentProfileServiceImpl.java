package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.StudentProfile;
import com.drone.training.mapper.StudentProfileMapper;
import com.drone.training.service.StudentProfileService;
import org.springframework.stereotype.Service;

/**
 * 学员档案服务实现
 *
 * @author 罗健 202308852
 */
@Service
public class StudentProfileServiceImpl extends ServiceImpl<StudentProfileMapper, StudentProfile>
        implements StudentProfileService {
}
