package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.Result;
import com.drone.training.entity.Course;
import com.drone.training.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 课程后台管理控制器 (仅管理员)
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/courses")
public class CourseAdminController {

    private final CourseService courseService;

    /**
     * 课程分页列表 (全部状态)
     */
    @GetMapping
    public Result<Page<Course>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(required = false) String keyword) {
        Page<Course> page = courseService.page(new Page<>(pageNum, pageSize),
                Wrappers.<Course>lambdaQuery()
                        .eq(category != null && !category.isEmpty(), Course::getCategory, category)
                        .like(keyword != null && !keyword.isEmpty(), Course::getTitle, keyword)
                        .orderByAsc(Course::getSort)
                        .orderByDesc(Course::getCreateTime));
        return Result.success(page);
    }

    /**
     * 新增课程
     */
    @PostMapping
    public Result<Course> create(@RequestBody Course course) {
        course.setId(null);
        if (course.getViewCount() == null) {
            course.setViewCount(0);
        }
        if (course.getStatus() == null) {
            course.setStatus(1);
        }
        if (course.getSort() == null) {
            course.setSort(0);
        }
        courseService.save(course);
        return Result.success(course);
    }

    /**
     * 修改课程
     */
    @PutMapping("/{id}")
    public Result<Course> update(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        courseService.updateById(course);
        return Result.success(course);
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.success();
    }
}
