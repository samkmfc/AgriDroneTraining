package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.PesticideRatio;
import com.drone.training.mapper.PesticideRatioMapper;
import com.drone.training.service.PesticideRatioService;
import org.springframework.stereotype.Service;

/**
 * 农药配比 Service 实现
 *
 * @author 罗健 202308852
 */
@Service
public class PesticideRatioServiceImpl extends ServiceImpl<PesticideRatioMapper, PesticideRatio> implements PesticideRatioService {
}
