<template>
  <div class="org-ext-page org-ext-fill">
    <div class="org-app-empty">{{ statusText }}</div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { AI_CODES, openAiLaunch, openPublicModule } from '../../api/moduleLaunch'
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
const statusText = computed(() => {
  if (!hub.value) return '正在打开独立模块'
  if (hub.value.error) return hub.value.error
  return '模块地址未配置'
})

async function load() {
  hub.value = null
  try {
    board.value = await http.get('/console/biz/apps')
  } catch {
    board.value = { apps: [] }
  }
  if (AI_CODES.has(appCode.value)) {
    try {
      await openAiLaunch(appCode.value === 'AI_HUB' && fn.value ? fn.value : appCode.value, true)
    } catch {
      hub.value = { error: '当前账号没有可用的 AI 入口。' }
    }
    return
  }
  const app = (board.value.apps || []).find((item) => item.appCode === appCode.value)
  if (!openPublicModule(app?.publicUrl, '', true)) {
    hub.value = { error: '独立模块公网地址尚未配置' }
  }
}

watch(appCode, () => load(), { immediate: true })
</script>
