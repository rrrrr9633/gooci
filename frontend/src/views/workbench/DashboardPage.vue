<template>
  <div>
    <div class="block">
      <div class="hd">
        <span>已开通模块</span>
      </div>
      <el-button type="primary" plain size="small" @click="$router.push('/workbench/biz/app')">配置入口</el-button>
    </div>
    <div v-if="apps.length" class="home-apps">
      <button v-for="app in apps" :key="app.appCode" class="home-app" type="button" @click="openApp(app)">
        <AppMark :code="app.appCode" size="sm" />
        <span>{{ app.appName }}</span>
      </button>
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
import { useRouter } from 'vue-router'
import http from '../../api/http'
import AppMark from '../../components/AppMark.vue'

const router = useRouter()
const board = ref({ apps: [] })
const todos = ref([])
const apps = computed(() => (board.value.apps || []).filter((app) => app.hideFlag !== '1'))

function openApp(app) {
  if (app.entryPath) router.push(app.entryPath)
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
.home-apps {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
  background: #fff;
  border-radius: 8px;
  padding: 20px 18px 28px;
  margin-bottom: 16px;
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
