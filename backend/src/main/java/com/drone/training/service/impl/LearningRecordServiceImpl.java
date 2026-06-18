package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.LearningRecord;
import com.drone.training.mapper.LearningRecordMapper;
import com.drone.training.service.LearningRecordService;
import org.springframework.stereotype.Service;

/**
 * 学习记录 Service 实现
 *
 * @author 罗健 202308852
 */
@Service
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord> implements LearningRecordService {
}
