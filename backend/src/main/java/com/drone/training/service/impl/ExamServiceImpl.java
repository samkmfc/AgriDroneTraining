package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.Exam;
import com.drone.training.mapper.ExamMapper;
import com.drone.training.service.ExamService;
import org.springframework.stereotype.Service;

/**
 * 试卷 Service 实现
 *
 * @author 罗健 202308852
 */
@Service
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements ExamService {
}
