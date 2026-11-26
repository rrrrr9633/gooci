<template>
  <div class="ops-page">
    <h2>{{ title }}</h2>
    <section v-if="deploymentAvailable" class="ops-toolbar">
      <el-form @submit.prevent="saveDeployment">
        <el-form-item label="客户云服务器">
          <el-input v-model="publicOrigin" :disabled="!deploymentLoaded || savingDeployment"
            placeholder="https://公网 IP 或域名[:端口]" />
          <el-button native-type="submit" type="primary" :loading="savingDeployment"
            :disabled="!deploymentLoaded || !publicOrigin.trim()">保存部署地址</el-button>
        </el-form-item>
        <p class="ops-lead">仅谷器运营人员可配置。使用允许名单内的公网 HTTPS 根地址；同一公司的账号共用此部署。</p>
        <p v-if="deploymentLoaded">{{ savedOrigin ? `已登记：${savedOrigin}` : '尚未登记' }}</p>
        <p v-else-if="deploymentLoadFailed">部署地址读取失败，请刷新后重试。</p>
      </el-form>
    </section>
    <p class="ops-lead">
      下方是当前环境的模块开通和目标配置，不会同步到其他环境。普通模块可使用此处的公网目标；AI 系列当前使用员工账号绑定的入口。探活「可达」只表示工作台服务能连到目标，不代表员工浏览器可访问。
    </p>
    <div class="ops-toolbar">
      <el-button type="primary" @click="openImplant">植入模块</el-button>
      <el-button :disabled="!selectedRows.length" @click="openBatchTarget">批量填入公网地址</el-button>
    </div>
    <el-table :key="route.params.id" :data="rows" row-key="appCode" stripe @selection-change="selectedRows = $event">
      <el-table-column type="selection" width="48" reserve-selection />
      <el-table-column prop="appName" label="模块" min-width="160" />
      <el-table-column prop="appCode" label="代码" width="140" />
      <el-table-column label="入口" min-width="160" prop="entryPath" />
      <el-table-column label="目标" min-width="240">
        <template #default="{ row }">
          <el-input v-model="row.targetUrl" size="small" placeholder="https://模块公网主机" />
        </template>
      </el-table-column>
      <el-table-column label="探活" width="120">
        <template #default="{ row }">
          <el-tooltip :content="row.healthHint || healthText(row.health)" placement="top">
            <span>{{ healthText(row.health) }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="对客" width="90">
        <template #default="{ row }">
          <el-switch v-model="row.hideFlag" active-value="0" inactive-value="1" />
        </template>
      </el-table-column>
      <el-table-column width="100">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="save(row)">保存</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog
      v-model="implantOpen"
      title="植入模块"
      width="480px"
      class="org-pick-dialog"
      header-class="org-pick-head"
      :close-on-click-modal="false"
    >
      <el-form label-width="92px">
        <el-form-item label="模块代码">
          <el-input v-model="form.appCode" placeholder="例如 EXT_PDM" />
        </el-form-item>
        <el-form-item label="模块名称">
          <el-input v-model="form.appName" placeholder="对客显示名" />
        </el-form-item>
        <el-form-item label="公网目标">
          <el-input v-model="form.targetUrl" placeholder="https://模块公网主机" />
        </el-form-item>
        <el-form-item label="入口路径">
          <el-input v-model="form.entryPath" placeholder="空则 /workbench/ext/代码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="implantOpen = false">取消</el-button>
        <el-button type="primary" @click="implant">写入开通</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="batchTargetOpen" title="批量填入公网地址" width="min(420px, 92vw)">
      <el-form label-width="84px" @submit.prevent="applyBatchTarget">
        <el-form-item label="公网地址">
          <el-input v-model.trim="batchHost" placeholder="公网 IP 或域名，不带端口" />
        </el-form-item>
        <p class="ops-lead">将替换已选 {{ selectedRows.length }} 行的目标地址，不保存。需要时可逐行补端口。</p>
        <el-button @click="batchTargetOpen = false">取消</el-button>
        <el-button type="primary" native-type="submit" :disabled="!selectedRows.length">批量填入</el-button>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import opsHttp from '../../api/opsHttp'

const route = useRoute()
const rows = ref([])
const enters = ref([])
const deploymentAvailable = ref(false)
const deploymentLoaded = ref(false)
const deploymentLoadFailed = ref(false)
const savingDeployment = ref(false)
const publicOrigin = ref('')
const savedOrigin = ref('')
const implantOpen = ref(false)
const selectedRows = ref([])
const batchTargetOpen = ref(false)
const batchHost = ref('')

function openBatchTarget() {
  batchHost.value = ''
  batchTargetOpen.value = true
}

function applyBatchTarget() {
  const host = batchHost.value.trim().replace(/^https:\/\//i, '').replace(/\/$/, '')
  const labels = host.split('.')
  if (host.length > 253 || labels.length < 2 || !labels.every((label) => /^[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$/i.test(label))) {
    ElMessage.warning('请输入公网 IPv4 或域名，不带端口或路径')
    return
  }
  let target
  try {
    target = new URL(`https://${host}`)
    if (target.hostname !== host.toLowerCase()) throw new Error('Invalid host')
  } catch {
    ElMessage.warning('请输入有效的公网 IPv4 或域名')
    return
  }
  const selected = new Set(selectedRows.value.map((row) => row.appCode))
  for (const row of rows.value) {
    if (selected.has(row.appCode)) row.targetUrl = target.origin
  }
  batchTargetOpen.value = false
}
const form = reactive({
  appCode: '',
  appName: '',
  targetUrl: '',
  entryPath: ''
})
const title = computed(() => {
  const hit = enters.value.find((item) => item.id === route.params.id)
  return hit ? `${hit.enterName} · 已植入模块` : '已植入模块'
})

onMounted(async () => {
  enters.value = await opsHttp.get('/enters')
})

watch(() => route.params.id, async (enterId, _, onCleanup) => {
  let current = true
  onCleanup(() => { current = false })
  rows.value = []
  selectedRows.value = []
  batchTargetOpen.value = false
  implantOpen.value = false
  if (!enterId) return
  try {
    const loaded = await opsHttp.get('/apps', { params: { enterId } })
    if (current) rows.value = loaded
  } catch {
    // The operations client reports the request failure.
  }
}, { immediate: true })

watch(() => route.params.id, async (enterId, _, onCleanup) => {
  let current = true
  onCleanup(() => { current = false })
  deploymentAvailable.value = false
  deploymentLoaded.value = false
  deploymentLoadFailed.value = false
  savingDeployment.value = false
  publicOrigin.value = ''
  savedOrigin.value = ''
  if (!enterId) return
  try {
    const available = await opsHttp.get('/deployments/availability')
    if (!current || !available) return
    deploymentAvailable.value = true
    const deployment = await opsHttp.get(`/deployments/${encodeURIComponent(enterId)}`)
    if (!current) return
    publicOrigin.value = savedOrigin.value = deployment?.publicOrigin ?? ''
    deploymentLoaded.value = true
  } catch {
    if (current) deploymentLoadFailed.value = true
  }
}, { immediate: true })

async function saveDeployment() {
  if (!deploymentLoaded.value || savingDeployment.value || !publicOrigin.value.trim()) return
  const enterId = route.params.id
  savingDeployment.value = true
  try {
    const deployment = await opsHttp.put(`/deployments/${encodeURIComponent(enterId)}`, {
      publicOrigin: publicOrigin.value.trim()
    })
    if (route.params.id !== enterId) return
    publicOrigin.value = savedOrigin.value = deployment.publicOrigin
    ElMessage.success('客户云服务器地址已保存')
  } catch {
    // The operations client reports the request failure.
  } finally {
    if (route.params.id === enterId) savingDeployment.value = false
  }
}

function healthText(code) {
  if (code === 'up') return '可达'
  if (code === 'down') return '未达'
  return '未配'
}

function openImplant() {
  form.appCode = ''
  form.appName = ''
  form.targetUrl = ''
  form.entryPath = ''
  implantOpen.value = true
}

async function save(row) {
  const enterId = route.params.id
  const targetUrl = row.targetUrl
  const hideFlag = row.hideFlag
  const savedRows = await opsHttp.post('/apps', {
    enterId,
    appCode: row.appCode,
    hideFlag,
    targetUrl
  })
  if (route.params.id !== enterId || !rows.value.includes(row)) return
  const saved = savedRows.find((item) => item.appCode === row.appCode)
  if (saved) {
    if (row.targetUrl === targetUrl) row.targetUrl = saved.targetUrl
    if (row.hideFlag === hideFlag) row.hideFlag = saved.hideFlag
    row.health = saved.health
    row.healthHint = saved.healthHint
  }
  ElMessage.success('已保存')
}

async function implant() {
  const enterId = route.params.id
  const updated = await opsHttp.post('/apps/implant', {
    enterId,
    appCode: form.appCode,
    appName: form.appName,
    targetUrl: form.targetUrl,
    entryPath: form.entryPath
  })
  if (route.params.id !== enterId) return
  rows.value = updated
  implantOpen.value = false
  ElMessage.success('已写入开通记录')
}
</script>
