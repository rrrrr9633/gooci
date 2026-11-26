import { ElMessage } from 'element-plus'
import http from './http'

export const AI_CODES = new Set(['AI_HUB', 'APP_EQKB', 'APP_SAFETY', 'APP_RDKB', 'APP_APS', 'APP_VOUCHER', 'APP_RECON'])

function openHref(href, replace) {
  if (replace) {
    window.location.replace(href)
    return true
  }
  const tab = window.open('about:blank', '_blank')
  if (!tab) {
    ElMessage.warning('浏览器拦截了新页面，请允许此站点弹出窗口')
    return false
  }
  tab.opener = null
  tab.location.replace(href)
  return true
}

export async function openAiLaunch(appCode, replace = false) {
  const launch = await http.post('/cloud-launch/issue', { appCode })
  const target = new URL('/gooci/enter', launch.publicOrigin)
  target.searchParams.set('code', launch.code)
  if (appCode !== 'AI_HUB') target.searchParams.set('fn', appCode)
  return openHref(target.href, replace)
}

export function openPublicModule(url, fn = '', replace = false) {
  if (!url) {
    ElMessage.warning('当前环境中的此模块没有可用的公网入口。')
    return false
  }
  let target
  try {
    target = new URL(url)
  } catch {
    ElMessage.error('模块公网入口配置无效')
    return false
  }
  const localHttp = target.protocol === 'http:' && ['localhost', '127.0.0.1', '::1'].includes(target.hostname)
  if ((!localHttp && target.protocol !== 'https:') || target.username || target.password) {
    ElMessage.error('模块公网入口必须是 HTTPS URL')
    return false
  }
  if (fn) target.searchParams.set('fn', fn)
  return openHref(target.href, replace)
}

export async function openWorkbenchApp(app, replace = false) {
  if (AI_CODES.has(app.appCode)) {
    try {
      return await openAiLaunch(app.appCode, replace)
    } catch {
      return false
    }
  }
  return openPublicModule(app.publicUrl, '', replace)
}
