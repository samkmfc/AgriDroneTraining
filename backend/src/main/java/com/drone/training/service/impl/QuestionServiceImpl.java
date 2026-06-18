package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.Question;
import com.drone.training.mapper.QuestionMapper;
import com.drone.training.service.QuestionService;
import org.springframework.stereotype.Service;

/**
 * 题库 Service 实现
 *
 * @author 罗健 202308852
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
}
