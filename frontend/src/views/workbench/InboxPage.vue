<template>
  <div class="org-page">
    <div class="org-inbox-title">站内信</div>
    <div v-if="warn" class="org-inbox-warn">请先选择要操作的消息！</div>

    <div class="org-inbox-bar">
      <div class="org-inbox-left">
        <label class="org-inbox-radio">
          <input v-model="readFlag" type="radio" value="1" @change="reload">
          <span>已读</span>
        </label>
        <label class="org-inbox-radio">
          <input v-model="readFlag" type="radio" value="0" @change="reload">
          <span>未读</span>
        </label>
      </div>
    </div>

    <div class="org-inbox-tools">
      <div class="org-inbox-filters">
        <el-select v-model="rangeCode" style="width: 220px" @change="reload">
          <el-option label="全部" value="ALL" />
          <el-option label="今天" value="TODAY" />
          <el-option label="近三天" value="3D" />
          <el-option label="近一周" value="7D" />
          <el-option label="近一个月" value="30D" />
        </el-select>
        <el-select v-model="appCode" style="width: 240px" @change="reload">
          <el-option label="全部产品" value="" />
          <el-option v-for="app in board.apps" :key="app.appCode" :label="app.appName" :value="app.appCode" />
        </el-select>
      </div>
      <div class="org-inbox-actions">
        <button class="org-gold" type="button" :disabled="acting" @click="act('remove')">删除</button>
        <button class="org-gold" type="button" :disabled="acting" @click="act('read')">标记已读</button>
      </div>
    </div>

    <el-table
      class="org-staff-table"
      :data="slice.records"
      stripe
      empty-text="暂无数据"
      @selection-change="picked = $event"
    >
      <el-table-column type="selection" width="48" />
      <el-table-column prop="content" label="内容" min-width="360" />
      <el-table-column prop="sendTime" label="时间" width="180" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">{{ row.readFlag === '1' ? '已读' : '未读' }}</template>
      </el-table-column>
      <el-table-column prop="publisher" label="发布者" width="180" />
    </el-table>
    <OrgPager :slice="slice" @change="(page) => load(page)" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '../../api/http'
import OrgPager from '../../components/OrgPager.vue'

const empty = { total: 0, pageNo: 1, pageSize: 10, records: [] }
const board = ref({ apps: [], slice: empty })
const slice = computed(() => board.value.slice || empty)
const readFlag = ref('0')
const rangeCode = ref('ALL')
const appCode = ref('')
const picked = ref([])
const acting = ref(false)
const warn = ref(false)
let hideWarn

async function load(page = 1) {
  board.value = await http.get('/console/inbox', {
    params: {
      readFlag: readFlag.value,
      rangeCode: rangeCode.value,
      appCode: appCode.value || undefined,
      pageNo: page,
      pageSize: 10
    }
  })
  picked.value = []
}

function reload() {
  load(1)
}

function showWarn() {
  warn.value = true
  clearTimeout(hideWarn)
  hideWarn = setTimeout(() => { warn.value = false }, 2200)
}

async function act(kind) {
  const ids = picked.value.map((row) => row.id).filter(Boolean)
  if (!ids.length) {
    showWarn()
    return
  }
  acting.value = true
  try {
    await http.post(kind === 'remove' ? '/console/inbox/remove' : '/console/inbox/read', { ids })
    await load(1)
  } finally {
    acting.value = false
  }
}

onMounted(load)
</script>
