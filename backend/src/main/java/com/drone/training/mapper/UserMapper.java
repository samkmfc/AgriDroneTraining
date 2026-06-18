package com.drone.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.training.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 *
 * @author 罗健 202308852
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
