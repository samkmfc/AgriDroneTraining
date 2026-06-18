package com.drone.training.controller;

import com.drone.training.common.Result;
import com.drone.training.service.AccessLogService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 访问日志记录 (前端上报页面访问)
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/access")
public class AccessLogController {

    private final AccessLogService accessLogService;

    /**
     * 记录一条访问日志 (任意已认证角色)
     */
    @PostMapping("/log")
    public Result<Void> log(@RequestBody AccessLogRequest request) {
        accessLogService.record(request.getModule(), request.getAction());
        return Result.success();
    }

    @Data
    public static class AccessLogRequest {
        private String module;
        private String action;
    }
}
