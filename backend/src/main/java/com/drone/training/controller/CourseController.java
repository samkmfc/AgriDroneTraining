package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.Result;
import com.drone.training.entity.Course;
import com.drone.training.entity.LearningRecord;
import com.drone.training.security.SecurityUtils;
import com.drone.training.service.CourseService;
import com.drone.training.service.LearningRecordService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程 / 在线学习 控制器
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final LearningRecordService learningRecordService;

    /**
     * 课程分页列表 (学生/公开端: 仅 status=1)
     */
    @GetMapping("/courses")
    public Result<Page<Course>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(required = false) String keyword) {
        Page<Course> page = courseService.page(new Page<>(pageNum, pageSize),
                Wrappers.<Course>lambdaQuery()
                        .eq(Course::getStatus, 1)
                        .eq(category != null && !category.isEmpty(), Course::getCategory, category)
                        .like(keyword != null && !keyword.isEmpty(), Course::getTitle, keyword)
                        .orderByAsc(Course::getSort)
                        .orderByDesc(Course::getCreateTime));
        return Result.success(page);
    }

    /**
     * 课程详情: viewCount + 1; 学生附带学习进度
     */
    @GetMapping("/courses/{id}")
    public Result<Course> detail(@PathVariable Long id) {
        Course course = courseService.getById(id);
        if (course == null) {
            return Result.error("课程不存在");
        }
        // 浏览量 +1
        Course update = new Course();
        update.setId(id);
        update.setViewCount((course.getViewCount() == null ? 0 : course.getViewCount()) + 1);
        courseService.updateById(update);
        course.setViewCount(update.getViewCount());

        // 学生附带学习进度
        if (!SecurityUtils.isAdmin()) {
            LearningRecord record = learningRecordService.getOne(
                    Wrappers.<LearningRecord>lambdaQuery()
                            .eq(LearningRecord::getUserId, SecurityUtils.getUserId())
                            .eq(LearningRecord::getCourseId, id)
                            .last("LIMIT 1"));
            course.setProgress(record == null ? 0 : record.getProgress());
        }
        return Result.success(course);
    }

    /**
     * 学习课程: upsert 学习记录 (user_id + course_id 唯一)
     */
    @PostMapping("/courses/{id}/study")
    public Result<LearningRecord> study(@PathVariable Long id, @RequestBody StudyRequest request) {
        Course course = courseService.getById(id);
        if (course == null) {
            return Result.error("课程不存在");
        }
        Long userId = SecurityUtils.getUserId();
        Integer progress = request.getProgress() == null ? 0 : request.getProgress();
        if (progress > 100) {
            progress = 100;
        }

        LearningRecord record = learningRecordService.getOne(
                Wrappers.<LearningRecord>lambdaQuery()
                        .eq(LearningRecord::getUserId, userId)
                        .eq(LearningRecord::getCourseId, id)
                        .last("LIMIT 1"));
        if (record == null) {
            record = new LearningRecord();
            record.setUserId(userId);
            record.setCourseId(id);
            record.setProgress(progress);
            record.setFinished(progress >= 100 ? 1 : 0);
            record.setLastStudyTime(LocalDateTime.now());
            learningRecordService.save(record);
        } else {
            record.setProgress(progress);
            record.setFinished(progress >= 100 ? 1 : 0);
            record.setLastStudyTime(LocalDateTime.now());
            learningRecordService.updateById(record);
        }
        return Result.success(record);
    }

    /**
     * 我的学习记录: 附带课程标题/封面/分类
     */
    @GetMapping("/learning/my")
    public Result<List<Map<String, Object>>> myLearning() {
        Long userId = SecurityUtils.getUserId();
        List<LearningRecord> records = learningRecordService.list(
                Wrappers.<LearningRecord>lambdaQuery()
                        .eq(LearningRecord::getUserId, userId)
                        .orderByDesc(LearningRecord::getLastStudyTime));
        if (records.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        List<Long> courseIds = records.stream()
                .map(LearningRecord::getCourseId)
                .collect(Collectors.toList());
        Map<Long, Course> courseMap = courseService.listByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c, (a, b) -> a));

        List<Map<String, Object>> result = new ArrayList<>();
        for (LearningRecord record : records) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", record.getId());
            map.put("courseId", record.getCourseId());
            map.put("progress", record.getProgress());
            map.put("finished", record.getFinished());
            map.put("lastStudyTime", record.getLastStudyTime());
            map.put("createTime", record.getCreateTime());
            Course course = courseMap.get(record.getCourseId());
            if (course != null) {
                map.put("courseTitle", course.getTitle());
                map.put("courseCover", course.getCover());
                map.put("courseCategory", course.getCategory());
            }
            result.add(map);
        }
        return Result.success(result);
    }

    /**
     * 学习进度请求体
     */
    @Data
    public static class StudyRequest {
        private Integer progress;
    }
}
