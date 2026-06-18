package com.drone.training.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.Result;
import com.drone.training.entity.Exam;
import com.drone.training.entity.ExamRecord;
import com.drone.training.entity.User;
import com.drone.training.mapper.UserMapper;
import com.drone.training.service.ExamRecordService;
import com.drone.training.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 考试 管理端 Controller
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminExamController {

    private final ExamService examService;
    private final ExamRecordService examRecordService;
    private final UserMapper userMapper;

    /**
     * 试卷分页查询
     */
    @GetMapping("/exams")
    public Result<Page<Exam>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Exam> page = examService.page(new Page<>(pageNum, pageSize),
                Wrappers.<Exam>lambdaQuery().orderByDesc(Exam::getCreateTime));
        for (Exam exam : page.getRecords()) {
            exam.setQuestionCount(parseLength(exam.getQuestionIds()));
        }
        return Result.success(page);
    }

    /**
     * 新增试卷
     */
    @PostMapping("/exams")
    public Result<Exam> save(@RequestBody Exam exam) {
        normalizeQuestionIds(exam);
        examService.save(exam);
        return Result.success(exam);
    }

    /**
     * 修改试卷
     */
    @PutMapping("/exams/{id}")
    public Result<Exam> update(@PathVariable Long id, @RequestBody Exam exam) {
        exam.setId(id);
        normalizeQuestionIds(exam);
        examService.updateById(exam);
        return Result.success(exam);
    }

    /**
     * 删除试卷
     */
    @DeleteMapping("/exams/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        examService.removeById(id);
        return Result.success();
    }

    /**
     * 全部考试记录分页, 填充姓名与试卷标题
     */
    @GetMapping("/exam-records")
    public Result<Page<ExamRecord>> records(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) Long examId) {
        Page<ExamRecord> page = examRecordService.page(new Page<>(pageNum, pageSize),
                Wrappers.<ExamRecord>lambdaQuery()
                        .eq(examId != null, ExamRecord::getExamId, examId)
                        .orderByDesc(ExamRecord::getCreateTime));
        enrich(page.getRecords());
        return Result.success(page);
    }

    /**
     * questionIds 可能是 JSON 字符串或数组, 统一存为 JSON 字符串
     */
    private void normalizeQuestionIds(Exam exam) {
        String qids = exam.getQuestionIds();
        if (qids == null || qids.trim().isEmpty()) {
            exam.setQuestionIds("[]");
            return;
        }
        String trimmed = qids.trim();
        if (trimmed.startsWith("[")) {
            // 已是 JSON 数组字符串, 重新序列化以确保规范
            try {
                exam.setQuestionIds(JSON.toJSONString(JSON.parseArray(trimmed)));
            } catch (Exception e) {
                exam.setQuestionIds(trimmed);
            }
        } else {
            // 逗号分隔, 转为 JSON 数组
            List<Long> ids = java.util.Arrays.stream(trimmed.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            exam.setQuestionIds(JSON.toJSONString(ids));
        }
    }

    private Integer parseLength(String json) {
        if (json == null || json.trim().isEmpty()) {
            return 0;
        }
        try {
            return JSON.parseArray(json).size();
        } catch (Exception e) {
            return 0;
        }
    }

    private void enrich(List<ExamRecord> records) {
        if (records.isEmpty()) {
            return;
        }
        Set<Long> examIds = records.stream().map(ExamRecord::getExamId).collect(Collectors.toSet());
        Map<Long, String> titleMap = examService.listByIds(examIds).stream()
                .collect(Collectors.toMap(Exam::getId, Exam::getTitle, (a, b) -> a));

        Set<Long> userIds = records.stream().map(ExamRecord::getUserId).collect(Collectors.toSet());
        Map<Long, String> nameMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName, (a, b) -> a));

        for (ExamRecord r : records) {
            r.setExamTitle(titleMap.get(r.getExamId()));
            r.setRealName(nameMap.get(r.getUserId()));
        }
    }
}
