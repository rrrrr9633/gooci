<template>
  <div class="org-ext-page org-ext-fill">
    <div class="org-app-empty">{{ statusText }}</div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import http from '../../api/http'

const route = useRoute()
const board = ref({ apps: [] })
const hub = ref(null)
const appCode = computed(() => String(route.params.appCode || ''))
const fn = computed(() => {
  const raw = route.query.fn
  const value = Array.isArray(raw) ? raw[0] : raw
  return value ? String(value) : ''
})
const current = computed(() => (board.value.apps || []).find((app) => app.appCode === appCode.value))
const statusText = computed(() => {
  if (!hub.value) return '正在打开独立模块'
  if (hub.value.error) return hub.value.error
  return '模块地址未配置'
})

function openTarget(url, fnCode = '') {
  if (!url) return false
  let target
  try {
    target = new URL(url)
    const localHttp = target.protocol === 'http:' && ['localhost', '127.0.0.1', '::1'].includes(target.hostname)
    if ((!localHttp && target.protocol !== 'https:') || target.username || target.password) return false
  } catch {
    return false
  }
  if (fnCode) target.searchParams.set('fn', fnCode)
  window.location.replace(target.href)
  return true
}

async function load() {
  hub.value = null
  if (appCode.value === 'AI_HUB') {
    const target = new URL('http://154.201.65.68:3080/?token=LVa5RYdo3QNBN2iUn6FFCpgmOw7B8-P_7mj88WM5EaQ')
    if (fn.value) target.searchParams.set('fn', fn.value)
    window.location.replace(target.href)
    return
  }
  try {
    board.value = await http.get('/console/biz/apps')
  } catch {
    board.value = { apps: [] }
  }
  const app = (board.value.apps || []).find((item) => item.appCode === appCode.value)
  if (!app?.publicUrl) {
    hub.value = { error: '独立模块公网地址尚未配置' }
    return
  }
  if (!openTarget(app.publicUrl, appCode.value === 'AI_HUB' ? fn.value : '')) {
    hub.value = { error: '独立模块地址无效' }
  }
}

watch(appCode, () => load(), { immediate: true })
</script>
