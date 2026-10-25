# GOOCI 本机旁挂模块运行手册

## 进程与端口

| 项目 | 进程/启动命令 | 监听 | 检查 |
|---|---|---|---|
| gooci | 现有 JVM（不重启） | `*:8080`，由 nginx 对外提供同域入口 | `curl http://127.0.0.1:8080/api/site/meta` |
| Big-Data-Modle | `python3 -m http.server 5174 --bind 127.0.0.1 --directory /root/Big-Data-Modle/frontend/dist` | `127.0.0.1:5174` | `curl -I http://127.0.0.1:5174/` |
| cad-transform | `/root/cad-transform/backend/.venv/bin/uvicorn app.main:app --host 127.0.0.1 --port 8777` | `127.0.0.1:8777` | `curl -I http://127.0.0.1:8777/` |
| weekly_plan_web | `/usr/bin/python3 /root/weekly_plan_web/proxy_server.py`（静态文件 + `/api` 反代） | `127.0.0.1:5175` | `curl -I http://127.0.0.1:5175/` |

日志：`/root/runlogs/gooci-ext-{pdm,cad,week}.log`。服务由 systemd 保活：`systemctl status gooci-ext-pdm gooci-ext-cad gooci-ext-week`；停止/启动分别使用 `systemctl stop|start <服务名>`。

## 生产构建

```bash
cd /root/Big-Data-Modle/frontend && npm run build
cd /root/weekly_plan_web && npm run build
cd /root/gooci/frontend && npm run build && cp -a dist/. /var/www/gooci/
```

周计划浏览器请求使用同域 `/api`，本机代理再转发到原后端 `https://tianxiadiyi.xyz/api`，用于避免远端后端未放行 gooci 域名导致的 CORS 失败。构建时使用 `VITE_API_BASE_URL=/api npm run build`。

## 已植入模块

运营接口：`POST /api/ops/auth/login`（厂商账号）后调用 `POST /api/ops/apps/implant`。当前企业为 `7c19ca07-9b32-4fb3-a0ad-8beec893bba6`：

```json
{"enterId":"7c19ca07-9b32-4fb3-a0ad-8beec893bba6","appCode":"EXT_PDM","appName":"设备故障预测","entryPath":"/workbench/ext/EXT_PDM","targetUrl":"http://127.0.0.1:5174"}
{"enterId":"7c19ca07-9b32-4fb3-a0ad-8beec893bba6","appCode":"EXT_CAD","appName":"CAD拆解","entryPath":"/workbench/ext/EXT_CAD","targetUrl":"http://127.0.0.1:8777"}
{"enterId":"7c19ca07-9b32-4fb3-a0ad-8beec893bba6","appCode":"EXT_WEEK","appName":"周计划","entryPath":"/workbench/ext/EXT_WEEK","targetUrl":"http://127.0.0.1:5175"}
```

验收：`curl -I http://127.0.0.1:8080/ext/EXT_PDM/`（CAD/WEEK 同理）应返回 200；模块端口只绑定环回。
