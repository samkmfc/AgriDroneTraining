package com.drone.training.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.drone.training.entity.DroneLicense;
import com.drone.training.mapper.DroneLicenseMapper;
import com.drone.training.service.DroneLicenseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 无人机执照服务实现
 *
 * @author 罗健 202308852
 */
@Service
public class DroneLicenseServiceImpl extends ServiceImpl<DroneLicenseMapper, DroneLicense>
        implements DroneLicenseService {

    @Value("${drone.license.expiring-days:60}")
    private long expiringDays;

    @Override
    public void computeStatus(DroneLicense license) {
        if (license == null || license.getExpiryDate() == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        LocalDate expiry = license.getExpiryDate();
        long remaining = ChronoUnit.DAYS.between(today, expiry);
        license.setRemainingDays(remaining);
        if (expiry.isBefore(today)) {
            license.setStatus("EXPIRED");
        } else if (remaining <= expiringDays) {
            license.setStatus("EXPIRING");
        } else {
            license.setStatus("NORMAL");
        }
    }
}
