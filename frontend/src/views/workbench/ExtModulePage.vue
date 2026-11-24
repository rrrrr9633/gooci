<template>
  <div class="org-ext-page org-ext-fill">
    <iframe v-if="showFrame" class="org-ext-frame" :src="frameSrc" title="外接模块"></iframe>
    <div v-else class="org-app-empty">{{ emptyText }}</div>
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
const ok = computed(() => Boolean(current.value?.entryPath))
const showFrame = computed(() => appCode.value !== 'AI_HUB' && ok.value)
const frameSrc = computed(() => `/ext/${encodeURIComponent(appCode.value)}/`)
const emptyText = computed(() => {
  if (appCode.value === 'AI_HUB' && !hub.value) return '正在打开 AI 中台'
  if (appCode.value === 'AI_HUB' && !hub.value.bound) return hub.value.deny || 'AI中台尚未绑定'
  if (appCode.value === 'AI_HUB' && !hub.value.launchUrl) return '尚未配置 AI 中台公网入口'
  if (!ok.value) return '该模块尚未植入入口'
  return '正在打开模块'
})

function openAiHub() {
  if (!hub.value?.bound || !hub.value.launchUrl) return
  let target
  try {
    target = new URL(hub.value.launchUrl)
  } catch {
    return
  }
  if (target.protocol !== 'https:' || target.username || target.password) return
  if (fn.value) target.searchParams.set('fn', fn.value)
  window.location.replace(target.href)
}

async function load() {
  hub.value = null
  try {
    board.value = await http.get('/console/biz/apps')
  } catch {
    board.value = { apps: [] }
  }
  if (appCode.value !== 'AI_HUB') return
  try {
    hub.value = await http.get('/console/ai/hub')
    openAiHub()
  } catch {
    hub.value = { bound: false, deny: 'AI中台状态读取失败', catalog: [] }
  }
}

watch(appCode, () => load(), { immediate: true })
</script>
