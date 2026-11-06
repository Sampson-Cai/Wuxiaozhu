# 物小助 · 智能补货采购平台

面向中小规模单位的补货、采购、库存与应付管理系统。后端基于 RuoYi 4.8.3（Spring Boot 4.1 / JDK 17）改造为 JSON API，前端为 uni-app 微信小程序，内置基于知识库的 AI 助手（RAG + SSE 流式）。

> 2026-11-06 · 后端 `https://demo7.ztcoding.com`

## 目录结构

```
.
├── backend/          Spring Boot 后端（多模块 Maven）
│   ├── ruoyi-admin       启动模块、小程序接口控制器、application*.yml
│   ├── ruoyi-framework   鉴权、拦截器、Shiro 配置
│   ├── ruoyi-system      业务 Service / Mapper（含 AI 模块）
│   ├── ruoyi-common      通用工具与响应体
│   ├── sql/              建表、菜单权限脚本
│   └── doc/              接口说明、开发规范、部署说明
└── miniprogram/      uni-app 小程序（Vue2）
    ├── pages/            39 个页面
    ├── api/              业务接口封装
    ├── utils/            请求、鉴权、服务地址、SSE 流式、本地问答
    └── styles/           iOS 风格设计系统
```

## 技术栈

| 层 | 选型 |
| --- | --- |
| 后端 | JDK 17、Spring Boot 4.1.0、MyBatis、Druid、Shiro |
| 数据库 | MySQL 8.0（utf8mb4） |
| 小程序 | uni-app + Vue2，编译到 mp-weixin（主包 1.74MB + 16 个分包） |
| 对话模型 | DeepSeek-V4.1-Flash（`deepseek-flash`，关闭思考） |
| 向量模型 | `Qwen/Qwen3-Embedding-0.6B`（硅基流动，1024 维） |

## 功能

- 档案：商品分类、商品（含物料图）、供应商（删除带引用校验）
- 库存：出入库、流水、手工流水冲销、安全库存预警
- 采购：建单 → 提交 → 审批 → 分批收货 → 自动生成应付
- 财务：应付账款、部分付款、到期与逾期筛选
- 补货：参数维护、规则计算（含在途抵扣）、建议转采购草稿
- 系统：账号与角色、操作日志、单位信息、导出与打印
- AI：知识库切片向量化 + 语义检索 + 实时业务快照 + SSE 流式输出，答案带来源标注

## 快速开始

### 后端

```bash
cd backend
# 1. 建库并导入脚本
mysql -h <host> -u <user> -p <db> < sql/ry_20260319.sql
mysql -h <host> -u <user> -p <db> < sql/quartz.sql
mysql -h <host> -u <user> -p <db> < sql/cangxiaozhu_schema.sql
mysql -h <host> -u <user> -p <db> < sql/cangxiaozhu_menu.sql

# 2. 打包
mvn -B -DskipTests package

# 3. 启动（口令与模型密钥走环境变量）
export DB_HOST=127.0.0.1 DB_NAME=cangxiaozhu DB_USER=root DB_PASSWORD=yourpass
export AI_CHAT_KEY=sk-xxx AI_EMBEDDING_KEY=sk-xxx
java -jar ruoyi-admin/target/ruoyi-admin.jar --server.port=18080
```

### 小程序

```bash
cd miniprogram
npm install
npm run build:mp-weixin
# 微信开发者工具导入 dist/build/mp-weixin，勾选「不校验合法域名」
```

后端地址在 `miniprogram/config.js`，也可在 App 内「我的 → 应用设置 → 服务器地址」运行时修改。

## 部署

生产环境：`152.136.137.81`，域名 `https://demo7.ztcoding.com`。详细步骤见 `backend/doc/部署运行说明.md`。

| 组件 | 说明 |
| --- | --- |
| 进程管理 | systemd 单元 `qixiaozhu.service`，开机自启、异常 5s 重启 |
| Web 服务 | 宝塔 nginx 1.24，站点配置 `/www/server/panel/vhost/nginx/demo7.ztcoding.com.conf` |
| 后端监听 | `127.0.0.1:18080`，仅本机，公网只能经 nginx 443 进入 |
| 证书 | Let's Encrypt，webroot 续期，续期后自动 reload nginx |
| 环境变量 | `/opt/qixiaozhu/app.env`（600，含库口令与模型密钥，不入库） |

```bash
sudo systemctl status qixiaozhu      # 查看状态
sudo systemctl restart qixiaozhu     # 重启后端
sudo systemctl restart nginx         # 重启 nginx
tail -f /opt/qixiaozhu/logs/stdout.log
```

### 安全基线

- 登录验证码开启（`shiro.user.captchaEnabled: true`），验证码按 `uuid` 缓存 2 分钟、一次性失效
- 公网只放行 `22`、`80`、`443`、`16988`（面板），MySQL、18080、888、8888、FTP 端口均已关闭
- 数据库仅监听本机，账号 `wxz@localhost`


## 默认账号

| 账号 | 口令 | 角色 |
| --- | --- | --- |
| admin | admin123 | 超级管理员 |
| ry | admin123 | 普通角色 |

## 文档

- `backend/doc/接口说明.md`：全部接口、权限点、AI 与 SSE 流式接口
- `backend/doc/开发规范.md`：分层、命名、删除校验、AI 约定、小程序 UI 规范
- `backend/readme.md`：开发进度与问题记录

## 许可

见 LICENSE。
