-- =============================================================
--  农业无人机培训管理系统  数据库结构 (MySQL 8.0)
--  Agricultural Drone Training Management System - Schema
--  作者：罗健  学号：202308852
-- =============================================================

DROP DATABASE IF EXISTS agri_drone_training;
CREATE DATABASE agri_drone_training DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE agri_drone_training;

-- -------------------------------------------------------------
-- 1. 系统用户表 (学员/管理员)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username      VARCHAR(50)  NOT NULL COMMENT '登录账号',
    password      VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name     VARCHAR(50)           DEFAULT NULL COMMENT '真实姓名',
    nickname      VARCHAR(50)           DEFAULT NULL COMMENT '昵称',
    gender        TINYINT               DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    phone         VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    email         VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    avatar        VARCHAR(255)          DEFAULT NULL COMMENT '头像URL',
    role          VARCHAR(20)  NOT NULL DEFAULT 'STUDENT' COMMENT '角色 STUDENT/ADMIN',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
    last_login_time DATETIME            DEFAULT NULL COMMENT '最后登录时间',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- -------------------------------------------------------------
-- 2. 学员资质档案表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS student_profile;
CREATE TABLE student_profile (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT       NOT NULL COMMENT '关联用户ID',
    id_card         VARCHAR(20)           DEFAULT NULL COMMENT '身份证号',
    address         VARCHAR(255)          DEFAULT NULL COMMENT '联系地址',
    emergency_contact VARCHAR(50)         DEFAULT NULL COMMENT '紧急联系人',
    emergency_phone VARCHAR(20)           DEFAULT NULL COMMENT '紧急联系电话',
    education       VARCHAR(50)           DEFAULT NULL COMMENT '学历',
    training_status VARCHAR(20)  NOT NULL DEFAULT 'TRAINING' COMMENT '培训状态 TRAINING培训中/GRADUATED已结业/SUSPENDED暂停',
    enroll_date     DATE                  DEFAULT NULL COMMENT '入学日期',
    remark          VARCHAR(500)          DEFAULT NULL COMMENT '备注',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学员资质档案表';

-- -------------------------------------------------------------
-- 3. 无人机执照表 (含有效期动态监控/临期预警)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS drone_license;
CREATE TABLE drone_license (
    id                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id           BIGINT      NOT NULL COMMENT '所属学员ID',
    license_no        VARCHAR(50) NOT NULL COMMENT '执照编号',
    license_type      VARCHAR(50) NOT NULL COMMENT '执照类型/适用机型 如:多旋翼植保/视距内驾驶员',
    drone_model       VARCHAR(50)          DEFAULT NULL COMMENT '准飞机型',
    issuing_authority VARCHAR(100)         DEFAULT NULL COMMENT '发证机构',
    issue_date        DATE        NOT NULL COMMENT '发证日期',
    expiry_date       DATE        NOT NULL COMMENT '有效期截止日期',
    status            VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态 NORMAL正常/EXPIRING临期/EXPIRED已过期',
    create_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_license_no (license_no),
    KEY idx_user_id (user_id),
    KEY idx_expiry_date (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='无人机执照表';

-- -------------------------------------------------------------
-- 4. 课程表 (在线学习)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS course;
CREATE TABLE course (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title        VARCHAR(100) NOT NULL COMMENT '课程标题',
    category     VARCHAR(50)           DEFAULT NULL COMMENT '课程分类 如:法规/飞行操作/植保技术/安全',
    cover        VARCHAR(255)          DEFAULT NULL COMMENT '封面图',
    intro        VARCHAR(500)          DEFAULT NULL COMMENT '课程简介',
    content      LONGTEXT              DEFAULT NULL COMMENT '课程图文内容',
    video_url    VARCHAR(255)          DEFAULT NULL COMMENT '视频地址',
    instructor   VARCHAR(50)           DEFAULT NULL COMMENT '讲师',
    duration     INT                   DEFAULT 0 COMMENT '时长(分钟)',
    view_count   INT          NOT NULL DEFAULT 0 COMMENT '学习人次',
    sort         INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0下架 1上架',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- -------------------------------------------------------------
-- 5. 学习记录表 (学员-课程)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS learning_record;
CREATE TABLE learning_record (
    id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id      BIGINT   NOT NULL COMMENT '学员ID',
    course_id    BIGINT   NOT NULL COMMENT '课程ID',
    progress     INT      NOT NULL DEFAULT 0 COMMENT '学习进度(%)',
    finished     TINYINT  NOT NULL DEFAULT 0 COMMENT '是否完成 0否 1是',
    last_study_time DATETIME       DEFAULT NULL COMMENT '最近学习时间',
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_course (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';

-- -------------------------------------------------------------
-- 6. 空域法规/政策资讯表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS regulation;
CREATE TABLE regulation (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title        VARCHAR(150) NOT NULL COMMENT '标题',
    type         VARCHAR(20)  NOT NULL DEFAULT 'REGULATION' COMMENT '类型 REGULATION法规/POLICY政策/NEWS资讯',
    cover        VARCHAR(255)          DEFAULT NULL COMMENT '封面图',
    summary      VARCHAR(500)          DEFAULT NULL COMMENT '摘要',
    content      LONGTEXT              DEFAULT NULL COMMENT '正文内容',
    author       VARCHAR(50)           DEFAULT NULL COMMENT '来源/作者',
    view_count   INT          NOT NULL DEFAULT 0 COMMENT '阅读量',
    publish_time DATETIME              DEFAULT NULL COMMENT '发布时间',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0草稿 1发布',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='空域法规/政策资讯表';

-- -------------------------------------------------------------
-- 7. 禁飞区表 (地图展示)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS no_fly_zone;
CREATE TABLE no_fly_zone (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(100) NOT NULL COMMENT '禁飞区名称',
    province    VARCHAR(50)           DEFAULT NULL COMMENT '省份',
    city        VARCHAR(50)           DEFAULT NULL COMMENT '城市',
    longitude   DECIMAL(10,6) NOT NULL COMMENT '中心经度',
    latitude    DECIMAL(10,6) NOT NULL COMMENT '中心纬度',
    radius      INT          NOT NULL DEFAULT 1000 COMMENT '半径(米)',
    level       VARCHAR(20)  NOT NULL DEFAULT 'STRICT' COMMENT '等级 STRICT禁飞/LIMIT限飞',
    description VARCHAR(500)          DEFAULT NULL COMMENT '说明',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='禁飞区表';

-- -------------------------------------------------------------
-- 8. 作物识别图谱表 (植保知识库)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS crop_atlas;
CREATE TABLE crop_atlas (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    crop_name     VARCHAR(50)  NOT NULL COMMENT '作物名称',
    image         VARCHAR(255)          DEFAULT NULL COMMENT '作物图片',
    category      VARCHAR(50)           DEFAULT NULL COMMENT '类别 如:粮食/经济作物/果树',
    growth_cycle  VARCHAR(100)          DEFAULT NULL COMMENT '生长周期',
    common_pests  VARCHAR(500)          DEFAULT NULL COMMENT '常见病虫害',
    description   TEXT                  DEFAULT NULL COMMENT '描述/植保要点',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作物识别图谱表';

-- -------------------------------------------------------------
-- 9. 农药配比表 (植保知识库)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS pesticide_ratio;
CREATE TABLE pesticide_ratio (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    pesticide_name  VARCHAR(80)  NOT NULL COMMENT '农药名称',
    crop_name       VARCHAR(50)           DEFAULT NULL COMMENT '适用作物',
    pest_name       VARCHAR(80)           DEFAULT NULL COMMENT '防治对象(病虫害)',
    dosage          VARCHAR(80)           DEFAULT NULL COMMENT '亩用药量',
    water_ratio     VARCHAR(80)           DEFAULT NULL COMMENT '兑水比例/亩喷液量',
    spray_method    VARCHAR(120)          DEFAULT NULL COMMENT '施药方式',
    safety_interval VARCHAR(50)           DEFAULT NULL COMMENT '安全间隔期',
    notes           VARCHAR(500)          DEFAULT NULL COMMENT '注意事项',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农药配比表';

-- -------------------------------------------------------------
-- 10. 题库表 (多维度试题)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS question;
CREATE TABLE question (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    type        VARCHAR(20)  NOT NULL DEFAULT 'SINGLE' COMMENT '题型 SINGLE单选/MULTI多选/JUDGE判断',
    category    VARCHAR(50)           DEFAULT NULL COMMENT '维度分类 如:法规/安全/植保/飞行操作',
    content     VARCHAR(1000) NOT NULL COMMENT '题干',
    options     JSON                  DEFAULT NULL COMMENT '选项 [{"key":"A","value":"..."}]',
    answer      VARCHAR(50)  NOT NULL COMMENT '正确答案 如:A 或 A,B 或 T/F',
    analysis    VARCHAR(1000)         DEFAULT NULL COMMENT '解析',
    difficulty  TINYINT      NOT NULL DEFAULT 1 COMMENT '难度 1易 2中 3难',
    score       INT          NOT NULL DEFAULT 5 COMMENT '分值',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';

-- -------------------------------------------------------------
-- 11. 试卷/场景化测试表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS exam;
CREATE TABLE exam (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title         VARCHAR(100) NOT NULL COMMENT '试卷标题',
    category      VARCHAR(50)           DEFAULT NULL COMMENT '考试维度/场景',
    description   VARCHAR(500)          DEFAULT NULL COMMENT '说明',
    total_score   INT          NOT NULL DEFAULT 100 COMMENT '总分',
    pass_score    INT          NOT NULL DEFAULT 60 COMMENT '及格分',
    duration      INT          NOT NULL DEFAULT 60 COMMENT '考试时长(分钟)',
    question_ids  JSON                  DEFAULT NULL COMMENT '题目ID集合',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- -------------------------------------------------------------
-- 12. 考试记录表 (通过率统计来源)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS exam_record;
CREATE TABLE exam_record (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    exam_id     BIGINT   NOT NULL COMMENT '试卷ID',
    user_id     BIGINT   NOT NULL COMMENT '学员ID',
    score       INT      NOT NULL DEFAULT 0 COMMENT '得分',
    passed      TINYINT  NOT NULL DEFAULT 0 COMMENT '是否通过 0否 1是',
    answers     JSON              DEFAULT NULL COMMENT '作答详情',
    duration    INT               DEFAULT 0 COMMENT '用时(秒)',
    start_time  DATETIME          DEFAULT NULL COMMENT '开始时间',
    submit_time DATETIME          DEFAULT NULL COMMENT '提交时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_exam_id (exam_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- -------------------------------------------------------------
-- 13. 飞前安全检查单表 (强制填报)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS safety_checklist;
CREATE TABLE safety_checklist (
    id              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT   NOT NULL COMMENT '学员ID',
    location        VARCHAR(150)      DEFAULT NULL COMMENT '作业地点',
    weather         VARCHAR(50)       DEFAULT NULL COMMENT '天气情况',
    wind_speed      VARCHAR(30)       DEFAULT NULL COMMENT '风速',
    battery_check   TINYINT  NOT NULL DEFAULT 0 COMMENT '电池检查 0未通过 1通过',
    propeller_check TINYINT  NOT NULL DEFAULT 0 COMMENT '螺旋桨/喷嘴检查',
    nozzle_check    TINYINT  NOT NULL DEFAULT 0 COMMENT '喷嘴检查',
    weather_check   TINYINT  NOT NULL DEFAULT 0 COMMENT '天气适航检查',
    airspace_check  TINYINT  NOT NULL DEFAULT 0 COMMENT '空域合规检查(是否禁飞区)',
    body_check      TINYINT  NOT NULL DEFAULT 0 COMMENT '机身/桨叶完整检查',
    signal_check    TINYINT  NOT NULL DEFAULT 0 COMMENT '信号/GPS检查',
    check_items     JSON              DEFAULT NULL COMMENT '检查项明细(扩展)',
    passed          TINYINT  NOT NULL DEFAULT 0 COMMENT '整体是否通过(全部勾选)',
    remark          VARCHAR(500)      DEFAULT NULL COMMENT '备注',
    check_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检查时间',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='飞前安全检查单表';

-- -------------------------------------------------------------
-- 14. 飞行作业日志表 (人工审核防伪)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS flight_log;
CREATE TABLE flight_log (
    id             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        BIGINT   NOT NULL COMMENT '学员ID',
    checklist_id   BIGINT            DEFAULT NULL COMMENT '关联飞前安全检查单ID(强制前置)',
    drone_model    VARCHAR(50)       DEFAULT NULL COMMENT '作业机型',
    location       VARCHAR(150)      DEFAULT NULL COMMENT '作业地点',
    longitude      DECIMAL(10,6)     DEFAULT NULL COMMENT '经度',
    latitude       DECIMAL(10,6)     DEFAULT NULL COMMENT '纬度',
    crop_type      VARCHAR(50)       DEFAULT NULL COMMENT '作业作物',
    area           DECIMAL(10,2)     DEFAULT NULL COMMENT '作业面积(亩)',
    pesticide_used VARCHAR(150)      DEFAULT NULL COMMENT '使用药剂',
    weather        VARCHAR(50)       DEFAULT NULL COMMENT '作业天气',
    flight_duration INT              DEFAULT 0 COMMENT '飞行时长(分钟)',
    start_time     DATETIME          DEFAULT NULL COMMENT '作业开始时间',
    end_time       DATETIME          DEFAULT NULL COMMENT '作业结束时间',
    images         JSON              DEFAULT NULL COMMENT '现场照片',
    description    VARCHAR(1000)     DEFAULT NULL COMMENT '作业描述',
    audit_status   VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态 PENDING待审/APPROVED通过/REJECTED驳回',
    audit_reason   VARCHAR(500)      DEFAULT NULL COMMENT '审核意见(驳回原因)',
    auditor_id     BIGINT            DEFAULT NULL COMMENT '审核人ID',
    audit_time     DATETIME          DEFAULT NULL COMMENT '审核时间',
    create_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_audit_status (audit_status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='飞行作业日志表';

-- -------------------------------------------------------------
-- 15. 智慧农情AI诊断记录表 (VisualGLM)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS ai_diagnosis;
CREATE TABLE ai_diagnosis (
    id               BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id          BIGINT   NOT NULL COMMENT '学员ID',
    image_url        VARCHAR(255)      DEFAULT NULL COMMENT '上传的田间图片',
    crop_type        VARCHAR(50)       DEFAULT NULL COMMENT '作物类型',
    question         VARCHAR(500)      DEFAULT NULL COMMENT '用户提问',
    recognition_result TEXT            DEFAULT NULL COMMENT '病虫害识别结果',
    recommend_pesticide VARCHAR(255)   DEFAULT NULL COMMENT '推荐农药及配比',
    recommend_params JSON              DEFAULT NULL COMMENT '推荐飞行参数 {speed,height,flow}',
    confidence       DECIMAL(5,2)      DEFAULT NULL COMMENT '识别置信度(%)',
    create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智慧农情AI诊断记录表';

-- -------------------------------------------------------------
-- 16. 访问日志表 (平台访问量统计)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS access_log;
CREATE TABLE access_log (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT            DEFAULT NULL COMMENT '用户ID',
    username    VARCHAR(50)       DEFAULT NULL COMMENT '用户名',
    ip          VARCHAR(50)       DEFAULT NULL COMMENT 'IP地址',
    module      VARCHAR(50)       DEFAULT NULL COMMENT '访问模块',
    action      VARCHAR(100)      DEFAULT NULL COMMENT '操作',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    PRIMARY KEY (id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问日志表';

-- -------------------------------------------------------------
-- 17. 消息通知表
-- -------------------------------------------------------------
DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '接收用户ID',
    type        VARCHAR(30)  NOT NULL COMMENT '类型 LICENSE_EXPIRING/LICENSE_EXPIRED/FLIGHT_APPROVED/FLIGHT_REJECTED/SYSTEM',
    title       VARCHAR(150) NOT NULL COMMENT '标题',
    content     VARCHAR(800)          DEFAULT NULL COMMENT '内容',
    related_id  BIGINT                DEFAULT NULL COMMENT '关联业务ID',
    is_read     TINYINT      NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user (user_id, is_read),
    KEY idx_create (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';
