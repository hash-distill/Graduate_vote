# 研究生支教团投票系统
# 前端React（类组件） + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react/README.md) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh



## 前端技术栈：yarn+vite+react

## 前端项目安装与启动方式

- 克隆项目：
    ```
    git clone [git@github.com:zkm222/Graduate_vote.git](https://github.com/zkm222/Graduate_vote.git)
    ```

- 安装相关依赖：使用前需要在电脑上安装node.js再安装yarn
    ```
    cd Graduate_vote
    cd front-end
    yarn
    ```

- 启动调试：
    ```
    yarn dev
    ```

- 部署代码构建：
    ```
    yarn build
    使用后在front-end文件夹会生成dist文件
    ```
## 前端项目pages页面功能介绍
- set：参数设置界面，学生人数（即正选人数，默认为2名候补，修改候补人数需要修改后端），教师人数（参与投票评委人数），每人限投（每位评委组多投票数量）
- vote：用户投票界面
- userShow：投票结束，用户结果展示界面
- waiting：用户投票结束等待界面（注意：若投票流程出现重投，需保证所有评委界面从等待界面跳转到投票界面再开始，否则可能出现问题）
- end：用户投票结束界面
- show：管理原实时监控投票结果界面

## 前端项目请求IP修改，此为早期项目，未设置axios拦截器设置统一baseurl，可使用vscode全局搜索与替换
![1](https://github.com/user-attachments/assets/1053cefe-b155-4667-9f2f-44c2d7f3647a)


## 项目接口文档
- 文档链接：[https://www.showdoc.com.cn/2422346087980335/10758172736244558](https://www.showdoc.com.cn/2366617952900052/10540793226756561)
- 文档密码：666666


# 后端
## 1. 代码位置
	如上/backend/voting_system

## 2. 使用步骤
### 2.1  配置nginx
#### （1） 将前端打包的文件dist改名vote放入nginx根目录或将前端dist文件夹内容覆盖vote文件夹内容
![nginx根目录](https://i-blog.csdnimg.cn/direct/9a3b3fe43afc4b6f8998a4c85fe1c8f3.png)
#### （2）修改配置文件./conf/nginx.conf

```
http {
    include       mime.types;
    default_type  application/octet-stream;

    sendfile        on;

    keepalive_timeout  65;

    server {
        listen       80;
        server_name  localhost;

        location / {
            root   vote;
            index  index.html index.htm;
            try_files $uri $uri/ /index.html;  
        }

        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
		}
    }
```

#### （3） 启动nginx

### 2.2 启动后端
#### （1）将后端代码克隆到本地，使用idea打开，运行，即可启动后端
#### （2）在浏览器键入`http://ip`即可使用系统（注：ip为启动后端的主机的局域网ip，如http://localhost）

