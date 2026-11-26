# GOOCI 谷器 · 工业互联网控制台

官网与工作台已拆开。当前只交付内核壳：首页、登录、工作台布局、图 4 左侧菜单。各业务页先占位。

## 不合适、已主动改掉的点

- 首页不放「产品 / 解决方案 / 新闻 / 社区 / 关于我们」，也不放对照图里那列产品线。
- 工作台不预装 20 个业务模块，已开通区为空。
- 数据库用 **utf8mb4**，不用规范里的 `utf8`。
- 前端是 **Vue 3 + Vite + Element Plus**，不是 Vue 2。
- 菜单由后端下发，给后面「按需植入模块」留口。
- 本机开发默认 **H2**。本机已有 mysqld 占着 3306，Docker 不要再映射 3306。

Spring Boot + Vue + MySQL 适合这套工作台。正式环境用 MySQL。

## 启动

需要 JDK 21、Maven、Node 18+。

**日常开发（推荐）**：不要依赖 Docker。在 `backend` 目录：

```bash
mvn spring-boot:run
```

默认 `local` 档，H2 文件库 `backend/data/`。

前端（另开终端，在 `frontend` 目录）：

```bash
npm install && npm run dev
```

- 官网：http://127.0.0.1:5173/
- 演示账号：`admin` / `GooCi@2026`
- 模块下一跳必须是允许名单内的公网 https，由门口 `/ext/{appCode}` 转发。AI 功能共用 `AI_HUB`，界面是 deepseek-harness 的 `dsh web`，不是控制台页面。现在同机转到 `127.0.0.1:3080`，`GOOCI_AI_HUB_LOCAL_FORWARD=true` 只放行这一条。以后 harness 挪走，关掉该开关，下一跳改为那台机器的公网 https。
- 接口文档：打开 `docs/index.html`（Markdown 原稿仍在同目录）

### 可选：Docker MySQL

必须在**仓库根目录**执行（在 `backend` 里 `cd infra` 会失败，13306 起不来）：

```bash
cd infra && docker compose up -d
cd ../backend && mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Docker 映射 **13306**，账号 `gooci` / `gooci_pwd`。等容器 healthy 再启后端。

### Docker 交付底座

两个交付清单复用同一组工作台镜像，但使用不同的 Compose 项目和数据库卷。下面是工作台运行方式，不包含 Harness；交付模式默认关闭旧的 `/ext/` 代理。

在仓库根目录准备独立的环境文件，通过 `--env-file` 指定。云版和本地版不要复用密钥或数据库卷。设置 `GOOCI_DB_ROOT_PASSWORD`、`GOOCI_DB_PASSWORD`、`GOOCI_JWT_SECRET`、`GOOCI_BOOTSTRAP_ADMIN_PASSWORD` 和 `GOOCI_BOOTSTRAP_OPS_PASSWORD`。两个初始账号密码至少 16 个字符且不超过 72 个 UTF-8 字节；只在创建账号时生效，不用于重置现有密码。JWT 密钥使用至少 32 字节的独立随机值。

```bash
# 本地版：默认 http://127.0.0.1:8080
docker compose --env-file /path/to/local.env -f infra/compose.delivery.yml -f infra/compose.local.yml up -d --build

# 云版：由同机的 HTTPS 入口转发到 127.0.0.1:8080
docker compose --env-file /path/to/cloud.env -f infra/compose.delivery.yml -f infra/compose.cloud.yml up -d --build
```

本地版在安装环境文件里设置 `GOOCI_BIND_IP`，然后重新运行同一条 Compose 命令。默认是 `127.0.0.1`；若监听局域网地址，应由安装者配置 HTTPS 入口后再供团队使用。两个版本都不发布数据库端口。云版和本地版在同一台机器上不能同时占用同一监听地址的 `8080`。

## 目录

```text
backend/     Spring Boot 控制台 API
frontend/    Vue3 官网 + 工作台
infra/       MySQL 8（可选）
modules/     外接模块壳（可拷到独立云主机）
docs/        架构复习、模块清单、接口文档
```
