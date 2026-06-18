package com.drone.training.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.drone.training.common.BusinessException;
import com.drone.training.common.Result;
import com.drone.training.dto.ExamSubmitDTO;
import com.drone.training.entity.Exam;
import com.drone.training.entity.ExamRecord;
import com.drone.training.entity.Question;
import com.drone.training.security.SecurityUtils;
import com.drone.training.service.ExamRecordService;
import com.drone.training.service.ExamService;
import com.drone.training.service.QuestionService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试 / 场景化测试 Controller
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final QuestionService questionService;
    private final ExamRecordService examRecordService;

    /**
     * 启用中的试卷列表 (不含题目内容), 每条带题目数量
     */
    @GetMapping("/exams")
    public Result<List<Exam>> list() {
        List<Exam> exams = examService.list(Wrappers.<Exam>lambdaQuery()
                .eq(Exam::getStatus, 1)
                .orderByDesc(Exam::getCreateTime));
        for (Exam exam : exams) {
            exam.setQuestionCount(parseQuestionIds(exam.getQuestionIds()).size());
        }
        return Result.success(exams);
    }

    /**
     * 获取试卷及其题目用于答题 (剥离正确答案与解析)
     */
    @GetMapping("/exams/{id}/paper")
    public Result<Map<String, Object>> paper(@PathVariable Long id) {
        Exam exam = examService.getById(id);
        if (exam == null) {
            throw new BusinessException("试卷不存在");
        }
        List<Long> questionIds = parseQuestionIds(exam.getQuestionIds());
        List<Question> questions = new ArrayList<>();
        if (!questionIds.isEmpty()) {
            questions = questionService.listByIds(questionIds);
            // 按试卷题目顺序排列
            Map<Long, Question> map = questions.stream()
                    .collect(Collectors.toMap(Question::getId, q -> q, (a, b) -> a));
            questions = questionIds.stream()
                    .map(map::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            // 剥离答案与解析, 防止学员看到
            for (Question q : questions) {
                q.setAnswer(null);
                q.setAnalysis(null);
            }
        }
        exam.setQuestionCount(questionIds.size());
        Map<String, Object> data = new HashMap<>();
        data.put("exam", exam);
        data.put("questions", questions);
        return Result.success(data);
    }

    /**
     * 提交试卷, 服务端判分
     */
    @PostMapping("/exams/{id}/submit")
    public Result<Map<String, Object>> submit(@PathVariable Long id, @RequestBody ExamSubmitDTO dto) {
        Exam exam = examService.getById(id);
        if (exam == null) {
            throw new BusinessException("试卷不存在");
        }
        List<Long> questionIds = parseQuestionIds(exam.getQuestionIds());
        Map<Long, Question> questionMap = new HashMap<>();
        if (!questionIds.isEmpty()) {
            for (Question q : questionService.listByIds(questionIds)) {
                questionMap.put(q.getId(), q);
            }
        }

        // 提交的答案: questionId -> answer
        Map<Long, String> submitted = new HashMap<>();
        if (dto.getAnswers() != null) {
            for (ExamSubmitDTO.AnswerItem item : dto.getAnswers()) {
                if (item != null && item.getQuestionId() != null) {
                    submitted.put(item.getQuestionId(), item.getAnswer());
                }
            }
        }

        int score = 0;
        int correctCount = 0;
        int totalCount = questionIds.size();
        for (Long qid : questionIds) {
            Question q = questionMap.get(qid);
            if (q == null) {
                continue;
            }
            String userAnswer = submitted.get(qid);
            if (isCorrect(q.getType(), q.getAnswer(), userAnswer)) {
                correctCount++;
                score += q.getScore() == null ? 0 : q.getScore();
            }
        }

        Integer passScore = exam.getPassScore() == null ? 0 : exam.getPassScore();
        boolean passed = score >= passScore;

        ExamRecord record = new ExamRecord();
        record.setExamId(id);
        record.setUserId(SecurityUtils.getUserId());
        record.setScore(score);
        record.setPassed(passed ? 1 : 0);
        record.setAnswers(JSON.toJSONString(dto.getAnswers() == null ? Collections.emptyList() : dto.getAnswers()));
        record.setDuration(dto.getDuration());
        record.setStartTime(dto.getStartTime());
        record.setSubmitTime(LocalDateTime.now());
        examRecordService.save(record);

        Map<String, Object> data = new HashMap<>();
        data.put("score", score);
        data.put("passed", passed);
        data.put("totalScore", exam.getTotalScore());
        data.put("passScore", passScore);
        data.put("correctCount", correctCount);
        data.put("totalCount", totalCount);
        return Result.success(data);
    }

    /**
     * 我的考试记录
     */
    @GetMapping("/exam-records/my")
    public Result<List<ExamRecord>> my() {
        List<ExamRecord> records = examRecordService.list(Wrappers.<ExamRecord>lambdaQuery()
                .eq(ExamRecord::getUserId, SecurityUtils.getUserId())
                .orderByDesc(ExamRecord::getCreateTime));
        enrichExamTitle(records);
        return Result.success(records);
    }

    /**
     * 考试记录详情 (答案解析回顾): 返回每题的题干/选项/正确答案/解析/我的作答/是否正确
     */
    @GetMapping("/exam-records/{id}")
    public Result<Map<String, Object>> recordDetail(@PathVariable Long id) {
        ExamRecord record = examRecordService.getById(id);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (!record.getUserId().equals(SecurityUtils.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("无权查看该记录");
        }
        Exam exam = examService.getById(record.getExamId());

        // 我的作答 questionId -> answer
        Map<Long, String> myAnswers = new HashMap<>();
        try {
            JSONArray arr = JSON.parseArray(record.getAnswers());
            if (arr != null) {
                for (int i = 0; i < arr.size(); i++) {
                    com.alibaba.fastjson2.JSONObject o = arr.getJSONObject(i);
                    if (o != null && o.get("questionId") != null) {
                        myAnswers.put(o.getLong("questionId"), o.getString("answer"));
                    }
                }
            }
        } catch (Exception ignored) {
        }

        List<Long> questionIds = exam == null ? new ArrayList<>() : parseQuestionIds(exam.getQuestionIds());
        Map<Long, Question> qMap = new HashMap<>();
        if (!questionIds.isEmpty()) {
            for (Question q : questionService.listByIds(questionIds)) {
                qMap.put(q.getId(), q);
            }
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (Long qid : questionIds) {
            Question q = qMap.get(qid);
            if (q == null) {
                continue;
            }
            String my = myAnswers.get(qid);
            Map<String, Object> item = new HashMap<>();
            item.put("id", q.getId());
            item.put("type", q.getType());
            item.put("category", q.getCategory());
            item.put("content", q.getContent());
            item.put("options", q.getOptions());
            item.put("answer", q.getAnswer());
            item.put("analysis", q.getAnalysis());
            item.put("score", q.getScore());
            item.put("myAnswer", my);
            item.put("correct", isCorrect(q.getType(), q.getAnswer(), my));
            items.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("record", record);
        data.put("examTitle", exam == null ? null : exam.getTitle());
        data.put("totalScore", exam == null ? null : exam.getTotalScore());
        data.put("passScore", exam == null ? null : exam.getPassScore());
        data.put("questions", items);
        return Result.success(data);
    }

    /**
     * 解析题目ID集合 JSON
     */
    private List<Long> parseQuestionIds(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            JSONArray arr = JSON.parseArray(json);
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                ids.add(arr.getLong(i));
            }
            return ids;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 判分: 对答案排序后比较 (MULTI 答案为逗号拼接的字母)
     */
    private boolean isCorrect(String type, String correct, String userAnswer) {
        if (correct == null) {
            return false;
        }
        if (userAnswer == null) {
            return false;
        }
        return normalize(correct).equals(normalize(userAnswer));
    }

    /**
     * 规范化答案: 去空白, 大写, 按字母排序 (对单选/判断也安全)
     */
    private String normalize(String answer) {
        String[] parts = answer.replace(" ", "").toUpperCase().split(",");
        return Arrays.stream(parts)
                .filter(s -> !s.isEmpty())
                .sorted()
                .collect(Collectors.joining(","));
    }

    /**
     * 填充试卷标题 (非数据库字段)
     */
    private void enrichExamTitle(List<ExamRecord> records) {
        if (records.isEmpty()) {
            return;
        }
        Set<Long> examIds = records.stream().map(ExamRecord::getExamId).collect(Collectors.toSet());
        Map<Long, String> titleMap = examService.listByIds(examIds).stream()
                .collect(Collectors.toMap(Exam::getId, Exam::getTitle, (a, b) -> a));
        for (ExamRecord r : records) {
            r.setExamTitle(titleMap.get(r.getExamId()));
        }
    }
}
