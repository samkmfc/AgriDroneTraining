package com.drone.training.security;

import com.alibaba.fastjson2.JSON;
import com.drone.training.common.Result;
import com.drone.training.common.ResultCode;
import org.springframework.stereotype.Component;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 未认证处理: 返回 401 JSON
 *
 * @author 罗健 202308852
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         org.springframework.security.core.AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(JSON.toJSONString(Result.error(ResultCode.UNAUTHORIZED)));
    }
}
