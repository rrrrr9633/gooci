<template>
  <div>
    <div class="block">
      <div class="hd">
        <span>企业智改数转工业APP统一总集</span>
      </div>
      <el-button type="primary" plain size="small" @click="$router.push('/workbench/biz/app')">配置入口</el-button>
    </div>
    <div v-if="apps.length" class="home-board">
      <div class="home-label">基础工具</div>
      <div class="home-apps">
        <button v-for="app in basicApps" :key="app.appCode" class="home-app" type="button" @click="openApp(app)">
          <AppMark :code="app.appCode" :name="app.appName" size="sm" />
          <span>{{ app.appName }}</span>
        </button>
      </div>
      <div class="home-line"></div>
      <div class="home-label">AI工具</div>
      <div class="home-apps">
        <button v-for="app in aiApps" :key="app.appCode" class="home-app" type="button" @click="openApp(app)">
          <AppMark :code="app.appCode" :name="app.appName" size="sm" />
          <span>{{ app.appName }}</span>
        </button>
      </div>
    </div>
    <div v-else class="empty-apps">暂无已开通模块</div>

    <div class="panel">
      <div class="hd">待办任务</div>
      <el-table :data="todos" empty-text="暂无数据">
        <el-table-column prop="bizLine" label="业务线" />
        <el-table-column prop="taskType" label="类型" />
        <el-table-column prop="content" label="内容" />
        <el-table-column prop="senderName" label="发送者" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <button v-if="row.handlePath" class="org-link" type="button" @click="router.push(row.handlePath)">处理</button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import http from '../../api/http'
import AppMark from '../../components/AppMark.vue'

const router = useRouter()
const board = ref({ apps: [] })
const todos = ref([])
const AI_CODES = new Set(['AI_HUB', 'APP_EQKB', 'APP_SAFETY', 'APP_RDKB', 'APP_APS', 'APP_VOUCHER', 'APP_RECON'])
const apps = computed(() => (board.value.apps || []).filter((app) => app.hideFlag !== '1'))
const basicApps = computed(() => apps.value.filter((app) => !AI_CODES.has(app.appCode)))
const aiApps = computed(() => apps.value.filter((app) => AI_CODES.has(app.appCode)))

function openApp(app) {
  if (app.appCode === 'AI_HUB') {
    window.open('http://154.201.65.68:3080/?token=LVa5RYdo3QNBN2iUn6FFCpgmOw7B8-P_7mj88WM5EaQ', '_blank', 'noopener,noreferrer')
    return
  }
  if (!app.publicUrl) {
    ElMessage.warning('该模块尚未配置公网入口路径')
    return
  }
  let target
  try {
    target = new URL(app.publicUrl)
  } catch {
    ElMessage.error('模块公网入口配置无效')
    return
  }
  const localHttp = target.protocol === 'http:' && ['localhost', '127.0.0.1', '::1'].includes(target.hostname)
  if ((!localHttp && target.protocol !== 'https:') || target.username || target.password) {
    ElMessage.error('模块公网入口必须是 HTTPS URL')
    return
  }
  if (AI_CODES.has(app.appCode) && app.appCode !== 'AI_HUB') target.searchParams.set('fn', app.appCode)
  const tab = window.open(target.href, '_blank', 'noopener,noreferrer')
  if (!tab) ElMessage.warning('浏览器拦截了新页面，请允许此站点弹出窗口')
}

onMounted(async () => {
  try {
    board.value = await http.get('/console/biz/apps')
  } catch {
    /* 占位 */
  }
  try {
    const slice = await http.get('/console/todo', { params: { pageNo: 1, pageSize: 10 } })
    todos.value = slice.records || []
  } catch {
    todos.value = []
  }
})
</script>

<style scoped>
.block, .panel { background: #fff; border-radius: 8px; padding: 16px 18px; margin-bottom: 16px; }
.hd { display: flex; align-items: baseline; gap: 10px; font-weight: 600; margin-bottom: 8px; }
.empty-apps {
  background: #fff;
  border-radius: 8px;
  padding: 48px;
  text-align: center;
  color: #9ab;
  margin-bottom: 16px;
}
.home-board {
  background: #fff;
  border-radius: 8px;
  padding: 8px 10px 12px;
  margin-bottom: 16px;
}
.home-label {
  padding: 14px 12px 0;
  font-size: 15px;
  font-weight: 650;
  color: #145860;
}
.home-line {
  height: 1px;
  margin: 4px 16px 0;
  background: #d5e4ec;
}
.home-apps {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
  padding: 18px 8px 22px;
}
.home-app {
  border: 0;
  background: transparent;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--gooci-text);
  font-size: 13px;
}
.home-app span {
  text-align: center;
  line-height: 1.35;
  max-width: 9.5em;
}
.home-app :deep(.app-mark) {
  filter: drop-shadow(0 8px 12px #14afe740);
}
.block { display: flex; justify-content: space-between; align-items: center; }
</style>
