package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.CropAtlas;
import com.drone.training.mapper.CropAtlasMapper;
import com.drone.training.service.CropAtlasService;
import org.springframework.stereotype.Service;

/**
 * 作物识别图谱 Service 实现
 *
 * @author 罗健 202308852
 */
@Service
public class CropAtlasServiceImpl extends ServiceImpl<CropAtlasMapper, CropAtlas> implements CropAtlasService {
}
