package com.drone.training.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.drone.training.common.Result;
import com.drone.training.dto.LoginDTO;
import com.drone.training.dto.RegisterDTO;
import com.drone.training.entity.StudentProfile;
import com.drone.training.entity.User;
import com.drone.training.security.JwtUtil;
import com.drone.training.security.LoginUser;
import com.drone.training.security.SecurityUtils;
import com.drone.training.service.StudentProfileService;
import com.drone.training.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证授权控制器
 *
 * @author 罗健 202308852
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final StudentProfileService studentProfileService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            User user = loginUser.getUser();

            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

            // 更新最后登录时间
            User update = new User();
            update.setId(user.getId());
            update.setLastLoginTime(LocalDateTime.now());
            userService.updateById(update);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("realName", user.getRealName());
            userInfo.put("role", user.getRole());
            userInfo.put("avatar", user.getAvatar());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", userInfo);
            return Result.success("登录成功", data);
        } catch (BadCredentialsException e) {
            return Result.error("用户名或密码错误");
        }
    }

    /**
     * 注册 (学员)
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        long count = userService.count(Wrappers.<User>lambdaQuery().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            return Result.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRole("STUDENT");
        user.setStatus(1);
        userService.save(user);

        // 创建空的学员档案
        StudentProfile profile = new StudentProfile();
        profile.setUserId(user.getId());
        profile.setTrainingStatus("TRAINING");
        profile.setEnrollDate(LocalDate.now());
        studentProfileService.save(profile);

        return Result.success("注册成功", null);
    }

    /**
     * 当前登录用户信息
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        User user = userService.getById(SecurityUtils.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("role", user.getRole());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("phone", user.getPhone());
        userInfo.put("email", user.getEmail());
        userInfo.put("gender", user.getGender());
        return Result.success(userInfo);
    }

    /**
     * 登出 (无状态, 前端清除 token 即可)
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
