package com.drone.training.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.training.common.Result;
import com.drone.training.entity.DroneLicense;
import com.drone.training.entity.User;
import com.drone.training.security.SecurityUtils;
import com.drone.training.service.DroneLicenseService;
import com.drone.training.service.NotificationService;
import com.drone.training.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

/**
 * 无人机执照控制器 (有效期监控 / 临期预警)
 *
 * @author 罗健 202308852
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DroneLicenseController {

    private final DroneLicenseService droneLicenseService;
    private final UserService userService;
    private final NotificationService notificationService;

    // ==================== 学员端 ====================

    /**
     * 学员查看自己的执照 (实时计算状态 + 剩余天数, 并持久化更新后的状态)
     */
    @GetMapping("/license/my")
    public Result<List<DroneLicense>> myLicenses() {
        List<DroneLicense> list = droneLicenseService.list(
                Wrappers.<DroneLicense>lambdaQuery().eq(DroneLicense::getUserId, SecurityUtils.getUserId()));
        for (DroneLicense license : list) {
            String old = license.getStatus();
            droneLicenseService.computeStatus(license);
            if (!java.util.Objects.equals(old, license.getStatus())) {
                DroneLicense update = new DroneLicense();
                update.setId(license.getId());
                update.setStatus(license.getStatus());
                droneLicenseService.updateById(update);
            }
        }
        return Result.success(list);
    }

    // ==================== 管理端 ====================

    /**
     * 执照分页列表
     */
    @GetMapping("/admin/licenses")
    public Result<Page<DroneLicense>> listLicenses(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String status) {
        LambdaQueryWrapper<DroneLicense> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(DroneLicense::getLicenseNo, keyword)
                    .or().like(DroneLicense::getDroneModel, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(DroneLicense::getStatus, status);
        }
        wrapper.orderByDesc(DroneLicense::getCreateTime);
        Page<DroneLicense> page = droneLicenseService.page(new Page<>(pageNum, pageSize), wrapper);
        for (DroneLicense license : page.getRecords()) {
            droneLicenseService.computeStatus(license);
            enrichRealName(license);
        }
        return Result.success(page);
    }

    /**
     * 创建执照
     */
    @PostMapping("/admin/licenses")
    public Result<Void> createLicense(@RequestBody DroneLicense license) {
        droneLicenseService.computeStatus(license);
        droneLicenseService.save(license);
        return Result.success("创建成功", null);
    }

    /**
     * 更新执照
     */
    @PutMapping("/admin/licenses/{id}")
    public Result<Void> updateLicense(@PathVariable Long id, @RequestBody DroneLicense license) {
        license.setId(id);
        droneLicenseService.computeStatus(license);
        droneLicenseService.updateById(license);
        return Result.success("更新成功", null);
    }

    /**
     * 删除执照
     */
    @DeleteMapping("/admin/licenses/{id}")
    public Result<Void> deleteLicense(@PathVariable Long id) {
        droneLicenseService.removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 一键续期: 在原到期日(或今日, 取较晚者)基础上延长 months 个月, 重新计算状态并通知学员
     */
    @PutMapping("/admin/licenses/{id}/renew")
    public Result<DroneLicense> renewLicense(@PathVariable Long id,
                                             @RequestParam(defaultValue = "24") Integer months) {
        DroneLicense license = droneLicenseService.getById(id);
        if (license == null) {
            return Result.error("执照不存在");
        }
        java.time.LocalDate base = license.getExpiryDate();
        java.time.LocalDate today = java.time.LocalDate.now();
        if (base == null || base.isBefore(today)) {
            base = today;
        }
        license.setExpiryDate(base.plusMonths(months));
        droneLicenseService.computeStatus(license);
        droneLicenseService.updateById(license);
        notificationService.notify(license.getUserId(), "SYSTEM", "执照已续期",
                "您的执照 " + license.getLicenseNo() + " 已续期至 " + license.getExpiryDate() + "，请知悉。", license.getId());
        return Result.success("续期成功", license);
    }

    /**
     * 临期预警列表: EXPIRING + EXPIRED, 按到期日期升序, 含学员姓名与剩余天数
     */
    @GetMapping("/admin/licenses/expiring")
    public Result<List<DroneLicense>> expiringLicenses() {
        List<DroneLicense> list = droneLicenseService.list();
        list.forEach(droneLicenseService::computeStatus);
        List<DroneLicense> result = list.stream()
                .filter(l -> "EXPIRING".equals(l.getStatus()) || "EXPIRED".equals(l.getStatus()))
                .sorted(Comparator.comparing(DroneLicense::getExpiryDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .peek(this::enrichRealName)
                .collect(java.util.stream.Collectors.toList());
        return Result.success(result);
    }

    /**
     * 填充学员姓名
     */
    private void enrichRealName(DroneLicense license) {
        if (license.getUserId() == null) {
            return;
        }
        User user = userService.getById(license.getUserId());
        if (user != null) {
            license.setRealName(user.getRealName());
        }
    }
}
