package com.drone.training.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.drone.training.entity.DroneLicense;

/**
 * 无人机执照服务
 *
 * @author 罗健 202308852
 */
public interface DroneLicenseService extends IService<DroneLicense> {

    /**
     * 计算执照状态(EXPIRED / EXPIRING / NORMAL)并设置距到期天数 remainingDays
     */
    void computeStatus(DroneLicense license);
}
