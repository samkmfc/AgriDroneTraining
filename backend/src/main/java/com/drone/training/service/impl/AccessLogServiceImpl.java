package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.AccessLog;
import com.drone.training.mapper.AccessLogMapper;
import com.drone.training.security.SecurityUtils;
import com.drone.training.service.AccessLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 访问日志 Service 实现
 *
 * @author 罗健 202308852
 */
@Slf4j
@Service
public class AccessLogServiceImpl extends ServiceImpl<AccessLogMapper, AccessLog> implements AccessLogService {

    @Override
    public void record(String module, String action) {
        try {
            AccessLog accessLog = new AccessLog();
            try {
                accessLog.setUserId(SecurityUtils.getUserId());
                accessLog.setUsername(SecurityUtils.getUsername());
            } catch (Exception ignore) {
                // 未登录或无安全上下文时, 仍记录访问 (匿名)
            }
            accessLog.setModule(module);
            accessLog.setAction(action);
            accessLog.setIp(resolveIp());
            accessLog.setCreateTime(LocalDateTime.now());
            save(accessLog);
        } catch (Exception e) {
            // 访问日志为非关键路径, 失败不影响主流程
            log.warn("记录访问日志失败: module={}, action={}", module, action, e);
        }
    }

    /**
     * 从当前请求解析客户端 IP
     */
    private String resolveIp() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                int idx = ip.indexOf(',');
                return idx > 0 ? ip.substring(0, idx).trim() : ip.trim();
            }
            ip = request.getHeader("X-Real-IP");
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
