<template>
  <div class="org-page">
    <div class="org-intro">
      <ol>
        <li>您可以根据自己的企业结构创建多级组织，新建子账号或关联账号，并使其属于您创建的组织，从而分批及逐人设备进行管理。</li>
        <li>企业管理员可以对部门组织结构进行创建及管理维护。</li>
        <li>构建完成的部门组织结构可供 GOOCI 工业 APP 产品统一调用。</li>
      </ol>
      <OrgSketch top="公司" left="部门" mid="部门" right="部门" />
    </div>
    <div class="org-rule" />
    <div class="org-split">
      <aside class="org-tree">
        <h3>部门结构树</h3>
        <div class="org-tree-tools">
          <button class="org-gold" type="button" @click="openAddDept">新增</button>
          <button class="org-gold ghost" type="button" @click="openFlag">{{ deptOff ? '启用' : '禁用' }}</button>
        </div>
        <div
          v-for="node in board.tree"
          :key="node.id"
          class="org-node"
          :class="{ on: node.id === board.current?.id, off: node.enableFlag === '0' }"
          @click="selectDept(node.id)"
        >
          {{ node.deptName }}{{ node.enableFlag === '0' ? '（已禁用）' : '' }}
        </div>
      </aside>
      <div class="org-main">
        <div class="org-main-h">
          <h3>{{ board.current?.deptName || '部门' }}</h3>
          <div style="display:flex;gap:8px">
            <button class="org-gold" type="button" @click="openAccount">新建账号</button>
            <button class="org-gold" type="button" @click="openEditDept">编辑</button>
          </div>
        </div>
        <div class="org-kv">
          <div><span>部门名称：</span>{{ board.current?.deptName || '—' }}</div>
          <div><span>上级部门：</span>{{ board.parentName || '无上级部门' }}</div>
        </div>
        <div class="org-kv" style="grid-template-columns:1fr">
          <div><span>部门描述：</span>{{ board.current?.deptDesc || '—' }}</div>
        </div>

        <div class="org-section-h">
          设备列表
          <button class="org-gold" type="button" @click="openAssignDevice">分配设备</button>
        </div>
        <el-table :data="devices.records" empty-text="暂无设备">
          <el-table-column prop="deviceSn" label="设备序列号" min-width="160" />
          <el-table-column prop="deviceAlias" label="设备别名" min-width="100" />
          <el-table-column prop="boxCode" label="BOX编号" min-width="140" />
          <el-table-column prop="brandName" label="品牌" width="90" />
          <el-table-column prop="modelName" label="型号" width="90" />
          <el-table-column prop="appOpenCount" label="开通应用数" width="110" />
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <button class="org-link danger" type="button" @click="removeDevice(row)">移除</button>
            </template>
          </el-table-column>
        </el-table>
        <OrgPager :slice="devices" @change="(page) => load(page, members.pageNo)" />

        <div class="org-section-h">
          成员列表
          <button class="org-gold" type="button" @click="openAssignMember">分配成员</button>
        </div>
        <el-table :data="members.records" empty-text="暂无成员">
          <el-table-column prop="mobile" label="手机号" min-width="140" />
          <el-table-column prop="displayName" label="姓名" min-width="120" />
          <el-table-column prop="mailAddr" label="邮箱" min-width="180" />
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <button class="org-link danger" type="button" @click="removeMember(row)">移除</button>
            </template>
          </el-table-column>
        </el-table>
        <OrgPager :slice="members" @change="(page) => load(devices.pageNo, page)" />
      </div>
    </div>

    <el-dialog v-model="deptOpen" :title="deptForm.id ? '编辑部门' : '新增部门'" width="460px" class="org-pick-dialog" header-class="org-pick-head" :close-on-click-modal="false">
      <el-form label-width="88px" class="org-dlg-form">
        <el-form-item label="上级部门">
          <el-select v-if="!deptForm.id" v-model="deptForm.parentId" placeholder="无上级部门" style="width:100%">
            <el-option label="无上级部门" value="" />
            <el-option v-for="node in parentOptions" :key="node.id" :label="node.deptName" :value="node.id" />
          </el-select>
          <span v-else class="org-static">{{ parentLabel(deptForm.parentId) }}</span>
        </el-form-item>
        <el-form-item label="部门名称"><el-input v-model="deptForm.deptName" maxlength="100" /></el-form-item>
        <el-form-item label="部门描述"><el-input v-model="deptForm.deptDesc" type="textarea" :rows="3" maxlength="200" /></el-form-item>
      </el-form>
      <template #footer>
        <button class="org-gold lg" type="button" :disabled="saving" @click="saveDept">确定</button>
        <button class="org-btn-dark" type="button" @click="deptOpen = false">取消</button>
      </template>
    </el-dialog>

    <el-dialog v-model="flagOpen" :title="deptOff ? '启用提示' : '禁用提示'" width="420px" class="org-pick-dialog" header-class="org-pick-head" :close-on-click-modal="false">
      <p class="org-confirm">{{ deptOff ? '确认启用该部门吗？' : '确认禁用该部门吗？' }}</p>
      <template #footer>
        <button class="org-gold lg" type="button" :disabled="saving" @click="confirmFlag">确定</button>
        <button class="org-btn-dark" type="button" @click="flagOpen = false">取消</button>
      </template>
    </el-dialog>

    <el-dialog v-model="accountOpen" title="新建账号" width="460px" class="org-pick-dialog" header-class="org-pick-head" :close-on-click-modal="false">
      <p class="org-dlg-lead">创建新账号到此部门</p>
      <el-form label-width="88px" class="org-dlg-form">
        <el-form-item label="手机号"><el-input v-model="account.mobile" maxlength="11" /></el-form-item>
        <el-form-item label="验证码">
          <div class="org-sms">
            <el-input v-model="account.smsCode" maxlength="6" />
            <button class="org-gold" type="button" :disabled="smsWait > 0" @click="sendSms">{{ smsWait > 0 ? smsWait + 's' : '获取验证码' }}</button>
          </div>
        </el-form-item>
        <el-form-item label="姓名"><el-input v-model="account.displayName" maxlength="100" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="account.mailAddr" maxlength="100" /></el-form-item>
      </el-form>
      <template #footer>
        <button class="org-gold lg" type="button" :disabled="saving" @click="saveAccount">确定</button>
        <button class="org-btn-dark" type="button" @click="accountOpen = false">取消</button>
      </template>
    </el-dialog>

    <el-dialog v-model="deviceOpen" title="分配设备" width="640px" class="org-pick-dialog" header-class="org-pick-head" :close-on-click-modal="false">
      <div class="org-xfer-search">
        <el-input v-model="deviceQuery" type="textarea" :rows="2" placeholder="请输入设备别名,多个设备别名用换行区分" />
        <button class="org-gold" type="button" @click="applyDeviceQuery">查找</button>
      </div>
      <div class="org-xfer">
        <div class="org-xfer-col">
          <div class="org-xfer-h"><label><input type="checkbox" :checked="allPendingOn" @change="togglePendingAll" /> 待分配列表</label><span>{{ pendingPicked.length }}/{{ pendingView.length }}</span></div>
          <label v-for="row in pendingView" :key="row.id" class="org-xfer-row"><input v-model="pendingPicked" type="checkbox" :value="row.id" /> {{ row.deviceAlias || row.deviceSn }}</label>
        </div>
        <div class="org-xfer-mid">
          <button type="button" @click="moveRight">&gt;</button>
          <button type="button" @click="moveLeft">&lt;</button>
        </div>
        <div class="org-xfer-col">
          <div class="org-xfer-h"><label><input type="checkbox" :checked="allAssignedOn" @change="toggleAssignedAll" /> 已分配列表</label><span>{{ assignedPicked.length }}/{{ assignedList.length }}</span></div>
          <label v-for="row in assignedList" :key="row.id" class="org-xfer-row"><input v-model="assignedPicked" type="checkbox" :value="row.id" /> {{ row.deviceAlias || row.deviceSn }}</label>
        </div>
      </div>
      <template #footer>
        <button class="org-gold lg" type="button" :disabled="saving" @click="saveDevices">确定</button>
        <button class="org-btn-dark" type="button" @click="deviceOpen = false">取消</button>
      </template>
    </el-dialog>

    <el-dialog v-model="memberOpen" title="分配成员" width="720px" class="org-pick-dialog" header-class="org-pick-head" :close-on-click-modal="false" @opened="searchStaff">
      <div class="org-pick-bar">
        <span>按部门查询</span>
        <el-select v-model="staffQuery.deptId" placeholder="全部" clearable style="width:140px">
          <el-option label="全部" value="" />
          <el-option v-for="node in board.tree" :key="node.id" :label="node.deptName" :value="node.id" />
        </el-select>
        <el-input v-model="staffQuery.keyword" placeholder="姓名或手机" style="width:180px" @keyup.enter="searchStaff" />
        <button class="org-gold" type="button" @click="searchStaff">查询</button>
      </div>
      <el-table :data="staffSlice.records" empty-text="暂无人员" row-key="userId" @selection-change="onStaffPick">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="mobile" label="手机号" min-width="140" />
        <el-table-column prop="displayName" label="姓名" min-width="120" />
        <el-table-column prop="deptName" label="部门" min-width="140" />
      </el-table>
      <OrgPager :slice="staffSlice" @change="(page) => loadStaff(page)" />
      <template #footer>
        <button class="org-gold lg" type="button" :disabled="saving" @click="saveMembers">确定</button>
        <button class="org-btn-dark" type="button" @click="memberOpen = false">取消</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import OrgSketch from '../../components/OrgSketch.vue'
import OrgPager from '../../components/OrgPager.vue'

const emptySlice = { total: 0, pageNo: 1, pageSize: 5, records: [] }
const board = ref({ tree: [], current: null, parentName: '', devices: emptySlice, members: emptySlice })
const deptOpen = ref(false)
const flagOpen = ref(false)
const accountOpen = ref(false)
const deviceOpen = ref(false)
const memberOpen = ref(false)
const saving = ref(false)
const smsWait = ref(0)
let smsTimer
const deptForm = reactive({ id: '', deptName: '', parentId: '', deptDesc: '' })
const account = reactive({ mobile: '', smsCode: '', displayName: '', mailAddr: '' })
const deviceQuery = ref('')
const deviceFilter = ref('')
const pendingList = ref([])
const assignedList = ref([])
const pendingPicked = ref([])
const assignedPicked = ref([])
const staffQuery = reactive({ deptId: '', keyword: '' })
const staffSlice = ref({ ...emptySlice })
const pickedStaff = ref([])

const devices = computed(() => board.value.devices || emptySlice)
const members = computed(() => board.value.members || emptySlice)
const deptOff = computed(() => board.value.current?.enableFlag === '0')
const parentOptions = computed(() =>
  (board.value.tree || []).filter((node) => node.id !== deptForm.id)
)
const pendingView = computed(() => {
  const keys = deviceFilter.value.split(/\s+/).map((item) => item.trim()).filter(Boolean)
  if (!keys.length) return pendingList.value
  return pendingList.value.filter((row) => keys.some((key) => (row.deviceAlias || '').includes(key) || (row.deviceSn || '').includes(key)))
})
const allPendingOn = computed(() => pendingView.value.length > 0 && pendingView.value.every((row) => pendingPicked.value.includes(row.id)))
const allAssignedOn = computed(() => assignedList.value.length > 0 && assignedList.value.every((row) => assignedPicked.value.includes(row.id)))

function parentLabel(parentId) {
  if (!parentId) return '无上级部门'
  const node = (board.value.tree || []).find((item) => item.id === parentId)
  return node?.deptName || '无上级部门'
}

async function load(devicePage = devices.value.pageNo || 1, memberPage = members.value.pageNo || 1) {
  board.value = await http.get('/console/org/depts', {
    params: {
      ...(board.value.current?.id ? { deptId: board.value.current.id } : {}),
      devicePage,
      memberPage,
      pageSize: 5
    }
  })
}

function selectDept(id) {
  board.value.current = { ...(board.value.current || {}), id }
  load(1, 1)
}

function openAddDept() {
  deptForm.id = ''
  deptForm.deptName = ''
  deptForm.parentId = board.value.current?.id || ''
  deptForm.deptDesc = ''
  deptOpen.value = true
}

function openEditDept() {
  const node = board.value.current
  if (!node?.id) return
  deptForm.id = node.id
  deptForm.deptName = node.deptName || ''
  deptForm.parentId = node.parentId || ''
  deptForm.deptDesc = node.deptDesc || ''
  deptOpen.value = true
}

async function saveDept() {
  saving.value = true
  try {
    board.value = await http.post('/console/org/depts', { ...deptForm })
    deptOpen.value = false
    ElMessage.success('已保存')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

function openFlag() {
  if (!board.value.current?.id) return
  flagOpen.value = true
}

async function confirmFlag() {
  if (!board.value.current?.id) return
  const enable = deptOff.value
  saving.value = true
  try {
    board.value = await http.post(enable ? '/console/org/depts/enable' : '/console/org/depts/disable', {
      deptId: board.value.current.id
    })
    flagOpen.value = false
    ElMessage.success(enable ? '已启用' : '已禁用')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

function openAccount() {
  if (!board.value.current?.id) return
  account.mobile = ''
  account.smsCode = ''
  account.displayName = ''
  account.mailAddr = ''
  accountOpen.value = true
}

async function sendSms() {
  try {
    const data = await http.post('/auth/sms', { mobile: account.mobile })
    if (data?.demoCode) ElMessage.success('演示验证码 ' + data.demoCode)
    smsWait.value = 60
    clearInterval(smsTimer)
    smsTimer = setInterval(() => {
      smsWait.value -= 1
      if (smsWait.value <= 0) clearInterval(smsTimer)
    }, 1000)
  } catch {
    /* http 已提示 */
  }
}

async function saveAccount() {
  saving.value = true
  try {
    board.value = await http.post('/console/org/members', {
      ...account,
      deptId: board.value.current?.id
    })
    accountOpen.value = false
    ElMessage.success('已创建')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

async function openAssignDevice() {
  if (!board.value.current?.id) return
  const pool = await http.get('/console/org/devices/pool', { params: { deptId: board.value.current.id } })
  pendingList.value = pool.pending || []
  assignedList.value = pool.assigned || []
  pendingPicked.value = []
  assignedPicked.value = []
  deviceQuery.value = ''
  deviceFilter.value = ''
  deviceOpen.value = true
}

function applyDeviceQuery() {
  deviceFilter.value = deviceQuery.value
}

function togglePendingAll(event) {
  const ids = pendingView.value.map((row) => row.id)
  pendingPicked.value = event.target.checked ? ids : []
}

function toggleAssignedAll(event) {
  const ids = assignedList.value.map((row) => row.id)
  assignedPicked.value = event.target.checked ? ids : []
}

function moveRight() {
  const moving = pendingList.value.filter((row) => pendingPicked.value.includes(row.id))
  pendingList.value = pendingList.value.filter((row) => !pendingPicked.value.includes(row.id))
  assignedList.value = assignedList.value.concat(moving)
  pendingPicked.value = []
}

function moveLeft() {
  const moving = assignedList.value.filter((row) => assignedPicked.value.includes(row.id))
  assignedList.value = assignedList.value.filter((row) => !assignedPicked.value.includes(row.id))
  pendingList.value = pendingList.value.concat(moving)
  assignedPicked.value = []
}

async function saveDevices() {
  saving.value = true
  try {
    board.value = await http.post('/console/org/devices/assign', {
      deptId: board.value.current?.id,
      deviceIds: assignedList.value.map((row) => row.id)
    })
    deviceOpen.value = false
    ElMessage.success('已分配')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

function openAssignMember() {
  if (!board.value.current?.id) return
  staffQuery.deptId = ''
  staffQuery.keyword = ''
  pickedStaff.value = []
  memberOpen.value = true
}

function searchStaff() {
  loadStaff(1)
}

async function loadStaff(pageNo = 1) {
  staffSlice.value = await http.get('/console/org/staff', {
    params: {
      deptId: staffQuery.deptId || undefined,
      keyword: staffQuery.keyword || undefined,
      pageNo,
      pageSize: 5
    }
  })
}

function onStaffPick(rows) {
  pickedStaff.value = rows
}

async function saveMembers() {
  saving.value = true
  try {
    board.value = await http.post('/console/org/members/assign', {
      deptId: board.value.current?.id,
      userIds: pickedStaff.value.map((row) => row.userId)
    })
    memberOpen.value = false
    ElMessage.success('已分配')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

async function removeMember(row) {
  try {
    board.value = await http.post('/console/org/members/remove', {
      userId: row.userId,
      deptId: board.value.current?.id
    })
    ElMessage.success('已移除')
  } catch {
    /* http 已提示 */
  }
}

async function removeDevice(row) {
  try {
    board.value = await http.post('/console/org/devices/remove', { id: row.id })
    ElMessage.success('已移除')
  } catch {
    /* http 已提示 */
  }
}

onMounted(() => load(1, 1))
</script>

