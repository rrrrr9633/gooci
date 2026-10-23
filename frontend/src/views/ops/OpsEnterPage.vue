<template>
  <div class="ops-page">
    <h2>{{ title }}</h2>
    <p class="ops-lead">
      探活是控制台进程去连 TARGET_URL，不是浏览器去连。可达=已连通该内网地址；未达=端口没人听、超时或主机不对。
      生产加模块：先把进程放到这家客户自己的 VPC 模块机，再点「植入」写开通记录。模块不会自己上报。
    </p>
    <div class="ops-toolbar">
      <el-button type="primary" @click="openImplant">植入模块</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="appName" label="模块" min-width="160" />
      <el-table-column prop="appCode" label="代码" width="140" />
      <el-table-column label="入口" min-width="160" prop="entryPath" />
      <el-table-column label="目标" min-width="240">
        <template #default="{ row }">
          <el-input v-model="row.targetUrl" size="small" placeholder="http://内网IP:端口" />
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
        <el-form-item label="内网目标">
          <el-input v-model="form.targetUrl" placeholder="http://10.x.x.x:端口" />
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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import opsHttp from '../../api/opsHttp'

const route = useRoute()
const rows = ref([])
const enters = ref([])
const implantOpen = ref(false)
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
  rows.value = await opsHttp.get('/apps', { params: { enterId: route.params.id } })
})

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
  rows.value = await opsHttp.post('/apps', {
    enterId: route.params.id,
    appCode: row.appCode,
    hideFlag: row.hideFlag,
    targetUrl: row.targetUrl
  })
  ElMessage.success('已保存')
}

async function implant() {
  rows.value = await opsHttp.post('/apps/implant', {
    enterId: route.params.id,
    appCode: form.appCode,
    appName: form.appName,
    targetUrl: form.targetUrl,
    entryPath: form.entryPath
  })
  implantOpen.value = false
  ElMessage.success('已写入开通记录')
}
</script>
