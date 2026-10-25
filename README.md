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
- 外接模块本机口：把进程开在 `http://127.0.0.1:8091`，工作台「演示外接模块」会转过去。静态壳：`cd modules/demo-shell && python3 -m http.server 8091 --bind 127.0.0.1`
- 接口文档：打开 `docs/index.html`（Markdown 原稿仍在同目录）

### 可选：Docker MySQL

必须在**仓库根目录**执行（在 `backend` 里 `cd infra` 会失败，13306 起不来）：

```bash
cd infra && docker compose up -d
cd ../backend && mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Docker 映射 **13306**，账号 `gooci` / `gooci_pwd`。等容器 healthy 再启后端。

## 目录

```text
backend/     Spring Boot 控制台 API
frontend/    Vue3 官网 + 工作台
infra/       MySQL 8（可选）
modules/     外接模块壳（可拷到独立云主机）
docs/        架构复习、模块清单、接口文档
```
