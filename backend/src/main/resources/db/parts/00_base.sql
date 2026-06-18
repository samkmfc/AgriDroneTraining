-- =============================================================
--  00 基础数据: 用户/档案/执照/学习记录/考试记录/检查单/作业日志/AI/访问日志
--  (内容类数据见 10_*~14_* 分文件)
-- =============================================================
USE agri_drone_training;

-- 用户 (1管理员 + 若干学员)  密码均为 123456 (BCrypt)
INSERT INTO sys_user (id, username, password, real_name, nickname, gender, phone, email, role, status, create_time) VALUES
(1,  'admin',    '$2a$10$Zen/AKWTvT7/0JHp/5p4Ouj7NHudTXit15ryTUnnB4H1unNWwoxP2', '李军',   '系统管理员', 1, '13800000000', 'admin@agridrone.com',   'ADMIN',   1, '2025-09-01 09:00:00'),
(2,  'luojian',  '$2a$10$Zen/AKWTvT7/0JHp/5p4Ouj7NHudTXit15ryTUnnB4H1unNWwoxP2', '罗健',   '罗健',     1, '13900000001', 'luojian@agridrone.com', 'STUDENT', 1, '2025-09-05 10:00:00'),
(3,  'zhangwei', '$2a$10$Zen/AKWTvT7/0JHp/5p4Ouj7NHudTXit15ryTUnnB4H1unNWwoxP2', '张伟',   '张伟',     1, '13900000002', 'zw@agridrone.com',      'STUDENT', 1, '2025-09-06 10:00:00'),
(4,  'lina',     '$2a$10$Zen/AKWTvT7/0JHp/5p4Ouj7NHudTXit15ryTUnnB4H1unNWwoxP2', '李娜',   '李娜',     2, '13900000003', 'ln@agridrone.com',      'STUDENT', 1, '2025-09-08 10:00:00'),
(5,  'wangfang', '$2a$10$Zen/AKWTvT7/0JHp/5p4Ouj7NHudTXit15ryTUnnB4H1unNWwoxP2', '王芳',   '王芳',     2, '13900000004', 'wf@agridrone.com',      'STUDENT', 1, '2025-10-01 10:00:00'),
(6,  'chenkai',  '$2a$10$Zen/AKWTvT7/0JHp/5p4Ouj7NHudTXit15ryTUnnB4H1unNWwoxP2', '陈凯',   '陈凯',     1, '13900000005', 'ck@agridrone.com',      'STUDENT', 1, '2025-10-12 10:00:00');

-- 学员档案
INSERT INTO student_profile (user_id, id_card, address, emergency_contact, emergency_phone, education, training_status, enroll_date) VALUES
(2, '440101200001011234', '广东省广州市天河区', '罗父', '13700000001', '本科', 'TRAINING',  '2025-09-05'),
(3, '440101199801011235', '广东省深圳市南山区', '张妻', '13700000002', '专科', 'TRAINING',  '2025-09-06'),
(4, '440101199901011236', '广东省佛山市禅城区', '李母', '13700000003', '本科', 'GRADUATED', '2025-09-08'),
(5, '440101200201011237', '广东省东莞市',       '王父', '13700000004', '专科', 'TRAINING',  '2025-10-01'),
(6, '440101200101011238', '广东省珠海市香洲区', '陈兄', '13700000005', '本科', 'TRAINING',  '2025-10-12');

-- 无人机执照 (覆盖 正常/临期/过期 三种状态)
INSERT INTO drone_license (user_id, license_no, license_type, drone_model, issuing_authority, issue_date, expiry_date, status) VALUES
(2, 'UAS-2024-000123', '多旋翼植保(视距内)', 'DJI T50',  '民航局AOPA', '2024-06-15', '2026-06-15', 'NORMAL'),
(3, 'UAS-2024-000456', '多旋翼植保(超视距)', 'DJI T40',  '民航局AOPA', '2024-07-01', '2026-07-10', 'EXPIRING'),
(4, 'UAS-2023-000789', '多旋翼植保(视距内)', '极飞 P100', '民航局AOPA', '2023-05-20', '2025-05-20', 'EXPIRED'),
(5, 'UAS-2025-000111', '多旋翼植保(视距内)', 'DJI T25',  '民航局AOPA', '2025-03-01', '2027-03-01', 'NORMAL'),
(6, 'UAS-2024-000222', '多旋翼植保(超视距)', '极飞 V40',  '民航局AOPA', '2024-09-10', '2026-06-30', 'EXPIRING');

-- 学习记录 (引用课程 1/2/3)
INSERT INTO learning_record (user_id, course_id, progress, finished, last_study_time) VALUES
(2, 1, 100, 1, '2025-11-10 14:00:00'),
(2, 2, 60,  0, '2025-11-15 16:00:00'),
(3, 1, 100, 1, '2025-11-12 10:00:00'),
(4, 3, 100, 1, '2025-11-01 09:00:00'),
(5, 1, 40,  0, '2025-11-16 11:00:00');

-- 考试记录 (引用试卷 1/2)
INSERT INTO exam_record (exam_id, user_id, score, passed, duration, start_time, submit_time, create_time) VALUES
(1, 2, 88, 1, 1800, '2025-11-12 14:00:00', '2025-11-12 14:30:00', '2025-11-12 14:30:00'),
(1, 3, 76, 1, 2000, '2025-11-13 14:00:00', '2025-11-13 14:33:00', '2025-11-13 14:33:00'),
(1, 4, 92, 1, 1500, '2025-11-10 14:00:00', '2025-11-10 14:25:00', '2025-11-10 14:25:00'),
(1, 5, 54, 0, 2200, '2025-11-15 14:00:00', '2025-11-15 14:36:00', '2025-11-15 14:36:00'),
(2, 2, 90, 1, 900,  '2025-11-14 10:00:00', '2025-11-14 10:15:00', '2025-11-14 10:15:00'),
(2, 6, 50, 0, 1100, '2025-11-16 10:00:00', '2025-11-16 10:18:00', '2025-11-16 10:18:00');

-- 飞前安全检查单
INSERT INTO safety_checklist (user_id, location, weather, wind_speed, battery_check, propeller_check, nozzle_check, weather_check, airspace_check, body_check, signal_check, passed, check_time) VALUES
(2, '广州市增城区水稻田',   '晴',   '2级', 1,1,1,1,1,1,1, 1, '2025-11-12 08:00:00'),
(3, '深圳市光明区蔬菜基地', '多云', '3级', 1,1,1,1,1,1,1, 1, '2025-11-13 08:30:00'),
(4, '佛山市南海区果园',     '晴',   '1级', 1,1,1,1,1,1,1, 1, '2025-11-10 07:30:00');

-- 飞行作业日志 (覆盖待审/通过/驳回)
INSERT INTO flight_log (user_id, checklist_id, drone_model, location, crop_type, area, pesticide_used, weather, flight_duration, start_time, end_time, description, audit_status, audit_reason, auditor_id, audit_time, create_time) VALUES
(2, 1, 'DJI T50', '广州市增城区水稻田',   '水稻', 120.50, '25%吡蚜酮悬浮剂', '晴',   45, '2025-11-12 08:10:00', '2025-11-12 08:55:00', '防治稻飞虱作业,航线重叠率20%,飞行平稳。', 'APPROVED', '记录真实,数据完整,通过。', 1, '2025-11-12 18:00:00', '2025-11-12 09:00:00'),
(3, 2, 'DJI T40', '深圳市光明区蔬菜基地', '蔬菜', 80.00,  '10%吡虫啉可湿性粉剂', '多云', 30, '2025-11-13 08:40:00', '2025-11-13 09:10:00', '蔬菜蚜虫防治,清晨低温时段作业。', 'APPROVED', '通过。', 1, '2025-11-13 19:00:00', '2025-11-13 09:20:00'),
(4, 3, '极飞 P100', '佛山市南海区果园',    '柑橘', 60.00,  '1.8%阿维菌素乳油', '晴',   50, '2025-11-10 07:40:00', '2025-11-10 08:30:00', '柑橘红蜘蛛防治。', 'REJECTED', '飞行时长与作业面积明显不符,疑似数据造假,请重新核实后提交。', 1, '2025-11-10 20:00:00', '2025-11-10 08:40:00'),
(5, NULL, 'DJI T25', '东莞市麻涌镇稻田',   '水稻', 50.00,  '25%吡蚜酮悬浮剂', '晴',   25, '2025-11-16 08:00:00', '2025-11-16 08:25:00', '稻飞虱防治作业。', 'PENDING', NULL, NULL, NULL, '2025-11-16 08:30:00'),
(6, NULL, '极飞 V40', '珠海市斗门区稻田',   '水稻', 90.00,  '20%三环唑可湿性粉剂', '多云', 38, '2025-11-17 08:00:00', '2025-11-17 08:38:00', '稻瘟病预防统防统治。', 'PENDING', NULL, NULL, NULL, '2025-11-17 08:45:00');

-- AI诊断记录
INSERT INTO ai_diagnosis (user_id, crop_type, question, recognition_result, recommend_pesticide, recommend_params, confidence, create_time) VALUES
(2, '水稻', '叶片有褐色斑点是什么病?', '识别为水稻纹枯病初期,叶鞘出现水渍状褐斑,需及时防治以防扩展至剑叶。', '建议使用井冈霉素A或苯甲·丙环唑', '{"speed":"4米/秒","height":"2.5米","flow":"1.2升/亩"}', 92.50, '2025-11-14 15:00:00'),
(3, '柑橘', '叶背有红色小点', '识别为柑橘红蜘蛛危害,叶背可见红色螨点及失绿斑。', '建议使用1.8%阿维菌素或螺螨酯', '{"speed":"3米/秒","height":"2米","flow":"2升/亩"}', 88.30, '2025-11-15 16:00:00');

-- 访问日志 (用于访问量看板)
INSERT INTO access_log (user_id, username, ip, module, action, create_time) VALUES
(2,'luojian','127.0.0.1','dashboard','登录',     '2025-11-11 09:00:00'),
(3,'zhangwei','127.0.0.1','course','学习',        '2025-11-12 09:00:00'),
(4,'lina','127.0.0.1','exam','考试',              '2025-11-13 09:00:00'),
(2,'luojian','127.0.0.1','flightlog','提交日志',  '2025-11-14 09:00:00'),
(5,'wangfang','127.0.0.1','ai','AI诊断',          '2025-11-15 09:00:00'),
(6,'chenkai','127.0.0.1','flightlog','提交日志',  '2025-11-16 09:00:00'),
(2,'luojian','127.0.0.1','knowledge','查询知识库','2025-11-17 09:00:00');
