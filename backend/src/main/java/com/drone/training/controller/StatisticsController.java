package com.drone.training.controller;

import com.drone.training.common.Result;
import com.drone.training.mapper.StatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计 / 仪表盘 (ECharts 数据源)
 *
 * @author 罗健 202308852
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/statistics")
public class StatisticsController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int TREND_DAYS = 14;

    private final StatisticsMapper statisticsMapper;

    /**
     * 管理端总览
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("studentCount", statisticsMapper.countStudents());
        data.put("courseCount", statisticsMapper.countCourses());
        data.put("flightLogCount", statisticsMapper.countFlightLogs());
        data.put("pendingAuditCount", statisticsMapper.countPendingAudit());
        data.put("expiringLicenseCount", statisticsMapper.countExpiringLicense());

        long examTotal = statisticsMapper.countExamRecords();
        long examPassed = statisticsMapper.countExamPassed();
        double passRate = examTotal == 0 ? 0d
                : BigDecimal.valueOf(examPassed * 100.0 / examTotal)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();
        data.put("examPassRate", passRate);

        data.put("totalVisits", statisticsMapper.countAccessLogs());
        data.put("regulationCount", statisticsMapper.countRegulations());
        data.put("aiDiagnosisCount", statisticsMapper.countAiDiagnosis());
        return Result.success(data);
    }

    /**
     * 各试卷通过率统计 (柱状图)
     */
    @GetMapping("/exam-pass")
    public Result<List<Map<String, Object>>> examPass() {
        List<Map<String, Object>> rows = statisticsMapper.examPassStats();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            long total = toLong(row.get("total"));
            long passed = toLong(row.get("passed"));
            double rate = total == 0 ? 0d
                    : BigDecimal.valueOf(passed * 100.0 / total)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("examTitle", row.get("examTitle"));
            item.put("total", total);
            item.put("passed", passed);
            item.put("passRate", rate);
            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * 最近14天每日飞行日志提交趋势 (缺失天补0, 升序)
     */
    @GetMapping("/flight-log-trend")
    public Result<List<Map<String, Object>>> flightLogTrend() {
        LocalDate start = LocalDate.now().minusDays(TREND_DAYS - 1);
        List<Map<String, Object>> rows = statisticsMapper.flightLogDailyCount(start.format(DAY));
        return Result.success(buildTrend(rows, start));
    }

    /**
     * 最近14天每日访问量趋势 (缺失天补0, 升序)
     */
    @GetMapping("/visit-trend")
    public Result<List<Map<String, Object>>> visitTrend() {
        LocalDate start = LocalDate.now().minusDays(TREND_DAYS - 1);
        List<Map<String, Object>> rows = statisticsMapper.accessLogDailyCount(start.format(DAY));
        return Result.success(buildTrend(rows, start));
    }

    /**
     * 执照状态分布 (饼图) — 保证 NORMAL/EXPIRING/EXPIRED 三类都存在
     */
    @GetMapping("/license-status")
    public Result<List<Map<String, Object>>> licenseStatus() {
        Map<String, Long> counts = new HashMap<>();
        for (Map<String, Object> row : statisticsMapper.licenseStatusStats()) {
            Object status = row.get("status");
            if (status != null) {
                counts.put(status.toString(), toLong(row.get("count")));
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (String status : new String[]{"NORMAL", "EXPIRING", "EXPIRED"}) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("status", status);
            item.put("count", counts.getOrDefault(status, 0L));
            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * 各机型培训人数 (去重学员, 前10)
     */
    @GetMapping("/model-distribution")
    public Result<List<Map<String, Object>>> modelDistribution() {
        return Result.success(statisticsMapper.modelDistribution());
    }

    /* ============================ 私有工具 ============================ */

    /**
     * 构建连续日期趋势, 缺失天补 0, 升序
     */
    private List<Map<String, Object>> buildTrend(List<Map<String, Object>> rows, LocalDate start) {
        Map<String, Long> countByDate = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object date = row.get("date");
            if (date != null) {
                countByDate.put(date.toString(), toLong(row.get("count")));
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < TREND_DAYS; i++) {
            LocalDate day = start.plusDays(i);
            String key = day.format(DAY);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", key);
            item.put("count", countByDate.getOrDefault(key, 0L));
            result.add(item);
        }
        return result;
    }

    /**
     * 将查询结果安全转为 long
     */
    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
