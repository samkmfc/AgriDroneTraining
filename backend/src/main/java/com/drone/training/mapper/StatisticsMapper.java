package com.drone.training.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 统计 Mapper (仪表盘 / ECharts 数据源)
 * 直接使用 @Select 编写聚合 SQL, 列名与 schema.sql 保持一致, 别名为驼峰以便 Map 直接映射
 *
 * @author 罗健 202308852
 */
@Mapper
public interface StatisticsMapper {

    /* ============================ 管理端总览 ============================ */

    @Select("SELECT COUNT(*) FROM sys_user WHERE role = 'STUDENT' AND deleted = 0")
    long countStudents();

    @Select("SELECT COUNT(*) FROM course")
    long countCourses();

    @Select("SELECT COUNT(*) FROM flight_log")
    long countFlightLogs();

    @Select("SELECT COUNT(*) FROM flight_log WHERE audit_status = 'PENDING'")
    long countPendingAudit();

    @Select("SELECT COUNT(*) FROM drone_license WHERE status IN ('EXPIRING', 'EXPIRED')")
    long countExpiringLicense();

    @Select("SELECT COUNT(*) FROM exam_record")
    long countExamRecords();

    @Select("SELECT COUNT(*) FROM exam_record WHERE passed = 1")
    long countExamPassed();

    @Select("SELECT COUNT(*) FROM access_log")
    long countAccessLogs();

    @Select("SELECT COUNT(*) FROM regulation")
    long countRegulations();

    @Select("SELECT COUNT(*) FROM ai_diagnosis")
    long countAiDiagnosis();

    /* ============================ 图表数据 ============================ */

    /**
     * 各试卷通过率统计 (柱状图)
     */
    @Select("SELECT e.title AS examTitle, " +
            "COUNT(r.id) AS total, " +
            "SUM(CASE WHEN r.passed = 1 THEN 1 ELSE 0 END) AS passed " +
            "FROM exam_record r " +
            "LEFT JOIN exam e ON e.id = r.exam_id " +
            "GROUP BY r.exam_id, e.title " +
            "ORDER BY total DESC")
    List<Map<String, Object>> examPassStats();

    /**
     * 最近若干天每日飞行日志提交数 (按 DATE(create_time) 分组)
     */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date, COUNT(*) AS count " +
            "FROM flight_log " +
            "WHERE create_time >= #{startDate} " +
            "GROUP BY DATE(create_time)")
    List<Map<String, Object>> flightLogDailyCount(@Param("startDate") String startDate);

    /**
     * 最近若干天每日访问量 (按 DATE(create_time) 分组)
     */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date, COUNT(*) AS count " +
            "FROM access_log " +
            "WHERE create_time >= #{startDate} " +
            "GROUP BY DATE(create_time)")
    List<Map<String, Object>> accessLogDailyCount(@Param("startDate") String startDate);

    /**
     * 执照状态分布 (饼图)
     */
    @Select("SELECT status AS status, COUNT(*) AS count " +
            "FROM drone_license " +
            "GROUP BY status")
    List<Map<String, Object>> licenseStatusStats();

    /**
     * 各机型培训人数 (按 drone_model 分组, 统计去重学员数, 取前10)
     */
    @Select("SELECT drone_model AS model, COUNT(DISTINCT user_id) AS count " +
            "FROM flight_log " +
            "WHERE drone_model IS NOT NULL AND drone_model <> '' " +
            "GROUP BY drone_model " +
            "ORDER BY count DESC " +
            "LIMIT 10")
    List<Map<String, Object>> modelDistribution();

    /* ============================ 学员仪表盘 ============================ */

    @Select("SELECT COUNT(*) FROM drone_license WHERE user_id = #{userId}")
    long countMyLicense(@Param("userId") Long userId);

    /**
     * 当前学员所有执照状态列表 (用于计算最关切状态)
     */
    @Select("SELECT status FROM drone_license WHERE user_id = #{userId}")
    List<String> myLicenseStatuses(@Param("userId") Long userId);

    /**
     * 当前学员执照距到期最小剩余天数 (可能为 null)
     */
    @Select("SELECT MIN(DATEDIFF(expiry_date, CURDATE())) FROM drone_license WHERE user_id = #{userId}")
    Integer myNearestExpiryDays(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM course WHERE status = 1")
    long countActiveCourses();

    @Select("SELECT COUNT(*) FROM learning_record WHERE user_id = #{userId} AND finished = 1")
    long countMyFinishedCourses(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM exam_record WHERE user_id = #{userId}")
    long countMyExams(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM exam_record WHERE user_id = #{userId} AND passed = 1")
    long countMyPassedExams(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM flight_log WHERE user_id = #{userId}")
    long countMyFlightLogs(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM flight_log WHERE user_id = #{userId} AND audit_status = 'PENDING'")
    long countMyPendingLogs(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM flight_log WHERE user_id = #{userId} AND audit_status = 'APPROVED'")
    long countMyApprovedLogs(@Param("userId") Long userId);
}
