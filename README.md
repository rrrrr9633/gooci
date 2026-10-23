# GOOCI 谷器 · 工业互联网控制台

客户只看到一个域名。浏览器走同域路径，网关转到客户 VPC 里的模块机。模块端口不对浏览器开放，也不为模块再写第二条公网 A 记录。

## 对外入口

| 项 | 环境变量 | 暂定值 |
|---|---|---|
| 域名 | `GOOCI_PUBLIC_DOMAIN` | `goocidata.top` |
| 网关公网 IP（A 记录唯一目标） | `GOOCI_PUBLIC_IP` | `154.201.65.68` |
| 对外 origin | `GOOCI_PUBLIC_ORIGIN` | `https://goocidata.top` |

完整变量见仓库根 `.env.example`。填好的 `.env` 不入库。

## 流量

```text
浏览器  https://$GOOCI_PUBLIC_DOMAIN/
浏览器  https://$GOOCI_PUBLIC_DOMAIN/工作台/...
浏览器  https://$GOOCI_PUBLIC_DOMAIN/ext/{appCode}/   同域壳
网关    →  $GOOCI_EXT_*_URL                         客户 VPC 内网 TARGET_URL
```

`TARGET_URL` 只填内网。禁止填 `GOOCI_PUBLIC_IP`，禁止让浏览器直开模块端口。

## 启动（生产）

JDK 21、Maven、Node 18+。先注入环境变量，再：

```bash
cd backend && mvn spring-boot:run
```

默认 `prod` 档，MySQL 来自 `GOOCI_DB_*`。前端构建后与网关同域发布：

```bash
cd frontend && npm install && npm run build
```

接口文档：`docs/index.html`。
