package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.FlightLog;
import com.drone.training.mapper.FlightLogMapper;
import com.drone.training.service.FlightLogService;
import org.springframework.stereotype.Service;

/**
 * 飞行作业日志 Service 实现
 *
 * @author 罗健 202308852
 */
@Service
public class FlightLogServiceImpl extends ServiceImpl<FlightLogMapper, FlightLog>
        implements FlightLogService {
}
