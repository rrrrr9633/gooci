<template>
  <div class="org-page">
    <div class="org-crumb">{{ title }}</div>
    <div class="org-rule" />
    <div class="org-perm-bar">
      <div class="org-perm-left">
        <span>已开通APP</span>
        <el-select v-model="draftApp" clearable placeholder="全部" style="width: 220px">
          <el-option label="全部" value="" />
          <el-option v-for="app in board.apps" :key="app.appCode" :label="app.appName" :value="app.appCode" />
        </el-select>
      </div>
      <div class="org-perm-right">
        <button class="org-gold" type="button" @click="query">查询</button>
        <button class="org-gold" type="button" @click="openCreate">{{ createLabel }}</button>
      </div>
    </div>
    <el-table class="org-staff-table" :data="slice.records" stripe empty-text="暂无角色">
      <el-table-column prop="appName" label="已开通APP" min-width="220" />
      <el-table-column prop="roleName" :label="roleLabel" min-width="220" />
      <el-table-column label="操作" width="120" />
    </el-table>
    <OrgPager :slice="slice" @change="(page) => load(page)" />

    <el-dialog
      v-model="formOpen"
      :title="createLabel"
      width="560px"
      class="org-pick-dialog"
      header-class="org-pick-head"
      :close-on-click-modal="false"
    >
      <el-form label-width="110px" class="org-dlg-form">
        <el-form-item :required="true" :label="nameLabel">
          <el-input v-model="form.roleName" maxlength="100" style="width: 240px" />
        </el-form-item>
        <el-form-item label="已开通APP">
          <el-select v-model="form.appCode" placeholder="全部" style="width: 100%">
            <el-option v-for="app in board.apps" :key="app.appCode" :label="app.appName" :value="app.appCode" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="org-gold lg" type="button" :disabled="saving" @click="save">确定</button>
        <button class="org-btn-dark lg" type="button" @click="formOpen = false">关闭</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import OrgPager from '../../components/OrgPager.vue'

const emptySlice = { total: 0, pageNo: 1, pageSize: 5, records: [] }
const route = useRoute()
const kind = computed(() => route.meta.roleKind || 'FUNC')
const title = computed(() => route.meta.title || '角色')
const roleLabel = computed(() => kind.value === 'DATA' ? '数据角色' : '功能角色')
const createLabel = computed(() => kind.value === 'DATA' ? '创建数据角色' : '创建功能角色')
const nameLabel = computed(() => kind.value === 'DATA' ? '数据角色名称' : '功能角色名称')

const board = ref({ apps: [], slice: emptySlice })
const draftApp = ref('')
const appliedApp = ref('')
const formOpen = ref(false)
const saving = ref(false)
const form = reactive({ appCode: '', roleName: '' })
const slice = computed(() => board.value.slice || emptySlice)

async function load(page = 1) {
  board.value = await http.get('/console/perm/roles', {
    params: { kind: kind.value, appCode: appliedApp.value || undefined, pageNo: page, pageSize: 5 }
  })
}

function query() {
  appliedApp.value = draftApp.value || ''
  load(1)
}

function openCreate() {
  form.appCode = board.value.apps?.[0]?.appCode || ''
  form.roleName = ''
  formOpen.value = true
}

async function save() {
  if (!form.roleName.trim()) {
    ElMessage.error(kind.value === 'DATA' ? '请填写数据角色名称' : '请填写功能角色名称')
    return
  }
  if (!form.appCode) {
    ElMessage.error('请选择已开通APP')
    return
  }
  saving.value = true
  try {
    board.value = await http.post('/console/perm/roles', {
      kind: kind.value,
      appCode: form.appCode,
      roleName: form.roleName.trim()
    })
    formOpen.value = false
    ElMessage.success('已创建')
    appliedApp.value = ''
    draftApp.value = ''
  } finally {
    saving.value = false
  }
}

onMounted(load)
watch(kind, () => {
  draftApp.value = ''
  appliedApp.value = ''
  load(1)
})
</script>
