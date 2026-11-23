<template>
  <div class="org-page">
    <div class="org-intro">
      <div>
        <ol>
          <li>企业组织管理提供企业经营组织管理功能，包括分公司的业务管理能力，以及在企业项目内的设备资源管理、资源迁入迁出、成员管理等。</li>
          <li>账号类型是实名企业的客户可以开通企业集团管理项目。创建企业组织，需要先注册下属企业，然后请联系 GOOCI 技术人员进行企业组织关联。</li>
        </ol>
        <div style="margin-top:16px;display:flex;align-items:center;gap:16px">
          <button class="org-gold" type="button" @click="$router.push('/register?kind=enterprise')">创建企业组织</button>
          <button class="org-link" type="button" @click="hintSupport">联系 GOOCI 技术支持</button>
        </div>
      </div>
      <OrgSketch />
    </div>
    <div class="org-rule" />
    <div class="org-split">
      <aside class="org-tree">
        <h3>企业组织结构树</h3>
        <div class="org-node on">{{ structure.enter?.enterName || '—' }}</div>
      </aside>
      <div class="org-main">
        <div class="org-main-h">
          <h3>企业管理员</h3>
          <button class="org-gold" type="button" @click="openPicker">添加企业管理员</button>
        </div>
        <el-table :data="structure.admins" empty-text="暂无管理员">
          <el-table-column prop="mobile" label="手机号" min-width="140" />
          <el-table-column prop="displayName" label="姓名" min-width="120" />
          <el-table-column prop="mailAddr" label="邮箱" min-width="180" />
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <button class="org-link danger" type="button" @click="removeAdmin(row)">移除</button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top:28px">
          <EnterInfoBlock :enter="structure.enter || {}" />
        </div>
      </div>
    </div>

    <el-dialog
      v-model="openAdd"
      title="添加企业管理员"
      width="720px"
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
      <template #footer>
        <button class="org-gold lg" type="button" :disabled="saving" @click="addAdmin">确定</button>
        <button class="org-btn-dark" type="button" @click="openAdd = false">取消</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'
import EnterInfoBlock from '../../components/EnterInfoBlock.vue'
import OrgSketch from '../../components/OrgSketch.vue'
import OrgPager from '../../components/OrgPager.vue'

const structure = ref({ enter: {}, admins: [] })
const depts = ref([])
const openAdd = ref(false)
const saving = ref(false)
const pickedId = ref('')
const query = reactive({ deptId: '', keyword: '' })
const staffSlice = ref({ records: [], total: 0, pageNo: 1, pageSize: 5 })

async function load() {
  structure.value = await http.get('/console/org/structure')
}

async function loadDepts() {
  const board = await http.get('/console/org/depts')
  depts.value = board.tree || []
}

function hintSupport() {
  ElMessage.info('请通过顶栏客服电话联系 GOOCI 技术支持')
}

function openPicker() {
  pickedId.value = ''
  query.deptId = ''
  query.keyword = ''
  openAdd.value = true
}

function onPick(row) {
  pickedId.value = row ? row.userId : ''
}

async function searchStaff() {
  await loadStaff(1)
}

async function loadStaff(pageNo) {
  staffSlice.value = await http.get('/console/org/staff', {
    params: {
      deptId: query.deptId || undefined,
      keyword: query.keyword || undefined,
      pageNo,
      pageSize: 5
    }
  })
}

async function addAdmin() {
  if (!pickedId.value) {
    ElMessage.warning('请选择人员')
    return
  }
  saving.value = true
  try {
    await http.post('/console/org/admins', { userId: pickedId.value })
    openAdd.value = false
    ElMessage.success('已添加')
    await load()
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

async function removeAdmin(row) {
  try {
    await ElMessageBox.confirm(`移除管理员 ${row.displayName}？`, '提示', { type: 'warning' })
    await http.post('/console/org/admins/remove', { userId: row.userId })
    ElMessage.success('已移除')
    await load()
  } catch {
    /* 取消或失败 */
  }
}

onMounted(async () => {
  await load()
  await loadDepts()
})
</script>
