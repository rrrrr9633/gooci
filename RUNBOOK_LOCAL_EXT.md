# 模块连接

本机回环旁挂已停用。故障预测、CAD、周计划不再绑定 `127.0.0.1`。

现在两段，以后只留后一段：

```text
现在（同一台云服务器）
  浏览器 → 已备案域名 /ext/AI_HUB
        → 门口转到这台机器的 http://127.0.0.1:3080
        → deepseek-harness 的 dsh web

以后（harness 挪走）
  浏览器 → 已备案域名 /ext/{appCode}
        → 门口按允许名单转发
        → 另一台云服务器的公网 https
```

AI 中台界面就是 deepseek-harness，不要再写一页。harness 只监听 `127.0.0.1:3080`，启动命令为 `dsh web --host 127.0.0.1 --port 3080 --no-open --trusted-host <AI公网IP[:端口]>`。nginx 在公网 TLS 端口整站反代到该端口；`GOOCI_AI_HUB_PUBLIC_URL` 配置为 `https://<AI公网IP>:<公网TLS端口>/?token=...`，使用 harness 启动输出的 token。证书必须包含公网 IP 的 IP SAN。设置 `GOOCI_AI_HUB_LOCAL_FORWARD=true` 并重启控制台；该开关只允许门口探活连接回环。不要在开发机启动。以后 harness 迁到其他云后，关闭此开关并按允许名单设置模块下一跳。

工作台所有模块砖块均在新标签页打开公网入口，使用 `/ext/{appCode}/` 区分模块，不再嵌入 iframe。AI 功能砖块先打开 AI 中台独立页并带 `fn` 参数。

安全组只放行门口。库和文件不要对公网开放。
