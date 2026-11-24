# 模块连接

本机回环旁挂已停用。故障预测、CAD、周计划不再绑定 `127.0.0.1`。

现在两段，以后只留后一段：

```text
现在（同一台云服务器）
  浏览器 → AI 公网 IP 的 HTTPS 根地址
        → nginx 整站反代到这台机器的 http://127.0.0.1:3080
        → deepseek-harness 的 dsh web

以后（harness 挪走）
  浏览器 → 工作台入口
        → 按允许名单跳转或转发
        → 另一台云服务器的公网 https
```

AI 中台界面就是 deepseek-harness，不要再写一页。harness 只监听 `127.0.0.1:3080`，启动命令为 `dsh web --host 127.0.0.1 --port 3080 --no-open --trusted-host <AI公网IP[:端口]>`。nginx 在公网 TLS 端口整站反代到该端口；`GOOCI_AI_HUB_PUBLIC_URL` 配置为 `https://<AI公网IP>:<公网TLS端口>/?token=...`，使用 harness 启动输出的 token。证书必须包含公网 IP 的 IP SAN。设置 `GOOCI_AI_HUB_LOCAL_FORWARD=true` 并重启控制台；该开关只允许门口探活连接回环。不要在开发机启动。以后 harness 迁到其他云后，关闭此开关并按允许名单设置模块下一跳。

工作台砖块点击后直接在新标签页打开模块自己的公网 HTTPS 地址，浏览器不跳到控制台工作台路径。每个 `TARGET_URL` 必须配置为该模块可直接访问的完整公网路径（同一公网 IP 可用不同路径区分模块）；AI 功能使用 `GOOCI_AI_HUB_PUBLIC_URL`，功能码通过 `fn` 参数传给 harness。内网或回环 TARGET_URL 不下发给浏览器。

安全组只放行门口。库和文件不要对公网开放。
