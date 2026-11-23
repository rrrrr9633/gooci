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

AI 中台界面就是 deepseek-harness，不要再写一页。现在和项目部署在同一台云服务器，在 harness 目录执行 `dsh web --host 127.0.0.1 --port 3080 --no-open`。把打印行里的 `token` 填进 `GOOCI_AI_HUB_TOKEN`，这台云再设 `GOOCI_AI_HUB_LOCAL_FORWARD=true` 并重启控制台。门口只放行 AI 中台这条回环，其它模块仍拒绝。不要在开发机启动。以后挪到被转发的云时，关掉这个开关，下一跳改成那台机器的公网 https。

工作台砖块入口是 `/workbench/ext/AI_HUB?fn={功能编码}`，页内就是 harness。不带 `fn` 时也是同一套 harness 界面。

安全组只放行门口。库和文件不要对公网开放。
