package com.drone.training.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.drone.training.entity.DroneLicense;
import com.drone.training.entity.Notification;
import com.drone.training.service.DroneLicenseService;
import com.drone.training.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 执照状态定时任务: 每日重新计算执照状态, 并对临期/过期执照生成预警通知。
 * 同时在应用启动时执行一次, 保证预警数据即时可见。
 *
 * @author 罗健 202308852
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LicenseScheduleTask implements ApplicationRunner {

    private final DroneLicenseService droneLicenseService;
    private final NotificationService notificationService;

    /** 每天凌晨 1 点执行 */
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduled() {
        refresh();
    }

    /** 启动时执行一次 */
    @Override
    public void run(ApplicationArguments args) {
        refresh();
    }

    public void refresh() {
        List<DroneLicense> list = droneLicenseService.list();
        int updated = 0;
        int notified = 0;
        for (DroneLicense license : list) {
            String old = license.getStatus();
            droneLicenseService.computeStatus(license);
            if (!Objects.equals(old, license.getStatus())) {
                DroneLicense update = new DroneLicense();
                update.setId(license.getId());
                update.setStatus(license.getStatus());
                droneLicenseService.updateById(update);
                updated++;
            }
            if (("EXPIRING".equals(license.getStatus()) || "EXPIRED".equals(license.getStatus()))
                    && createExpiryNotificationIfAbsent(license)) {
                notified++;
            }
        }
        log.info("执照状态刷新完成, 共 {} 条, 更新 {} 条, 新增预警通知 {} 条", list.size(), updated, notified);
    }

    /**
     * 若该执照尚无对应状态的预警通知, 则创建一条 (避免重复)
     */
    private boolean createExpiryNotificationIfAbsent(DroneLicense license) {
        String type = "EXPIRED".equals(license.getStatus()) ? "LICENSE_EXPIRED" : "LICENSE_EXPIRING";
        long exists = notificationService.count(Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getUserId, license.getUserId())
                .eq(Notification::getType, type)
                .eq(Notification::getRelatedId, license.getId()));
        if (exists > 0) {
            return false;
        }
        boolean expired = "EXPIRED".equals(license.getStatus());
        String title = expired ? "执照已过期" : "执照临期提醒";
        String content = expired
                ? "您的执照 " + license.getLicenseNo() + " 已于 " + license.getExpiryDate() + " 过期，请勿违规作业，尽快办理续期。"
                : "您的执照 " + license.getLicenseNo() + " 将于 " + license.getExpiryDate() + " 到期，请及时续期。";
        notificationService.notify(license.getUserId(), type, title, content, license.getId());
        return true;
    }
}
