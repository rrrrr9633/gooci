<template>
  <div class="org-page">
    <div class="org-inbox-title">待办任务</div>
    <p class="org-inbox-hint">点击任务条目，可以查看任务消息详情；点击处理按钮，快速跳转到对应任务处理页面。</p>
    <el-table class="org-staff-table" :data="slice.records" stripe empty-text="暂无数据" @row-click="openRow">
      <el-table-column prop="bizLine" label="业务线" min-width="160" />
      <el-table-column prop="taskType" label="类型" min-width="140" />
      <el-table-column prop="content" label="内容" min-width="280" />
      <el-table-column prop="senderName" label="发送者" min-width="140" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <button v-if="row.id" class="org-link" type="button" @click.stop="handle(row)">处理</button>
        </template>
      </el-table-column>
    </el-table>
    <OrgPager :slice="slice" @change="(page) => load(page)" />

    <el-dialog
      v-model="detailOpen"
      title="任务详情"
      width="560px"
      class="org-pick-dialog"
      header-class="org-pick-head"
      :close-on-click-modal="false"
    >
      <div class="org-auth-kv">
        <div><span>业务线：</span>{{ detail.bizLine || '—' }}</div>
        <div><span>类型：</span>{{ detail.taskType || '—' }}</div>
        <div><span>发送者：</span>{{ detail.senderName || '—' }}</div>
      </div>
      <p class="org-todo-body">{{ detail.content || '—' }}</p>
      <template #footer>
        <button class="org-gold" type="button" @click="handle(detail)">处理</button>
        <button class="org-btn-dark lg" type="button" @click="detailOpen = false">关闭</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import OrgPager from '../../components/OrgPager.vue'

const empty = { total: 0, pageNo: 1, pageSize: 10, records: [] }
const slice = ref(empty)
const detailOpen = ref(false)
const detail = ref({})
const router = useRouter()

async function load(page = 1) {
  slice.value = await http.get('/console/todo', { params: { pageNo: page, pageSize: 10 } })
}

function openRow(row) {
  if (!row?.id) return
  detail.value = row
  detailOpen.value = true
}

function handle(row) {
  if (!row?.handlePath) {
    ElMessage.info('处理页尚未接入')
    return
  }
  router.push(row.handlePath)
}

onMounted(load)
</script>
