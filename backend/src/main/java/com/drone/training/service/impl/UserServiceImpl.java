package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.User;
import com.drone.training.mapper.UserMapper;
import com.drone.training.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 *
 * @author 罗健 202308852
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
