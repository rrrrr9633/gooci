#!/usr/bin/env python3
"""把接口文档与架构复习 Markdown 合成 docs/index.html。"""
from __future__ import annotations

import html
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
OUT = ROOT / "index.html"

PAGES = [
    ("api", "接口文档", ROOT / "接口文档.md"),
    ("arch", "架构复习 · 目录", ROOT / "架构复习" / "README.md"),
    ("arch-01", "01 产品形态与交付", ROOT / "架构复习" / "01-产品形态与交付.md"),
    ("arch-02", "02 模块植入与不停机", ROOT / "架构复习" / "02-模块植入与不停机.md"),
    ("arch-03", "03 域名备案与客户账号", ROOT / "架构复习" / "03-域名备案与客户账号.md"),
    ("arch-04", "04 云服务器拼法", ROOT / "架构复习" / "04-云服务器拼法.md"),
    ("arch-05", "05 小程序与数据接入", ROOT / "架构复习" / "05-小程序与数据接入.md"),
    ("arch-06", "06 磁盘与200G", ROOT / "架构复习" / "06-磁盘与200G.md"),
    ("arch-07", "07 总图与口诀", ROOT / "架构复习" / "07-总图与口诀.md"),
]

MD_LINK = {
    "README.md": "#arch",
    "01-产品形态与交付.md": "#arch-01",
    "02-模块植入与不停机.md": "#arch-02",
    "03-域名备案与客户账号.md": "#arch-03",
    "04-云服务器拼法.md": "#arch-04",
    "05-小程序与数据接入.md": "#arch-05",
    "06-磁盘与200G.md": "#arch-06",
    "07-总图与口诀.md": "#arch-07",
}


def inline(text: str) -> str:
    text = html.escape(text)

    def repl_link(match: re.Match[str]) -> str:
        label, href = match.group(1), match.group(2)
        target = MD_LINK.get(Path(href).name, href)
        return f'<a href="{html.escape(target)}">{label}</a>'

    text = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", repl_link, text)
    text = re.sub(r"`([^`]+)`", r"<code>\1</code>", text)
    text = re.sub(r"\*\*([^*]+)\*\*", r"<strong>\1</strong>", text)
    text = text.replace("  \n", "<br>\n")
    return text


def convert(md: str) -> str:
    lines = md.replace("\r\n", "\n").split("\n")
    out: list[str] = []
    i = 0
    in_code = False
    code: list[str] = []
    in_ul = False
    in_ol = False
    table: list[str] = []

    def close_lists() -> None:
        nonlocal in_ul, in_ol
        if in_ul:
            out.append("</ul>")
            in_ul = False
        if in_ol:
            out.append("</ol>")
            in_ol = False

    def flush_table() -> None:
        nonlocal table
        if not table:
            return
        rows = [row.strip() for row in table if row.strip()]
        table = []
        if len(rows) < 2:
            return
        def cells(row: str) -> list[str]:
            return [c.strip() for c in row.strip("|").split("|")]
        head = cells(rows[0])
        body_rows = rows[2:] if re.match(r"^\s*\|?\s*:?-{3,}", rows[1]) else rows[1:]
        buf = ["<table><thead><tr>"]
        buf.extend(f"<th>{inline(c)}</th>" for c in head)
        buf.append("</tr></thead><tbody>")
        for row in body_rows:
            buf.append("<tr>")
            buf.extend(f"<td>{inline(c)}</td>" for c in cells(row))
            buf.append("</tr>")
        buf.append("</tbody></table>")
        out.append("".join(buf))

    while i < len(lines):
        line = lines[i]
        if in_code:
            if line.startswith("```"):
                out.append("<pre><code>" + html.escape("\n".join(code)) + "</code></pre>")
                code = []
                in_code = False
            else:
                code.append(line)
            i += 1
            continue
        if line.startswith("```"):
            close_lists()
            flush_table()
            in_code = True
            code = []
            i += 1
            continue
        if line.strip().startswith("|"):
            close_lists()
            table.append(line)
            i += 1
            continue
        flush_table()
        if not line.strip():
            close_lists()
            i += 1
            continue
        if line.strip() in ("---", "***"):
            close_lists()
            out.append("<hr>")
            i += 1
            continue
        heading = re.match(r"^(#{1,6})\s+(.*)$", line)
        if heading:
            close_lists()
            level = len(heading.group(1))
            out.append(f"<h{level}>{inline(heading.group(2))}</h{level}>")
            i += 1
            continue
        ol = re.match(r"^\d+\.\s+(.*)$", line)
        if ol:
            if in_ul:
                out.append("</ul>")
                in_ul = False
            if not in_ol:
                out.append("<ol>")
                in_ol = True
            out.append(f"<li>{inline(ol.group(1))}</li>")
            i += 1
            continue
        ul = re.match(r"^[-*]\s+(.*)$", line)
        if ul:
            if in_ol:
                out.append("</ol>")
                in_ol = False
            if not in_ul:
                out.append("<ul>")
                in_ul = True
            out.append(f"<li>{inline(ul.group(1))}</li>")
            i += 1
            continue
        close_lists()
        out.append(f"<p>{inline(line)}</p>")
        i += 1
    flush_table()
    close_lists()
    if in_code:
        out.append("<pre><code>" + html.escape("\n".join(code)) + "</code></pre>")
    html_body = "\n".join(out)
    html_body = re.sub(r"<h1>.*?</h1>\n", "", html_body, count=1)
    return html_body


CSS = """
:root {
  --sky: #5ec0e4;
  --sky-deep: #3ba8d0;
  --ink: #1f2d3d;
  --muted: #5d7380;
  --line: #d7e8ef;
  --paper: #f4fafc;
}
* { box-sizing: border-box; }
html, body { margin: 0; height: 100%; font-family: "PingFang SC", "Microsoft YaHei", sans-serif; color: var(--ink); background: var(--paper); }
.wrap { display: flex; min-height: 100%; }
nav {
  width: 260px; flex-shrink: 0; background: #fff; border-right: 1px solid var(--line);
  padding: 18px 12px 32px; position: sticky; top: 0; height: 100vh; overflow: auto;
}
.brand { font-weight: 700; font-size: 18px; color: var(--sky-deep); padding: 4px 10px 16px; }
.group { font-size: 11px; color: var(--muted); letter-spacing: .08em; padding: 12px 10px 6px; }
nav a {
  display: block; color: var(--ink); text-decoration: none; padding: 7px 10px; border-radius: 8px; font-size: 13px;
}
nav a:hover, nav a.on { background: #e8f6fb; color: var(--sky-deep); }
main { flex: 1; padding: 36px 48px 80px; max-width: 980px; }
.page { display: none; }
.page.on { display: block; }
h1 { font-size: 28px; margin: 0 0 16px; }
h2 { font-size: 20px; margin: 32px 0 12px; padding-top: 8px; border-top: 1px solid var(--line); }
h3 { font-size: 16px; margin: 22px 0 8px; }
h4 { font-size: 14px; margin: 16px 0 6px; color: #246a86; }
p, li { line-height: 1.75; font-size: 14px; }
ul, ol { padding-left: 22px; }
table { border-collapse: collapse; width: 100%; margin: 12px 0 20px; font-size: 13px; background: #fff; }
th, td { border: 1px solid var(--line); padding: 8px 10px; text-align: left; vertical-align: top; }
th { background: #e8f6fb; color: #246a86; }
code { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 12px; background: #eef6f9; padding: 1px 5px; border-radius: 4px; }
pre { background: #163a48; color: #e8f6fb; padding: 14px 16px; border-radius: 10px; overflow: auto; font-size: 12px; line-height: 1.55; }
pre code { background: none; color: inherit; padding: 0; }
hr { border: 0; border-top: 1px solid var(--line); margin: 24px 0; }
a { color: var(--sky-deep); }
.top { height: 8px; background: var(--sky); position: sticky; top: 0; z-index: 5; }
"""


def main() -> None:
    nav = ['<div class="brand">GOOCI 文档</div>', '<div class="group">接口</div>']
    articles: list[str] = []
    for pid, label, path in PAGES:
        if pid == "arch":
            nav.append('<div class="group">架构</div>')
        nav.append(f'<a href="#{pid}">{label}</a>')
        body = convert(path.read_text(encoding="utf-8"))
        articles.append(f'<article class="page" id="{pid}"><h1>{html.escape(label)}</h1>\n{body}</article>')
    html_out = f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>GOOCI 文档</title>
<style>{CSS}</style>
</head>
<body>
<div class="top"></div>
<div class="wrap">
<nav>
{chr(10).join(nav)}
</nav>
<main>
  {chr(10).join(articles)}
</main>
</div>
<script>
const pages = document.querySelectorAll(".page");
const links = document.querySelectorAll("nav a");
function show(id) {{
  const key = id.replace(/^#/, "") || "api";
  pages.forEach((p) => p.classList.toggle("on", p.id === key));
  links.forEach((a) => a.classList.toggle("on", a.getAttribute("href") === "#" + key));
  window.scrollTo(0, 0);
}}
window.addEventListener("hashchange", () => show(location.hash));
show(location.hash);
</script>
</body>
</html>
"""
    OUT.write_text(html_out, encoding="utf-8")
    print(f"wrote {OUT}")


if __name__ == "__main__":
    main()
