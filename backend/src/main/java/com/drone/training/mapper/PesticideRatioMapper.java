package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.PesticideRatio;
import org.apache.ibatis.annotations.Mapper;

/**
 * 农药配比 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface PesticideRatioMapper extends BaseMapper<PesticideRatio> {
}
