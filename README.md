# 研究生支教团投票系统

本项目是面向高校研究生支教团正选成员及候补评选场景的**实时投票系统**，支持评委在线投票、自动计票、平票重投以及结果实时展示等功能。

> 本项目基于 `zkm` 和 `dcs` 两位 21 届学长的代码，主要由 `zhs` 进行了更新迭代（git 版本管理、问题排查与代码修复）。25 年正式启用前，对前后端进行了多次内容修改和 Bug 修复，确保投票页面和逻辑的正确性；此外在26年启动时对页面以及相应流程做了较大的改动。

> [!TIP]
> 系统兼容 **Chrome**、**Firefox**、**Edge** 等主流浏览器，推荐使用 Edge 稳定版本浏览器访问。
> 注意：若某一浏览器（如 Chrome）与 Edge 显示不一致，通常不是代码问题，而是该浏览器的**页面缩放/扩展（深色模式、字体类）/无痕模式**等本地设置差异，请先在浏览器中复位缩放（Ctrl+0）并在无痕窗口验证。

---

## 目录

- [系统功能](#系统功能)
- [页面与使用流程](#页面与使用流程)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [环境要求](#环境要求)
- [部署流程](#部署流程)
  - [一、数据库配置](#一数据库配置)
  - [二、后端部署](#二后端部署)
  - [三、前端构建](#三前端构建)
  - [四、Nginx 配置与启动](#四nginx-配置与启动)
- [管理员与评委操作说明](#管理员与评委操作说明)
- [常见问题排查](#常见问题排查)
- [注意事项](#注意事项)

---

## 系统功能

| 功能模块 | 说明 |
|:--|:--|
| **管理员鉴权** | `/login` 口令登录后才有权进入 `/set` 参数设置等管理操作；`/admin/**` 接口由拦截器统一保护 |
| **参数设置** | 管理员设置**正选人数、候补人数（默认 4）、评委（设备）人数、每人限投票数**；本轮全部参数可配置，无需改代码 |
| **候选人管理** | 支持 Excel 批量导入或单个录入候选人信息 |
| **在线投票** | 评委在各自设备浏览器投票；**按“设备”限一票**，同设备同轮只能投一次，防双击/刷新/多标签重复提交 |
| **自动计票** | 全部评委投完后自动统计票数，按票数和学院顺序排序 |
| **平票重投** | 出现平票时自动触发重投流程（正选/候补分别处理，可多轮直至无平票） |
| **实时监控** | `/show` 实时查看投票进度与轮次结果 |
| **结果展示** | 投票结束后分别展示**正选名单**与**候补名单** |

---

## 页面与使用流程

| 路由 | 页面名称 | 功能说明 |
|:--|:--|:--|
| `/` | 首页 | 系统入口：自动探测投票状态（未开始/进行中/已结束），给出对应入口（评委投票 / 管理员登录 / 查看结果） |
| `/vote` | 投票页 | 评委投票界面（打开即投，无需链接/口令） |
| `/login` | 管理员登录 | 输入管理口令（默认 `2026`，可用环境变量 `ADMIN_PASSWORD` 覆盖）后跳转 `/set` |
| `/set` | 参数设置页 | 管理员设置正选人数、候补人数、评委（设备）数、每人限投票数 |
| `/show` | 实时监控页 | 实时查看投票进度、各轮重投与最终结果 |
| `/waiting` | 等待页 | 评委投完后的等待界面（自动轮询，重投时自动跳回 `/vote`） |
| `/end` | 结束页 | 投票结束提示，可前往结果页 |
| `/usershow` | 结果展示页 | 投票结束后展示正选名单与候补名单 |

> [!TIP]
> `/set` 页面已加入参数描述文字与更清晰的表单样式；正选人数默认 45、候补人数默认 4，均可修改。

---

## 技术栈

### 前端
| 技术 | 版本 | 说明 |
|:--|:--|:--|
| React | 18.2.0 | 核心框架（类组件 + 函数组件） |
| Vite | 4.4.5 | 构建工具 |
| Ant Design | 5.9.0 | UI 组件库 |
| React Router | 6.15.0 | 路由管理 |
| Axios | 1.5.0 | HTTP 客户端 |

### 后端
| 技术 | 版本 | 说明 |
|:--|:--|:--|
| Java | JDK 17 | 运行环境 |
| Spring Boot | 3.2.0 | 后端框架 |
| MyBatis | — | ORM 框架 |
| MySQL | 8.0.33+ | 数据库 |
| Druid | — | 数据库连接池 |
| Nginx | 1.29.0 | 反向代理 / 静态资源服务 |

---

## 项目结构

```
Graduate_vote/
├── README.md                          # 项目说明文档
├── docs/
│   └── 投票系统鉴权与身份投票设计.md    # 鉴权/设备限票/落库设计文档
├── front-end/                         # 前端项目
│   ├── dist/                          # 构建产物（部署用）
│   ├── src/
│   │   ├── auth.js                    # 管理员 token / 设备号（deviceId）存取
│   │   ├── config.js                  # API 地址配置（部署时修改）
│   │   ├── App.jsx                    # 路由配置
│   │   ├── main.jsx                   # 入口文件（axios 全局拦截器）
│   │   ├── pages/
│   │   │   ├── home/                   # 系统首页（按状态给入口）
│   │   ├── login/                 # 管理员登录页
│   │   │   ├── vote/                  # 投票页
│   │   │   ├── set/                   # 参数设置页（含候补人数）
│   │   │   ├── show/                  # 实时监控页
│   │   │   ├── waiting/               # 等待页
│   │   │   ├── end/                   # 结束页
│   │   │   └── userShow/              # 结果展示页
│   │   └── compoments/                # 公共组件（vote / show）
│   ├── public/                        # 静态资源
│   └── package.json                   # 前端依赖配置
└── back-end/
    ├── vote.sql                       # 数据库初始化脚本（候选人表 vote01）
    └── voting_system/                 # 后端 Spring Boot 项目
        ├── pom.xml
        └── src/main/
            ├── java/com/bluemsun/
            │   ├── VotingSystemApplication.java   # 启动类
            │   ├── GlobalExceptionHandler.java    # 全局异常处理
            │   ├── auth/AdminSessionManager.java  # 管理员会话管理
            │   ├── config/WebConfig.java          # 拦截器注册
            │   ├── interceptor/AuthenticationInterceptor.java # /admin 鉴权
            │   ├── controller/ (AdminController / UserController)
            │   ├── service/ (UserService + impl/UserServiceImpl)
            │   ├── dao/ (UserDao / VoteLogDao)
            │   ├── entity/ (User / dto/ResultDto)
            │   └── utils/ (排序比较器)
            └── resources/
                ├── application.yml                # 应用配置（含管理员口令、数据库）
                ├── mapper/UserDao.xml
                └── schema_vote_step4.sql          # 场次/投票流水扩展表（可选）
```

---

## 环境要求

| 软件 | 最低版本 | 备注 |
|:--|:--|:--|
| JDK | 17 | 后端运行环境（17/21/23 均可） |
| MySQL | 8.0.33 | 数据存储 |
| Node.js | 16+ | 前端构建（如需修改前端） |
| Yarn / npm | 1.x / 任意 | 前端包管理器（如需修改前端，`npm run dev` 亦可） |
| Nginx | 1.20+ | 反向代理与静态资源服务（部署用） |

---

## 部署流程

### 一、数据库配置

1. 创建数据库：
   ```sql
   CREATE DATABASE vote DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```

2. 导入数据表结构与初始数据：
   ```bash
   mysql -u root -p vote < back-end/vote.sql
   # 鉴权/投票审计扩展表（vote_session / vote_log，可选但推荐）
   mysql -u root -p vote < back-end/voting_system/src/main/resources/schema_vote_step4.sql
   ```

3. 根据实际情况修改后端数据库连接配置（`back-end/voting_system/src/main/resources/application.yml`）：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/vote    # 数据库地址
       username: root                           # 数据库用户名
       password: your_password                  # 数据库密码
   ```

---

### 二、后端部署

#### 方式 A：使用 IntelliJ IDEA

1. 使用 IntelliJ IDEA 打开 `back-end/voting_system` 目录（或仓库根目录并加载该 Maven 工程）
2. 等待 Maven 自动下载依赖
3. 运行 `VotingSystemApplication.java` 主启动类

#### 方式 B：使用 VSCode

推荐装三个微软官方扩展：**Extension Pack for Java**、**Spring Boot Extension Pack**、**Maven for Java**。

1. 用 VSCode 打开 `back-end/voting_system` 目录：
   ```bash
   code E:\Graduate_vote\back-end\voting_system
   ```
2. 首次打开时右下角会提示是否导入 Maven 项目 / 信任 Java 工程，选择导入（Trust & Import），等待右下角进度条完成依赖下载（需要网络，仅首次较慢）。
3. 三种启动方式任选其一：
   - **① Spring Boot Dashboard**：左侧资源管理器出现 Spring 图标面板 → 找到 `voting_system` → 点 ▶ 运行；
   - **② 直接运行主类**：打开 `src/main/java/com/bluemsun/VotingSystemApplication.java`，点击 main 方法上方的 **Run**（或右键 → Run Java）；
   - **③ 终端 + Maven**：在 VSCode 菜单 Terminal → New Terminal，执行：
     ```bash
     cd E:\Graduate_vote\back-end\voting_system
     mvn spring-boot:run        # 或 mvnw spring-boot:run（使用项目自带 wrapper）
     ```
4. 看到如下日志即为启动成功：
   ```
   Started VotingSystemApplication in x.xxx seconds
   ```
   后端服务默认运行在 **8081** 端口

> [!TIP]
> - 需要 JDK 17 或更高版本：VSCode 会自动使用系统 `JAVA_HOME`（如未配置，请先安装 JDK 并在设置中把 `java.jdt.ls.java.home` 指向 JDK 目录）。
> - 若 8081 端口被占用导致启动失败（`Port 8081 was already in use`），请先在任务管理器中结束占用 8081 的旧 Java 进程，再重新启动。
> - 如需用环境变量覆盖管理员口令，可在 VSCode 中创建 `.vscode/launch.json` 配置 `env`，或在方式 B-③ 的终端里先执行 `set ADMIN_PASSWORD=你的口令` 再启动。

---

### 三、前端构建

> 如果不需要修改前端代码，可以直接使用 `front-end/dist/` 目录下的构建产物，跳过此步。

1. 安装依赖并启动开发服务器：
   ```bash
   cd front-end
   yarn install    # 或 npm install
   yarn dev        # 或 npm run dev
   ```

2. 构建生产版本：
   ```bash
   yarn build      # 或 npm run build
   ```
   构建完成后会在 `front-end/` 下生成 `dist/` 目录

> [!IMPORTANT]
> 前端 API 地址已集中管理在 `front-end/src/config.js` 中，部署时只需修改 `API_BASE_URL` 为实际后端地址：
> ```js
> export const API_BASE_URL = 'http://实际部署IP:8081';
> ```

---

### 四、Nginx 配置与启动

#### 1. 部署前端文件

将 `front-end/dist` 复制并重命名为 `vote`，放入 Nginx 安装根目录。

#### 2. 修改 Nginx 配置

编辑 `nginx/conf/nginx.conf`，核心配置如下：

```nginx
http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;
    keepalive_timeout  65;

    server {
        listen       80;
        server_name  localhost;

        # 前端静态资源
        location / {
            root   vote;
            index  index.html index.htm;
            try_files $uri $uri/ /index.html;  # SPA 路由支持
        }

        # 后端 API 反向代理（/users、/vote 等由前端 config.js 直连 8081，
        # 若希望全部经 80 访问，可在此统一代理，并相应修改 config.js）
        location /admin {
            proxy_pass http://localhost:8081;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        error_page 500 502 503 504 /50x.html;
        location = /50x.html {
            root html;
        }
    }
}
```

#### 3. 启动 Nginx

双击 `nginx.exe` 启动（终端自动退出是正常现象），在任务管理器中确认 `nginx.exe` 进程存在即为成功。

#### 4. 访问系统

在浏览器（Chrome / Firefox / Edge）中访问 `http://<部署主机IP>` 即可。

---

## 管理员与评委操作说明

### 1. 管理员：登录与设置参数

1. 打开 `http://<主机IP>/login`，输入管理口令（默认 **`2026`**；如需修改，可在启动后端前设置环境变量 **`ADMIN_PASSWORD`** 覆盖，或直接改 `application.yml` 中 `app.admin.password` 的默认值）。
2. 进入 `/set` 参数设置页，填写：
   - **正选人数**（默认 45）；
   - **候补人数**（默认 4，可修改）；
   - **参与投票设备/评委数**（现场有几台设备在投票就填几）；
   - **每人限投票数**（须与正选人数一致，默认 45）。
3. 点击“确认提交”——系统将初始化本轮投票（清空票数），随后评委即可在各自设备上投票。

### 2. 评委：投票（按设备限一票，无需任何链接/口令）

1. 评委在各自设备浏览器打开 `http://<主机IP>/`（系统首页）。首页会自动探测投票状态：
   - **未开始**：提示等待管理员设置（评委无需操作）；
   - **进行中**：点“进入投票”跳转投票页 `/vote`。
2. 投票页直接显示候选人名单，勾选“是否同意”后点“提交”。
3. 投完后进入 `/waiting` 等待页，系统自动轮询；若本轮无平票，等待所有人投完后出结果；
   若出现平票需重投，等待页会自动跳回 `/vote`，评委再次投票即可（每轮每位评委仅一票）。

> [!NOTE]
> **按设备限票的边界**：同一台设备每轮只能投一次（刷新/双击/多标签都会提示“本设备已投过”）。
> 但若人为清除浏览器缓存、使用无痕窗口或换一台设备，系统无法识别为“同一人”再投一次。
> 请现场安排“每人一台设备”并留意监督；如需更强的“一人一票”身份绑定，可启用编号+口令方案（见设计文档 §5）。

### 3. 管理员：监控与结果

- `/show`：实时查看投票人数、各轮次结果；全部确定后分别展示**正选名单**与**候补名单**。
- 结束页 `/end` 与结果页 `/usershow` 向评委/观众展示最终正选与候补名单。

---

## 常见问题排查

1. **后端已启动但页面数据为空**：
   - 访问 `http://localhost:8081/vote/status`，若返回 JSON 则后端正常；
   - 若提示“请管理员先设置投票限制和老师数量”，说明尚未在 `/login` 登录并在 `/set` 完成参数设置；
   - F12 打开控制台查看报错；
   - 检查 `application.yml` 数据库连接与 `vote01` 表是否存在。

2. **登录 `/login` 提示“口令错误”**：默认口令为 `2026`。修改方式二选一：① 启动后端前设置环境变量 `ADMIN_PASSWORD=<新口令>`；② 改 `application.yml` 中 `app.admin.password` 的默认值。改后需重启后端。

3. **进入 `/set` 被跳回 `/login`**：属正常鉴权，请先登录。

4. **提示“本设备已投过”**：该设备本轮已完成投票；若确需重投请管理员重新初始化本轮（注意会清空全部票数）。

5. **浏览器之间显示不一致**：先按本页顶部提示复位缩放并在无痕窗口验证；确认后端当前运行的版本已包含最新代码。

---

## 注意事项

1. **浏览器要求**：推荐使用 Edge 稳定版本访问；使用 Chrome 前请先确认其页面缩放为 100%、无深色/字体类扩展干扰。

2. **参数一致性**：`每人限投票数` 必须与 `正选人数` 一致（后端会校验），否则设置会被拒绝。

3. **初始化会清票**：管理员每次提交 `/set` 都会清空本轮全部票数并开启新场次，请确认参数无误后再提交。

4. **重投流程**：出现平票进入重投后，请让所有评委从等待页自动跳回投票页完成本轮重投，避免部分评委漏投导致结果未收敛。

5. **IP 地址配置**：部署时修改 `front-end/src/config.js` 的 `API_BASE_URL` 后重新构建即可。

6. **数据库密码**：生产部署时请修改 `application.yml` 中的数据库连接密码。

7. **内网穿透**（可选）：可使用 natapp 等内网穿透工具进行外网访问，避免频繁更换后端 URL 配置。
