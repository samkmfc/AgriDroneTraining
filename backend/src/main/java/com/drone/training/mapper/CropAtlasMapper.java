package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.CropAtlas;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作物识别图谱 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface CropAtlasMapper extends BaseMapper<CropAtlas> {
}
