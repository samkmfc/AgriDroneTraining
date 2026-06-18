package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.ExamRecord;
import com.drone.training.mapper.ExamRecordMapper;
import com.drone.training.service.ExamRecordService;
import org.springframework.stereotype.Service;

/**
 * 考试记录 Service 实现
 *
 * @author 罗健 202308852
 */
@Service
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamRecordService {
}
