<template>
  <div class="hold">
    <h3>{{ title }}</h3>
    <p>页面设计暂留，后续按页开发。</p>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import http from '../../api/http'

const NAMES = {
  EXT_CAD: 'CAD拆解',
  EXT_WEEK: '周计划',
  APP_AFTER: '售后管理',
  APP_WORK: '轻工单',
  APP_MONITOR: '设备监控平台',
  APP_WMS: '轻仓储',
  APP_TRACE: '在线追溯',
  APP_ENERGY: '能源管理',
  APP_MAINT: '设备维保',
  APP_EQKB: 'AI设备知识库',
  APP_DRAW: '图档管理',
  APP_ESOP: 'ESOP管理',
  APP_SAFETY: '安全生产AI行为监控',
  APP_VISIT: '入场预约',
  APP_EDLP: '企业终端安全管理',
  APP_RDKB: '知识库研发辅助',
  APP_APS: 'AI排程预测',
  APP_VOUCHER: 'AI凭证自动处理',
  APP_RECON: 'AI自动对账',
  APP_OA: '协同办公'
}

const route = useRoute()
const title = ref(route.meta.title || '模块')

onMounted(async () => {
  const code = String(route.params.appCode || '')
  title.value = NAMES[code] || route.meta.title || '模块'
  if (!code) return
  try {
    const board = await http.get('/console/biz/apps')
    const hit = (board.apps || []).find((app) => app.appCode === code)
    if (hit?.appName) title.value = hit.appName
  } catch {
    /* 占位名已够 */
  }
})
</script>

<style scoped>
.hold {
  background: #fff;
  min-height: 420px;
  border-radius: 8px;
  padding: 48px;
  color: #667;
}
h3 { color: #123445; margin: 0 0 8px; }
</style>
