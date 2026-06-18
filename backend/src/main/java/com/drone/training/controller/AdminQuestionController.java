package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.Result;
import com.drone.training.entity.Question;
import com.drone.training.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 题库 管理端 Controller
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/questions")
public class AdminQuestionController {

    private final QuestionService questionService;

    /**
     * 分页查询
     */
    @GetMapping
    public Result<Page<Question>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String type,
                                       @RequestParam(required = false) String category,
                                       @RequestParam(required = false) String keyword) {
        Page<Question> page = questionService.page(new Page<>(pageNum, pageSize),
                Wrappers.<Question>lambdaQuery()
                        .eq(StringUtils.hasText(type), Question::getType, type)
                        .eq(StringUtils.hasText(category), Question::getCategory, category)
                        .like(StringUtils.hasText(keyword), Question::getContent, keyword)
                        .orderByDesc(Question::getCreateTime));
        return Result.success(page);
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Question> save(@RequestBody Question question) {
        questionService.save(question);
        return Result.success(question);
    }

    /**
     * 修改
     */
    @PutMapping("/{id}")
    public Result<Question> update(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        questionService.updateById(question);
        return Result.success(question);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.removeById(id);
        return Result.success();
    }
}
