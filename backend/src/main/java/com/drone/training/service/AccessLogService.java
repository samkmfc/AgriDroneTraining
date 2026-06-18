package com.drone.training.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drone.training.entity.AccessLog;

/**
 * 访问日志 Service
 *
 * @author 罗健 202308852
 */
public interface AccessLogService extends IService<AccessLog> {

    /**
     * 记录一条访问日志 (使用当前登录用户), 内部捕获异常, 永不抛出
     *
     * @param module 访问模块
     * @param action 操作
     */
    void record(String module, String action);
}
