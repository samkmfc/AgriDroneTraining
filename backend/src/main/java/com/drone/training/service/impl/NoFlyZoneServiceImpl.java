package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.NoFlyZone;
import com.drone.training.mapper.NoFlyZoneMapper;
import com.drone.training.service.NoFlyZoneService;
import org.springframework.stereotype.Service;

/**
 * 禁飞区 Service 实现
 *
 * @author 罗健 202308852
 */
@Service
public class NoFlyZoneServiceImpl extends ServiceImpl<NoFlyZoneMapper, NoFlyZone> implements NoFlyZoneService {
}
