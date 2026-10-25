<template>
  <div class="org-ext-page org-ext-fill">
    <iframe v-if="ok" class="org-ext-frame" :src="frameSrc" title="外接模块"></iframe>
    <div v-else class="org-app-empty">该模块尚未植入入口</div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import http from '../../api/http'

const route = useRoute()
const board = ref({ apps: [] })
const appCode = computed(() => String(route.params.appCode || ''))
const current = computed(() => (board.value.apps || []).find((app) => app.appCode === appCode.value))
const ok = computed(() => Boolean(current.value?.entryPath))
const frameSrc = computed(() => `/ext/${encodeURIComponent(appCode.value)}/`)

onMounted(async () => {
  try {
    board.value = await http.get('/console/biz/apps')
  } catch {
    board.value = { apps: [] }
  }
})
</script>
