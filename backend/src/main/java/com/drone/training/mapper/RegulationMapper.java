package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.Regulation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 法规/政策/资讯 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface RegulationMapper extends BaseMapper<Regulation> {
}
