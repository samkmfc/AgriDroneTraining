# 基于 SpringBoot 的农业无人机培训管理系统


将信息化技术与农业植保、法规安全、实操监管深度融合，解决农业无人机培训行业
"会飞不会洒""黑飞风险高""飞行记录易伪造""资质过期违规作业"等管理痛点。系统采用
**前后端分离**架构，分为**学员端（C 端）**与**管理端（B 端）**两大体系。

---

## 一、技术栈（严格按照开题报告）

| 类别 | 技术选型 |
| --- | --- |
| 后端 | JDK 1.8、SpringBoot 2.7.10、MyBatis-Plus 3.5.3.1、Spring Security 5.7.8 + JWT、Redis 6.2.12、MySQL 8.0.36 |
| 前端 | Vue.js 3.2.45、Element Plus 2.3.8、Axios 1.4.0、ECharts 5.4.3、Vue Router、Vuex |
| 架构 | 四层：表现层(Vue) → 业务逻辑层(SpringBoot) → 数据访问层(MyBatis-Plus) → 数据层(MySQL+Redis) |
| 部署 | Tomcat 9.0（WAR）、Nginx 反向代理 |

## 二、功能模块
**学员端（C 端）**
1. 用户与资质档案管理 —— 注册登录、个人档案、执照查看
2. 空域法规与知识库 —— 法规/政策/资讯、禁飞区地图、作物图谱、农药配比库
3. 课程与场景化测试 —— 在线课程学习、多维度试题、在线考试与记录
4. 作业日志与安全自查 —— **强制飞前安全检查清单**、飞行作业日志填报
5. 智慧农情 AI 助手 —— 调用 VisualGLM 第三方大模型，病虫害识别 + 飞行参数推荐
6. 个人工作台 —— 学习进度、执照预警、作业概览

**管理端（B 端）**
1. 数据看板 —— ECharts 可视化（作业趋势、访问量、通过率、执照分布、机型分布）
2. 学员与用户管理 —— 用户 CRUD、状态管理、档案查看
3. **执照管理与临期预警** —— 有效期动态监控、临期/过期预警
4. 课程 / 法规资讯 / 禁飞区 / 作物图谱 / 农药配比库 管理
5. 题库 / 试卷 管理、考核记录统计
6. **作业日志人工审核** —— 防伪驳回工作流

### 企业级交互能力（已落地）
- **消息通知中心**：顶栏铃铛 + 未读角标；执照临期/过期自动预警通知；作业日志审核结果自动推送学员；一键全部已读。
- **飞行日志批量审核**：多选 + 批量通过/驳回，审核即通知学员。
- **执照一键续期**：管理端按 12/24/36 个月续期并通知学员，状态自动重算。
- **考试答案解析回顾**：学员可查看每题的正确答案、自己的作答、对错与解析。
- **数据导出**：用户/执照/考核记录/作业日志一键导出 CSV(Excel 可读)。
- **农药配比计算器**：按作业面积估算总用药量与总喷液量。
- **禁飞区热力地图**：高德地图热力图 + 净空圆形范围 + 地图选点。
- **在线课程视频**：真实视频播放，观看进度自动回写学习记录。

## 三、目录结构

```
AgriDroneTraining/
├── backend/                      # SpringBoot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/drone/training/
│       │   ├── common/           # 统一返回 Result、全局异常
│       │   ├── config/           # Security/MyBatisPlus/Redis/WebMvc
│       │   ├── security/         # JWT 过滤器、登录用户、工具类
│       │   ├── entity/           # 16 张表实体
│       │   ├── mapper/           # MyBatis-Plus Mapper
│       │   ├── service/(impl)    # 业务逻辑
│       │   ├── controller/       # REST 接口
│       │   ├── dto/              # 请求 DTO
│       │   └── task/             # 执照临期预警定时任务
│       └── resources/
│           ├── application.yml
│           └── db/
│               ├── schema.sql    # 建库建表脚本
│               └── data.sql      # 初始化演示数据
└── frontend/                     # Vue3 前端
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── api/                  # 接口封装
        ├── components/           # ChartBox、ImageUpload
        ├── layout/               # 后台框架布局
        ├── router/  store/  utils/  styles/
        └── views/
            ├── student/          # 学员端 16 个页面
            └── admin/            # 管理端 12 个页面
```

## 四、快速启动

### 1. 准备环境
需安装：**JDK 1.8、Maven、MySQL 8.0、Redis、Node.js 16+**

### 2. 初始化数据库
```bash
mysql -uroot -p < backend/src/main/resources/db/schema.sql
mysql -uroot -p < backend/src/main/resources/db/data.sql
```
> 默认连接 `localhost:3306`，账号 `root/root`。如不同请修改
> `backend/src/main/resources/application.yml` 中的 datasource 与 redis 配置。

### 3. 启动后端
```bash
cd backend
mvn spring-boot:run
# 接口地址：http://localhost:8080/api
```

### 4. 启动前端
```bash
cd frontend
npm install
npm run dev
# 访问：http://localhost:5173
```

### 5. 演示账号（密码均为 123456）
| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `123456` |
| 学员 | `luojian` | `123456` |

## 五、说明
- **智慧农情 AI 助手**：默认 `drone.ai.enabled=false`，使用内置模拟应答（基于作物类型给出
  病虫害识别、农药配比与飞行参数推荐）。配置真实 VisualGLM 接口地址与密钥后即可对接真实大模型。
- **文件上传**：默认存储于后端 `./uploads/` 目录（开题报告中的 FastDFS 可在生产替换）。
- **安全**：密码 BCrypt 加密，JWT 无状态鉴权，基于角色（STUDENT/ADMIN）的权限控制，
  `/admin/**` 接口仅管理员可访问。

## 六、生产部署（开题报告 3.2）
- 后端 `mvn package` 打成 `agri-drone-training.war`，部署到 Tomcat 9.0。
- 前端 `npm run build` 生成 `dist/`，由 Nginx 托管并反向代理 `/api` 到后端。
