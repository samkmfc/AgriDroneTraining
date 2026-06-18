package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.SafetyChecklist;
import com.drone.training.mapper.SafetyChecklistMapper;
import com.drone.training.service.SafetyChecklistService;
import org.springframework.stereotype.Service;

/**
 * 飞前安全检查单 Service 实现
 *
 * @author 罗健 202308852
 */
@Service
public class SafetyChecklistServiceImpl extends ServiceImpl<SafetyChecklistMapper, SafetyChecklist>
        implements SafetyChecklistService {
}
