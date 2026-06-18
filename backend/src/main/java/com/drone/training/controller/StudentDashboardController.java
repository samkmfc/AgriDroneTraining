package com.drone.training.controller;

import com.drone.training.common.Result;
import com.drone.training.mapper.StatisticsMapper;
import com.drone.training.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学员仪表盘 (非 /admin 路径, 需认证)
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/student/dashboard")
public class StudentDashboardController {

    private final StatisticsMapper statisticsMapper;

    /**
     * 当前学员仪表盘汇总数据
     */
    @GetMapping
    public Result<Map<String, Object>> dashboard() {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> data = new HashMap<>();

        long licenseCount = statisticsMapper.countMyLicense(userId);
        data.put("licenseCount", licenseCount);
        data.put("licenseStatus", resolveLicenseStatus(statisticsMapper.myLicenseStatuses(userId)));
        data.put("nearestExpiryDays", licenseCount == 0 ? null : statisticsMapper.myNearestExpiryDays(userId));

        data.put("courseCount", statisticsMapper.countActiveCourses());
        data.put("finishedCourseCount", statisticsMapper.countMyFinishedCourses(userId));
        data.put("examCount", statisticsMapper.countMyExams(userId));
        data.put("passedExamCount", statisticsMapper.countMyPassedExams(userId));
        data.put("flightLogCount", statisticsMapper.countMyFlightLogs(userId));
        data.put("pendingLogCount", statisticsMapper.countMyPendingLogs(userId));
        data.put("approvedLogCount", statisticsMapper.countMyApprovedLogs(userId));
        return Result.success(data);
    }

    /**
     * 取学员最关切的执照状态: EXPIRED > EXPIRING > NORMAL, 无执照返回 NONE
     */
    private String resolveLicenseStatus(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return "NONE";
        }
        boolean hasNormal = false;
        boolean hasExpiring = false;
        for (String status : statuses) {
            if ("EXPIRED".equals(status)) {
                return "EXPIRED";
            }
            if ("EXPIRING".equals(status)) {
                hasExpiring = true;
            } else if ("NORMAL".equals(status)) {
                hasNormal = true;
            }
        }
        if (hasExpiring) {
            return "EXPIRING";
        }
        return hasNormal ? "NORMAL" : "NONE";
    }
}
