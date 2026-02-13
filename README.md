# 研究生支教团投票系统

本项目是面向高校研究生支教团正选成员及候补评选场景的**实时投票系统**，支持评委在线投票、自动计票、平票重投以及结果实时展示等功能。

> 本项目基于 `zkm` 和 `dcs` 两位 21 届学长的代码，主要由 `zhs` 进行了更新迭代，git版本管理，完成了相关问题排查与代码修复。25 年正式启用前，与`jqy`和`lcp` 学长对前后端进行了多次内容修改和 Bug 修复，确保投票页面和逻辑的正确性。

> [!TIP]
> 系统兼容 **Chrome**、**Firefox**、**Edge** 等主流浏览器，推荐使用`Edge`稳定版本浏览器访问。

---

## 目录

- [系统功能](#系统功能)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [环境要求](#环境要求)
- [部署流程](#部署流程)
  - [一、数据库配置](#一数据库配置)
  - [二、后端部署](#二后端部署)
  - [三、前端构建](#三前端构建)
  - [四、Nginx 配置与启动](#四nginx-配置与启动)
- [页面功能说明](#页面功能说明)
- [注意事项](#注意事项)

---

## 系统功能

| 功能模块 | 说明 |
|:--|:--|
| **参数设置** | 管理员设置正选人数、评委人数、每人限投票数 |
| **候选人管理** | 支持 Excel 批量导入或单个录入候选人信息 |
| **在线投票** | 评委通过浏览器进行投票，支持限票校验 |
| **自动计票** | 投票结束后自动统计票数，按票数和学院排序 |
| **平票重投** | 出现平票时自动触发重投流程（正选/候补分别处理） |
| **实时监控** | 管理员可实时查看投票进度与结果 |
| **结果展示** | 投票结束后展示正选名单及候补名单 |

---

## 页面功能说明

| 路由 | 页面名称 | 功能说明 |
|:--|:--|:--|
| `/set` | 参数设置页 | 设置正选人数、评委人数、每人限投票数（默认 2 名候补，修改候补人数需改后端代码） |
| `/vote` | 投票页 | 评委投票界面（默认首页） |
| `/show` | 实时监控页 | 管理员实时查看投票进度和结果 |
| `/waiting` | 等待页 | 评委投票结束后的等待界面 |
| `/userShow` | 结果展示页 | 投票结束后向用户展示最终结果 |
| `/end` | 结束页 | 投票流程结束页面 |

> [!TIP]
> 更新后的set页面，加上了三个参数的描述文字，并优化了盒子和css样式。

![](https://cdn.jsdelivr.net/gh/hash-distill/PicGo-Repo@master/202602131527332.png)


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
├── CODE_ISSUES.md                     # 代码问题分析报告
├── front-end/                         # 前端项目
│   ├── dist/                          # 构建产物（部署用）
│   ├── src/                           # 前端源代码
│   │   ├── config.js                  # API 地址配置（部署时修改此文件）
│   │   ├── App.jsx                    # 路由配置
│   │   ├── main.jsx                   # 入口文件
│   │   ├── pages/                     # 页面组件
│   │   │   ├── vote/                  # 投票页
│   │   │   ├── set/                   # 参数设置页
│   │   │   ├── show/                  # 实时监控页
│   │   │   ├── waiting/               # 等待页
│   │   │   ├── end/                   # 结束页
│   │   │   └── userShow/              # 结果展示页
│   │   └── compoments/                # 公共组件
│   │       ├── vote/                  # 投票相关组件
│   │       └── show/                  # 展示相关组件
│   ├── public/                        # 静态资源
│   ├── package.json                   # 项目依赖配置
│   ├── vite.config.js                 # Vite 配置
│   └── index.html                     # 入口 HTML
└── back-end/
    ├── vote.sql                       # 数据库初始化脚本
    └── voting_system/                 # 后端 Spring Boot 项目
        ├── pom.xml                    # Maven 依赖配置
        └── src/main/
            ├── java/com/bluemsun/
            │   ├── VotingSystemApplication.java   # 启动类
            │   ├── GlobalExceptionHandler.java    # 全局异常处理
            │   ├── controller/
            │   │   ├── AdminController.java       # 管理员接口
            │   │   └── UserController.java        # 用户投票接口
            │   ├── service/
            │   │   ├── UserService.java           # 服务层接口
            │   │   └── impl/UserServiceImpl.java  # 核心投票逻辑
            │   ├── dao/UserDao.java               # 数据访问层
            │   ├── entity/
            │   │   ├── User.java                  # 用户实体
            │   │   └── dto/ResultDto.java         # 响应 DTO
            │   ├── interceptor/                   # 拦截器
            │   └── utils/                         # 排序工具类
            └── resources/
                ├── application.yml                # 应用配置
                └── mapper/UserDao.xml             # MyBatis 映射
```

---

## 环境要求

| 软件 | 最低版本 | 备注 |
|:--|:--|:--|
| JDK | 17 | 后端运行环境 |
| MySQL | 8.0.33 | 数据存储 |
| Node.js | 16+ | 前端构建（如需修改前端） |
| Yarn | 1.x | 前端包管理器（如需修改前端） |
| Nginx | 1.20+ | 反向代理与静态资源服务 |

---

## 部署流程

### 一、数据库配置

1. 创建数据库：
   ```sql
   CREATE DATABASE vote DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```

2. 导入数据表结构和初始数据：
   ```bash
   mysql -u root -p vote < back-end/vote.sql
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

1. 使用 IntelliJ IDEA 打开 `back-end/voting_system` 目录

2. 等待 Maven 自动下载依赖

3. 运行 `VotingSystemApplication.java` 主启动类

4. 看到如下日志即为启动成功：
   ```
   Started VotingSystemApplication in x.xxx seconds
   ```
   后端服务默认运行在 **8081** 端口

![](https://cdn.jsdelivr.net/gh/hash-distill/PicGo-Repo@master/202602131237060.png)

---

### 三、前端构建

> 如果不需要修改前端代码，可以直接使用 `front-end/dist/` 目录下的构建产物，跳过此步。

1. 安装 Node.js 和 Yarn

2. 安装依赖并启动开发服务器：
   ```bash
   cd front-end
   yarn install
   yarn dev
   ```

3. 构建生产版本：
   ```bash
   yarn build
   ```
   构建完成后会在 `front-end/` 下生成 `dist/` 目录

<img src="https://cdn.jsdelivr.net/gh/hash-distill/PicGo-Repo@master/202602131244639.png" style="zoom: 67%;" />

> [!IMPORTANT]
> 前端 API 地址已集中管理在 `front-end/src/config.js` 中。部署时只需修改该文件中的 `API_BASE_URL` 为实际后端地址即可，无需逐个文件查找替换。
>
> ```js
> export const API_BASE_URL = 'http://实际部署IP:8081';
> ```

---

### 四、Nginx 配置与启动

#### 1. 部署前端文件

将 `front-end/dist` 文件夹复制并重命名为 `vote`，放入 Nginx 安装根目录：

![Nginx 目录结构](https://cdn.jsdelivr.net/gh/hash-distill/PicGo-Repo@master/202602131025311.png)

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

        # 后端 API 反向代理
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

<img src="https://cdn.jsdelivr.net/gh/hash-distill/PicGo-Repo@master/202602131304859.png" style="zoom: 50%;" />

#### 4. 访问系统

在浏览器（Chrome / Firefox / Edge）中访问 `http://<部署主机IP>` 即可使用系统。

打开后出现类似投票界面即为部署成功：

![投票界面示例](https://cdn.jsdelivr.net/gh/hash-distill/PicGo-Repo@master/202602131026557.png)

> [!TIP]
> 在访问系统时可能会出现前端页面能够正常打开，但下方的数据全为空的情况。可按照下面一一进行排查：
>
> 1、访问`http://localhost:8081/users`，如果有返回的 JSON 数据则说明后端已正常启动；
>
> 2 、`F12`打开浏览器控制台，查看控制台报错；
>
> 3、 查看`application.yml`下数据库连接配置是否有误，检查`SQL`数据库字段是否对应。





---

## 注意事项

1. **浏览器要求**：推荐使用  **Edge** 稳定版本访问

2. **投票参数设置**：每次投票前，管理员需先通过 `/set` 页面设置参数，否则投票接口会返回错误

3. **重投流程**：若投票过程中出现平票需要重投，务必确保所有评委界面从等待页（`/waiting`）跳转到投票页后再开始重投，否则可能导致数据不一致

4. **IP 地址配置**：部署时修改 `front-end/src/config.js` 中的 `API_BASE_URL`，构建后部署即可

5. **数据库密码**：生产环境部署时，请修改 `application.yml` 中的数据库密码，避免使用默认密码

6. **内网穿透**（可选）：可使用 natapp 等内网穿透工具进行外网访问，避免频繁更换后端 URL 配置

