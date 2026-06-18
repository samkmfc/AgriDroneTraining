package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.Result;
import com.drone.training.dto.PasswordDTO;
import com.drone.training.entity.StudentProfile;
import com.drone.training.entity.User;
import com.drone.training.security.SecurityUtils;
import com.drone.training.service.StudentProfileService;
import com.drone.training.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器 (个人中心 / 管理端用户管理 / 学员档案)
 *
 * @author 罗健 202308852
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final StudentProfileService studentProfileService;
    private final PasswordEncoder passwordEncoder;

    // ==================== 个人中心 ====================

    /**
     * 更新个人资料
     */
    @PutMapping("/user/profile")
    public Result<Void> updateProfile(@RequestBody User body) {
        User update = new User();
        update.setId(SecurityUtils.getUserId());
        update.setRealName(body.getRealName());
        update.setNickname(body.getNickname());
        update.setPhone(body.getPhone());
        update.setEmail(body.getEmail());
        update.setGender(body.getGender());
        update.setAvatar(body.getAvatar());
        userService.updateById(update);
        return Result.success("更新成功", null);
    }

    /**
     * 修改密码
     */
    @PutMapping("/user/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordDTO dto) {
        User user = userService.getById(SecurityUtils.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return Result.error("原密码错误");
        }
        User update = new User();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userService.updateById(update);
        return Result.success("密码修改成功", null);
    }

    // ==================== 管理端用户管理 ====================

    /**
     * 用户分页列表
     */
    @GetMapping("/admin/users")
    public Result<Page<User>> listUsers(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String role,
                                        @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getPhone, keyword));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> page = userService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    /**
     * 创建用户
     */
    @PostMapping("/admin/users")
    public Result<Void> createUser(@RequestBody User user) {
        if (!StringUtils.hasText(user.getUsername())) {
            return Result.error("用户名不能为空");
        }
        long count = userService.count(Wrappers.<User>lambdaQuery().eq(User::getUsername, user.getUsername()));
        if (count > 0) {
            return Result.error("用户名已存在");
        }
        String rawPwd = StringUtils.hasText(user.getPassword()) ? user.getPassword() : "123456";
        user.setPassword(passwordEncoder.encode(rawPwd));
        if (!StringUtils.hasText(user.getRole())) {
            user.setRole("STUDENT");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        userService.save(user);
        return Result.success("创建成功", null);
    }

    /**
     * 更新用户
     */
    @PutMapping("/admin/users/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        // 不通过该接口修改密码
        user.setPassword(null);
        userService.updateById(user);
        return Result.success("更新成功", null);
    }

    /**
     * 删除用户 (逻辑删除)
     */
    @DeleteMapping("/admin/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/admin/users/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User update = new User();
        update.setId(id);
        update.setStatus(status);
        userService.updateById(update);
        return Result.success("操作成功", null);
    }

    // ==================== 学员档案 ====================

    /**
     * 学员查看自己的档案 (含用户信息)
     */
    @GetMapping("/student/profile")
    public Result<Map<String, Object>> myProfile() {
        return Result.success(buildProfile(SecurityUtils.getUserId()));
    }

    /**
     * 学员更新自己的档案
     */
    @PutMapping("/student/profile")
    public Result<Void> updateMyProfile(@RequestBody StudentProfile body) {
        Long userId = SecurityUtils.getUserId();
        StudentProfile profile = studentProfileService.getOne(
                Wrappers.<StudentProfile>lambdaQuery().eq(StudentProfile::getUserId, userId));
        if (profile == null) {
            profile = new StudentProfile();
            profile.setUserId(userId);
            profile.setTrainingStatus("TRAINING");
        }
        profile.setIdCard(body.getIdCard());
        profile.setAddress(body.getAddress());
        profile.setEmergencyContact(body.getEmergencyContact());
        profile.setEmergencyPhone(body.getEmergencyPhone());
        profile.setEducation(body.getEducation());
        studentProfileService.saveOrUpdate(profile);
        return Result.success("更新成功", null);
    }

    /**
     * 管理端查看指定学员档案 (含用户信息)
     */
    @GetMapping("/admin/students/{userId}")
    public Result<Map<String, Object>> studentProfile(@PathVariable Long userId) {
        return Result.success(buildProfile(userId));
    }

    /**
     * 构建 用户信息 + 学员档案 的合并视图
     */
    private Map<String, Object> buildProfile(Long userId) {
        Map<String, Object> map = new HashMap<>();
        User user = userService.getById(userId);
        if (user != null) {
            map.put("userId", user.getId());
            map.put("username", user.getUsername());
            map.put("realName", user.getRealName());
            map.put("nickname", user.getNickname());
            map.put("gender", user.getGender());
            map.put("phone", user.getPhone());
            map.put("email", user.getEmail());
            map.put("avatar", user.getAvatar());
            map.put("role", user.getRole());
            map.put("status", user.getStatus());
        }
        StudentProfile profile = studentProfileService.getOne(
                Wrappers.<StudentProfile>lambdaQuery().eq(StudentProfile::getUserId, userId));
        if (profile != null) {
            map.put("profileId", profile.getId());
            map.put("idCard", profile.getIdCard());
            map.put("address", profile.getAddress());
            map.put("emergencyContact", profile.getEmergencyContact());
            map.put("emergencyPhone", profile.getEmergencyPhone());
            map.put("education", profile.getEducation());
            map.put("trainingStatus", profile.getTrainingStatus());
            map.put("enrollDate", profile.getEnrollDate());
            map.put("remark", profile.getRemark());
        }
        return map;
    }
}
