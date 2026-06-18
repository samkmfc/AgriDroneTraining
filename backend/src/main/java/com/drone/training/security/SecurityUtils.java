package com.drone.training.security;

import com.drone.training.common.BusinessException;
import com.drone.training.common.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类: 获取当前登录用户信息
 *
 * @author 罗健 202308852
 */
public class SecurityUtils {

    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return (LoginUser) authentication.getPrincipal();
    }

    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    public static String getUsername() {
        return getLoginUser().getUsername();
    }

    public static String getRole() {
        return getLoginUser().getRole();
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }
}
