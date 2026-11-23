<template>
  <div class="org-page">
    <div class="org-crumb">APP授权管理</div>
    <div class="org-rule" />
    <div class="org-perm-bar">
      <div class="org-perm-left">
        <span>手机号或姓名</span>
        <el-input v-model="draftKey" placeholder="请输入手机号或姓名" style="width: 220px" @keyup.enter="query" />
      </div>
      <div class="org-perm-right">
        <button class="org-gold" type="button" @click="query">查询</button>
      </div>
    </div>
    <el-table class="org-staff-table" :data="slice.records" stripe empty-text="暂无账号">
      <el-table-column prop="mobile" label="手机号" min-width="140" />
      <el-table-column prop="displayName" label="姓名" min-width="100" />
      <el-table-column prop="deptName" label="部门" min-width="160" />
      <el-table-column prop="mailAddr" label="邮箱" min-width="180" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <button class="org-link" type="button" @click="openDetail(row)">详情</button>
          <button class="org-link" type="button" @click="openAssign(row, 'FUNC')">功能角色</button>
          <button class="org-link" type="button" @click="openAssign(row, 'DATA')">数据角色</button>
        </template>
      </el-table-column>
    </el-table>
    <OrgPager :slice="slice" @change="(page) => load(page)" />

    <el-dialog
      v-model="detailOpen"
      title="详情"
      width="760px"
      class="org-pick-dialog"
      header-class="org-pick-head"
      :close-on-click-modal="false"
    >
      <div class="org-auth-kv">
        <div><span>姓名：</span>{{ detail.displayName || '—' }}</div>
        <div><span>手机号：</span>{{ detail.mobile || '—' }}</div>
        <div><span>邮箱：</span>{{ detail.mailAddr || '—' }}</div>
        <div><span>角色：</span>{{ detail.roleName || '—' }}</div>
        <div><span>所属部门：</span>{{ detail.deptName || '—' }}</div>
      </div>
      <el-table class="org-staff-table" :data="detail.grants" stripe empty-text="暂无已开通应用">
        <el-table-column prop="appName" label="已开通APP" min-width="180" />
        <el-table-column prop="funcRoleName" label="功能角色" min-width="180" />
        <el-table-column prop="dataRoleName" label="数据角色" min-width="180" />
      </el-table>
      <template #footer>
        <button class="org-btn-dark lg" type="button" @click="detailOpen = false">关闭</button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="assignOpen"
      :title="assignKind === 'DATA' ? '数据角色' : '功能角色'"
      width="720px"
      class="org-pick-dialog"
      header-class="org-pick-head"
      :close-on-click-modal="false"
    >
      <div class="org-perm-form">
        <span>已开通APP</span>
        <el-select
          v-model="pickedApps"
          multiple
          collapse-tags
          collapse-tags-tooltip
          placeholder="请选择已开通APP"
          style="width: 100%"
        >
          <el-option v-for="app in board.apps" :key="app.appCode" :label="app.appName" :value="app.appCode" />
        </el-select>
      </div>
      <div v-for="app in pickedCatalogs" :key="app.appCode" class="org-role-box">
        <div class="org-role-box-h">{{ app.appName }}</div>
        <label v-for="name in rolesOf(app)" :key="name" class="org-role-check">
          <el-checkbox :model-value="isOn(app.appCode, name)" @change="(on) => toggle(app.appCode, name, on)" />
          {{ name }}
        </label>
        <div v-if="!rolesOf(app).length" class="org-muted">该APP暂无{{ assignKind === 'DATA' ? '数据' : '功能' }}角色</div>
      </div>
      <template #footer>
        <button class="org-gold lg" type="button" :disabled="saving" @click="saveAssign">确定</button>
        <button class="org-btn-dark lg" type="button" @click="assignOpen = false">关闭</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import OrgPager from '../../components/OrgPager.vue'

const emptySlice = { total: 0, pageNo: 1, pageSize: 5, records: [] }
const board = ref({ apps: [], slice: emptySlice })
const draftKey = ref('')
const appliedKey = ref('')
const detailOpen = ref(false)
const assignOpen = ref(false)
const saving = ref(false)
const detail = ref({ grants: [] })
const assignKind = ref('FUNC')
const assignUserId = ref('')
const pickedApps = ref([])
const pickedRoles = ref({})
const slice = computed(() => board.value.slice || emptySlice)
const pickedCatalogs = computed(() => (board.value.apps || []).filter((app) => pickedApps.value.includes(app.appCode)))

function splitNames(text) {
  if (!text) return []
  return text.split(/[、,，]/).map((item) => item.trim()).filter(Boolean)
}

function rolesOf(app) {
  return assignKind.value === 'DATA' ? (app.dataRoles || []) : (app.funcRoles || [])
}

function isOn(appCode, name) {
  return (pickedRoles.value[appCode] || []).includes(name)
}

function toggle(appCode, name, on) {
  const current = new Set(pickedRoles.value[appCode] || [])
  if (on) current.add(name)
  else current.delete(name)
  pickedRoles.value = { ...pickedRoles.value, [appCode]: [...current] }
}

async function load(page = 1) {
  board.value = await http.get('/console/perm/auth', {
    params: { keyword: appliedKey.value || undefined, pageNo: page, pageSize: 5 }
  })
}

function query() {
  appliedKey.value = draftKey.value.trim()
  load(1)
}

async function openDetail(row) {
  detail.value = await http.get('/console/perm/auth/detail', { params: { userId: row.userId } })
  detailOpen.value = true
}

async function openAssign(row, kind) {
  const data = await http.get('/console/perm/auth/detail', { params: { userId: row.userId } })
  assignKind.value = kind
  assignUserId.value = row.userId
  const grants = data.grants || []
  pickedApps.value = grants.map((item) => item.appCode)
  const map = {}
  for (const grant of grants) {
    map[grant.appCode] = splitNames(kind === 'DATA' ? grant.dataRoleName : grant.funcRoleName)
  }
  pickedRoles.value = map
  assignOpen.value = true
}

async function saveAssign() {
  saving.value = true
  try {
    board.value = await http.post('/console/perm/auth', {
      userId: assignUserId.value,
      kind: assignKind.value,
      items: pickedApps.value.map((appCode) => ({
        appCode,
        roleNames: pickedRoles.value[appCode] || []
      }))
    })
    assignOpen.value = false
    ElMessage.success('已保存')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
