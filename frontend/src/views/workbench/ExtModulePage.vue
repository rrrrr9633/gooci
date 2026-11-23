<template>
  <div class="org-ext-page org-ext-fill">
    <div v-if="appCode === 'AI_HUB' && hub?.bound" class="org-hub-bar">
      <button
        v-for="item in hub.catalog"
        :key="item.appCode"
        class="org-hub-fn"
        :class="{ on: item.appCode === fn }"
        type="button"
        @click="pick(item.appCode)"
      >{{ item.appName }}</button>
    </div>
    <iframe v-if="showFrame" class="org-ext-frame" :src="frameSrc" title="外接模块"></iframe>
    <div v-else-if="appCode === 'AI_HUB' && hub?.bound" class="org-app-grid org-hub-pick">
      <button v-for="item in hub.catalog" :key="item.appCode" class="org-app-tile" type="button" @click="pick(item.appCode)">
        <AppMark :code="item.appCode" :name="item.appName" />
        <span class="org-app-name">{{ item.appName }}</span>
      </button>
    </div>
    <div v-else class="org-app-empty">{{ emptyText }}</div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../../api/http'
import AppMark from '../../components/AppMark.vue'

const route = useRoute()
const router = useRouter()
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
const knownFn = computed(() => (hub.value?.catalog || []).some((item) => item.appCode === fn.value))
const showFrame = computed(() => {
  if (appCode.value === 'AI_HUB') return Boolean(hub.value?.bound && knownFn.value)
  return ok.value
})
const frameSrc = computed(() => {
  if (appCode.value === 'AI_HUB' && fn.value) {
    return `/ext/AI_HUB/?fn=${encodeURIComponent(fn.value)}`
  }
  return `/ext/${encodeURIComponent(appCode.value)}/`
})
const emptyText = computed(() => {
  if (appCode.value === 'AI_HUB' && !hub.value) return '正在确认中台绑定'
  if (appCode.value === 'AI_HUB' && !hub.value.bound) return hub.value.deny || 'AI中台尚未绑定'
  if (!ok.value) return '该模块尚未植入入口'
  return '该模块尚未植入入口'
})

function pick(code) {
  router.replace({ name: 'extModule', params: { appCode: 'AI_HUB' }, query: { fn: code } })
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
  } catch {
    hub.value = { bound: false, deny: 'AI中台状态读取失败', catalog: [] }
  }
}

watch(appCode, () => load(), { immediate: true })
</script>
