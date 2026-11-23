import { randomUUID } from 'node:crypto'
import { mkdir, readFile, rename, writeFile } from 'node:fs/promises'
import { createServer } from 'node:http'
import { dirname, join } from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'
import { catalog, known } from './catalog.mjs'

const root = dirname(fileURLToPath(import.meta.url))
const dataFile = process.env.AI_HUB_DATA || join(root, 'data', 'boards.json')
const pageFile = join(root, 'page.html')
const host = process.env.AI_HUB_HOST || '127.0.0.1'
const port = Number(process.env.AI_HUB_PORT || 8787)
const maxBody = 8192

let chain = Promise.resolve()

function header(req, name) {
  const raw = req.headers[name]
  const value = Array.isArray(raw) ? raw[0] : raw
  return value ? String(value).trim() : ''
}

function enterId(req) {
  const given = header(req, 'x-gooci-enter-id')
  return (given || 'LOCAL').slice(0, 64)
}

function fnOf(req, url) {
  return known(header(req, 'x-gooci-fn')) || known(url.searchParams.get('fn'))
}

function session(req, url) {
  const fn = fnOf(req, url)
  return {
    enterId: enterId(req),
    appCode: header(req, 'x-gooci-app-code') || 'AI_HUB',
    door: header(req, 'x-gooci-door') || 'direct',
    fn: fn ? fn.code : '',
    catalog
  }
}

async function loadBoards() {
  try {
    return JSON.parse(await readFile(dataFile, 'utf8'))
  } catch {
    return {}
  }
}

function changeBoards(mutate) {
  const run = chain.then(async () => {
    const boards = await loadBoards()
    const result = await mutate(boards)
    await mkdir(dirname(dataFile), { recursive: true })
    const temp = dataFile + '.tmp'
    await writeFile(temp, JSON.stringify(boards))
    await rename(temp, dataFile)
    return result
  })
  chain = run.then(() => {}, () => {})
  return run
}

function itemsOf(boards, enter, code) {
  const bucket = boards[enter] && boards[enter][code]
  return Array.isArray(bucket) ? bucket : []
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    const chunks = []
    let size = 0
    req.on('data', (chunk) => {
      size += chunk.length
      if (size > maxBody) {
        reject(new Error('内容过大'))
        req.destroy()
        return
      }
      chunks.push(chunk)
    })
    req.on('end', () => resolve(Buffer.concat(chunks).toString('utf8')))
    req.on('error', reject)
  })
}

function send(res, status, body, type) {
  const payload = Buffer.from(body)
  res.writeHead(status, {
    'Content-Type': type,
    'Content-Length': payload.length,
    'Cache-Control': 'no-store'
  })
  res.end(payload)
}

function sendJson(res, status, value) {
  send(res, status, JSON.stringify(value), 'application/json; charset=utf-8')
}

export async function handle(req, res) {
  const url = new URL(req.url || '/', 'http://127.0.0.1')
  const path = url.pathname
  if (path.includes('..')) {
    sendJson(res, 400, { error: '路径无效' })
    return
  }
  if (req.method === 'GET' && (path === '/' || path === '/index.html')) {
    send(res, 200, await readFile(pageFile), 'text/html; charset=utf-8')
    return
  }
  if (req.method === 'GET' && path === '/health') {
    sendJson(res, 200, { ok: true, service: 'ai-hub' })
    return
  }
  if (req.method === 'GET' && path === '/api/session') {
    sendJson(res, 200, session(req, url))
    return
  }
  if (path === '/api/items') {
    const fn = fnOf(req, url)
    if (!fn) {
      sendJson(res, 400, { error: '未知功能' })
      return
    }
    const enter = enterId(req)
    if (req.method === 'GET') {
      const boards = await loadBoards()
      sendJson(res, 200, { fn: fn.code, items: itemsOf(boards, enter, fn.code) })
      return
    }
    if (req.method === 'POST') {
      const body = JSON.parse(await readBody(req) || '{}')
      if (body.op === 'remove') {
        const next = await changeBoards((boards) => {
          const kept = itemsOf(boards, enter, fn.code).filter((item) => item.id !== body.id)
          boards[enter] = { ...(boards[enter] || {}), [fn.code]: kept }
          return kept
        })
        sendJson(res, 200, { fn: fn.code, items: next })
        return
      }
      const left = String(body.left || '').trim().slice(0, 80)
      const right = String(body.right || '').trim().slice(0, 200)
      if (!left || !right) {
        sendJson(res, 400, { error: '两项都要填' })
        return
      }
      const next = await changeBoards((boards) => {
        const item = { id: randomUUID(), left, right, at: new Date().toISOString() }
        const kept = [item, ...itemsOf(boards, enter, fn.code)].slice(0, 100)
        boards[enter] = { ...(boards[enter] || {}), [fn.code]: kept }
        return kept
      })
      sendJson(res, 200, { fn: fn.code, items: next })
      return
    }
  }
  sendJson(res, 404, { error: '没有这个路径' })
}

export function createHub() {
  return createServer((req, res) => {
    handle(req, res).catch((error) => {
      if (!res.headersSent) sendJson(res, 400, { error: error.message || '请求无效' })
    })
  })
}

const invoked = process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href
if (invoked) {
  const server = createHub()
  server.on('error', (error) => {
    process.stderr.write(`ai-hub 未能监听 ${host}:${port} ${error.code || error.message}\n`)
    process.exit(1)
  })
  server.listen(port, host, () => {
    process.stdout.write(`ai-hub ${host}:${port}\n`)
  })
}
