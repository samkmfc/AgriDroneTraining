package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.Regulation;
import com.drone.training.mapper.RegulationMapper;
import com.drone.training.service.RegulationService;
import org.springframework.stereotype.Service;

/**
 * 法规/政策/资讯 Service 实现
 *
 * @author 罗健 202308852
 */
@Service
public class RegulationServiceImpl extends ServiceImpl<RegulationMapper, Regulation> implements RegulationService {
}
