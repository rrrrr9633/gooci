<template>
  <div class="org-page">
    <div class="org-app-head">
      <div>
        <h3>已开通工业APP</h3>
        <small>业务功能快捷入口</small>
      </div>
      <div v-if="editing" class="org-app-actions">
        <button class="org-gold" type="button" :disabled="saving" @click="saveHide">保存</button>
        <button class="org-gold ghost" type="button" @click="cancelEdit">取消</button>
      </div>
      <button v-else class="org-gold" type="button" @click="startEdit">配置入口</button>
    </div>

    <div v-if="shown.length" class="org-app-grid">
      <div v-for="app in shown" :key="app.appCode" class="org-app-tile">
        <button class="org-app-mark" type="button" :disabled="editing" @click="openApp(app)">
          <AppMark :code="app.appCode" />
        </button>
        <div class="org-app-name">{{ app.appName }}</div>
        <label v-if="editing" class="org-app-hide">
          <input v-model="draft[app.appCode]" type="checkbox" true-value="1" false-value="0">
          隐藏
        </label>
        <button v-else class="org-gold" type="button" @click="openAdmin(app)">配置管理员</button>
      </div>
    </div>
    <div v-else class="org-app-empty">暂无已开通工业APP</div>

    <el-dialog
      v-model="adminOpen"
      title="配置管理员"
      width="760px"
      class="org-pick-dialog"
      header-class="org-pick-head"
      :close-on-click-modal="false"
      @opened="searchStaff"
    >
      <div class="org-pick-bar">
        <span>按部门查询</span>
        <el-select v-model="query.deptId" placeholder="全部" clearable style="width:160px">
          <el-option label="全部" value="" />
          <el-option v-for="dept in depts" :key="dept.id" :label="dept.deptName" :value="dept.id" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="姓名或手机" style="width:180px" @keyup.enter="searchStaff" />
        <button class="org-gold" type="button" @click="searchStaff">查询</button>
      </div>
      <el-table :data="staffSlice.records" empty-text="暂无人员" highlight-current-row @current-change="onPick">
        <el-table-column width="52" align="center">
          <template #default="{ row }">
            <el-radio v-model="pickedId" :value="row.userId">&nbsp;</el-radio>
          </template>
        </el-table-column>
        <el-table-column prop="mobile" label="手机号" min-width="140" />
        <el-table-column prop="displayName" label="姓名" min-width="120" />
        <el-table-column prop="deptName" label="部门" min-width="140" />
      </el-table>
      <OrgPager :slice="staffSlice" @change="(page) => loadStaff(page)" />
      <div class="org-app-role">
        <span class="req">角色</span>
        <el-select v-model="roleName" placeholder="请选择" style="width:220px">
          <el-option v-for="role in roles" :key="role" :label="role" :value="role" />
        </el-select>
      </div>
      <template #footer>
        <button class="org-gold lg" type="button" :disabled="saving" @click="saveAdmin">确定</button>
        <button class="org-btn-dark lg" type="button" @click="adminOpen = false">取消</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import OrgPager from '../../components/OrgPager.vue'
import AppMark from '../../components/AppMark.vue'

const router = useRouter()
const board = ref({ apps: [] })
const editing = ref(false)
const saving = ref(false)
const draft = reactive({})
const adminOpen = ref(false)
const currentApp = ref('')
const pickedId = ref('')
const roleName = ref('')
const roles = ref([])
const depts = ref([])
const query = reactive({ deptId: '', keyword: '' })
const staffSlice = ref({ records: [], total: 0, pageNo: 1, pageSize: 5 })

const shown = computed(() => {
  const apps = board.value.apps || []
  if (editing.value) return apps
  return apps.filter((app) => app.hideFlag !== '1')
})

async function load() {
  board.value = await http.get('/console/biz/apps')
}

function startEdit() {
  Object.keys(draft).forEach((key) => delete draft[key])
  for (const app of board.value.apps || []) {
    draft[app.appCode] = app.hideFlag === '1' ? '1' : '0'
  }
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function saveHide() {
  saving.value = true
  try {
    board.value = await http.post('/console/biz/apps/hide', {
      items: (board.value.apps || []).map((app) => ({
        appCode: app.appCode,
        hideFlag: draft[app.appCode] === '1' ? '1' : '0'
      }))
    })
    editing.value = false
    ElMessage.success('已保存')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

function openApp(app) {
  if (!app.entryPath) return
  router.push(app.entryPath)
}

async function openAdmin(app) {
  currentApp.value = app.appCode
  pickedId.value = ''
  roleName.value = ''
  const roleBoard = await http.get('/console/perm/roles', {
    params: { kind: 'FUNC', appCode: app.appCode, pageNo: 1, pageSize: 50 }
  })
  roles.value = (roleBoard.slice?.records || []).map((row) => row.roleName)
  if (!roles.value.length) {
    ElMessage.warning('该模块尚未配置功能角色')
    return
  }
  roleName.value = roles.value[0]
  adminOpen.value = true
}

function onPick(row) {
  pickedId.value = row ? row.userId : ''
}

async function searchStaff() {
  await loadStaff(1)
}

async function loadStaff(pageNo) {
  const board = await http.get('/console/staff', {
    params: {
      deptId: query.deptId || undefined,
      keyword: query.keyword || undefined,
      pageNo,
      pageSize: 5
    }
  })
  staffSlice.value = board.slice || { records: [], total: 0, pageNo, pageSize: 5 }
}

async function saveAdmin() {
  if (!pickedId.value) {
    ElMessage.warning('请选择人员')
    return
  }
  if (!roleName.value) {
    ElMessage.warning('请选择角色')
    return
  }
  saving.value = true
  try {
    await http.post('/console/biz/apps/admin', {
      appCode: currentApp.value,
      userId: pickedId.value,
      roleName: roleName.value
    })
    adminOpen.value = false
    ElMessage.success('已配置管理员')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await load()
  const deptBoard = await http.get('/console/org/depts')
  depts.value = deptBoard.tree || []
})
</script>
