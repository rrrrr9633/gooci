<template>
  <div class="org-page">
    <div class="org-staff-bar">
      <button class="org-gold" type="button" :disabled="saving" @click="syncBoard">同步</button>
      <el-popover placement="bottom-end" :width="180" trigger="click">
        <div class="org-col-pop">
          <label v-for="col in board.columns" :key="col.colKey">
            <el-checkbox :model-value="col.visibleFlag === '1'" @change="(on) => toggleCol(col, on)" />
            {{ col.colName }}
          </label>
        </div>
        <template #reference>
          <button class="org-gold" type="button">列配置</button>
        </template>
      </el-popover>
      <button class="org-gold" type="button" @click="openFields">自定义字段设置</button>
      <button class="org-gold" type="button" @click="openImport">导入</button>
      <button class="org-gold" type="button" @click="openCreate">新增</button>
    </div>
    <div class="org-rule" />
    <el-table
      class="org-staff-table"
      :data="slice.records"
      stripe
      empty-text="暂无设备"
      @sort-change="onSort"
    >
      <el-table-column v-if="shown('deviceCode')" prop="deviceCode" label="设备编号" min-width="150" sortable="custom">
        <template #header><HeadFilter label="设备编号" col="deviceCode" type="text" :query="query" @apply="applyFilter" /></template>
      </el-table-column>
      <el-table-column v-if="shown('deviceSn')" prop="deviceSn" label="设备序列号" min-width="150" sortable="custom">
        <template #header><HeadFilter label="设备序列号" col="deviceSn" type="text" :query="query" @apply="applyFilter" /></template>
      </el-table-column>
      <el-table-column v-if="shown('deviceAlias')" prop="deviceAlias" label="设备别名" min-width="110">
        <template #header><HeadFilter label="设备别名" col="deviceAlias" type="text" :query="query" @apply="applyFilter" /></template>
      </el-table-column>
      <el-table-column v-if="shown('deviceType')" prop="deviceType" label="设备类型" min-width="120" sortable="custom">
        <template #header>
          <HeadFilter label="设备类型" col="deviceType" type="text" :query="query" @apply="applyFilter" />
        </template>
      </el-table-column>
      <el-table-column v-if="shown('makerName')" prop="makerName" label="系统制造商" min-width="120" sortable="custom">
        <template #header><HeadFilter label="系统制造商" col="makerName" type="text" :query="query" @apply="applyFilter" /></template>
      </el-table-column>
      <el-table-column v-if="shown('brandName')" prop="brandName" label="系统品牌" min-width="120" sortable="custom">
        <template #header>
          <HeadFilter label="系统品牌" col="brandName" type="text" placeholder="请输入系统品牌" :query="query" @apply="applyFilter" />
        </template>
      </el-table-column>
      <el-table-column v-if="shown('modelName')" prop="modelName" label="型号" min-width="100" sortable="custom">
        <template #header><HeadFilter label="型号" col="modelName" type="text" :query="query" @apply="applyFilter" /></template>
      </el-table-column>
      <el-table-column v-if="shown('installPlace')" prop="installPlace" label="安装位置" min-width="120" sortable="custom">
        <template #header><HeadFilter label="安装位置" col="installPlace" type="text" :query="query" @apply="applyFilter" /></template>
      </el-table-column>
      <el-table-column v-if="shown('buyDate')" prop="buyDate" label="购置日期" min-width="120" sortable="custom" />
      <el-table-column v-if="shown('deptName')" prop="deptName" label="使用部门" min-width="120" sortable="custom">
        <template #header>
          <HeadFilter
            label="使用部门"
            col="deptIds"
            type="check"
            :options="deptOptions"
            :query="query"
            @apply="applyFilter"
          />
        </template>
      </el-table-column>
      <el-table-column v-if="shown('onlineText')" prop="onlineText" label="是否联网" min-width="110" sortable="custom">
        <template #header>
          <HeadFilter
            label="是否联网"
            col="onlineFlag"
            type="check"
            :options="onlineOptions"
            :query="query"
            @apply="applyFilter"
          />
        </template>
      </el-table-column>
      <el-table-column v-if="shown('deviceStatus')" prop="deviceStatus" label="设备状态" min-width="110" sortable="custom" />
      <el-table-column v-if="shown('deviceLevel')" prop="deviceLevel" label="设备等级" min-width="110" sortable="custom">
        <template #header>
          <HeadFilter
            label="设备等级"
            col="deviceLevel"
            type="check"
            :options="levelOptions"
            :query="query"
            @apply="applyFilter"
          />
        </template>
      </el-table-column>
      <el-table-column v-if="shown('ownerName')" prop="ownerName" label="包机人" min-width="100" />
      <el-table-column v-if="shown('remarkText')" prop="remarkText" label="备注" min-width="120" />
      <el-table-column v-if="shown('crtTime')" prop="crtTime" label="创建时间" min-width="160" sortable="custom" />
      <el-table-column v-for="field in extraCols" :key="field.colKey" :label="field.colName" min-width="120">
        <template #default="{ row }">{{ extraVal(row, field.colKey) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <button class="org-link" type="button" @click="openEdit(row)">编辑</button>
          <button class="org-link danger" type="button" @click="askRemove(row)">删除</button>
        </template>
      </el-table-column>
    </el-table>
    <div class="org-device-pager">
      <el-pagination
        v-model:current-page="pageNo"
        v-model:page-size="pageSize"
        :total="slice.total || 0"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="load"
        @size-change="() => load(1)"
      />
    </div>

    <el-dialog v-model="fieldOpen" title="自定义字段设置" width="760px" class="org-pick-dialog" header-class="org-pick-head" :close-on-click-modal="false">
      <div class="org-field-split">
        <aside class="org-field-side">
          <div class="tools">
            <button class="org-gold" type="button" @click="newField">新增</button>
            <button class="org-gold ghost" type="button" :disabled="!fieldForm.fieldId" @click="removeField">删除</button>
          </div>
          <div
            v-for="item in board.fields"
            :key="item.fieldId"
            class="org-field-item"
            :class="{ on: item.fieldId === fieldForm.fieldId }"
            @click="pickField(item)"
          >{{ item.fieldName }}</div>
        </aside>
        <div class="org-field-main">
          <h4>字段信息</h4>
          <div class="org-field-row">
            <span><i style="color:#e25c5c">*</i> 字段名称</span>
            <el-input v-model="fieldForm.fieldName" />
            <span><i style="color:#e25c5c">*</i> 字段类型</span>
            <el-select v-model="fieldForm.fieldType" placeholder="全部" style="width:100%">
              <el-option label="文本" value="TEXT" />
              <el-option label="数字" value="NUMBER" />
              <el-option label="日期" value="DATE" />
            </el-select>
          </div>
          <div class="org-field-row">
            <span>字段属性</span>
            <label class="span2"><el-checkbox v-model="fieldRequired" /> 是否必填</label>
          </div>
          <div class="org-field-row">
            <span>引导文字</span>
            <el-input class="span2" v-model="fieldForm.hintText" />
          </div>
        </div>
      </div>
      <template #footer>
        <button class="org-gold lg" type="button" :disabled="saving" @click="saveField">保存</button>
        <button class="org-btn-dark" type="button" @click="fieldOpen = false">返回</button>
      </template>
    </el-dialog>

    <el-dialog v-model="importOpen" title="导入设备" width="640px" class="org-pick-dialog" header-class="org-pick-head" :close-on-click-modal="false">
      <div class="org-steps">
        <span :class="{ on: importStep >= 1 }">1.选择文件</span>
        <span :class="{ on: importStep >= 2 }">2.导入向导</span>
        <span :class="{ on: importStep >= 3 }">3.导入完成</span>
      </div>
      <div v-if="importStep === 1">
        <div class="org-field-row" style="grid-template-columns:88px 1fr">
          <span><i style="color:#e25c5c">*</i> 导入模式</span>
          <el-select v-model="importMode" placeholder="请选择" style="width:100%">
            <el-option label="仅新增" value="CREATE" />
            <el-option label="新增与更新" value="UPSERT" />
          </el-select>
        </div>
        <div class="org-import-lead">
          操作步骤：<br>
          1.下载 <a @click="downloadTemplate">《设备导入模板》</a><br>
          2.打开下载表，将对应信息填入或粘贴进本表。为保证导入成功，请使用纯文本或数字。<br>
          3.信息输入完毕并保存后，点击下方“上传文件”按钮选择已保存的 excel 文档。
        </div>
        <label class="org-upload">
          选择上传文件
          <input type="file" accept=".xlsx" class="hidden" @change="onPickFile" />
        </label>
        <div v-if="importFile" class="org-upload-file">
          {{ importFile.name }}
          <i>✓</i>
        </div>
      </div>
      <div v-else-if="importStep === 2">
        <h4 style="margin:0 0 12px;color:var(--gooci-primary);border-left:3px solid var(--gooci-primary);padding-left:8px">映射导入字段</h4>
        <table class="org-map-table">
          <thead><tr><th>设备表字段</th><th>文本字段</th></tr></thead>
          <tbody>
            <tr v-for="item in mapRows" :key="item.key">
              <td>{{ item.required ? '* ' : '' }}{{ item.label }}</td>
              <td>
                <el-select v-model="mapping[item.key]" style="width:100%">
                  <el-option v-for="head in importHeaders" :key="head" :label="head" :value="head" />
                </el-select>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="org-progress">
        <p>导入进度</p>
        <div class="bar"><i :style="{ width: importPct + '%' }" /></div>
        <p>{{ importPct }}% {{ importResultText }}</p>
      </div>
      <template #footer>
        <button v-if="importStep === 2" class="org-btn-dark" type="button" @click="importStep = 1">上一步</button>
        <button v-if="importStep === 1" class="org-gold" type="button" :disabled="!importMode || !importFile" @click="toMap">下一步</button>
        <button v-if="importStep === 2" class="org-gold" type="button" :disabled="saving" @click="runImport">确认</button>
        <button class="org-btn-dark" type="button" @click="importOpen = false">{{ importStep === 3 ? '关闭' : '取消' }}</button>
      </template>
    </el-dialog>

    <el-dialog v-model="formOpen" :title="form.id ? '编辑设备' : '新增设备'" width="760px" class="org-pick-dialog" header-class="org-pick-head" :close-on-click-modal="false">
      <el-form class="org-device-form" label-width="96px">
        <el-form-item required label="设备编号">
          <el-input v-model="form.deviceCode" disabled />
        </el-form-item>
        <el-form-item label="设备序列号">
          <el-input v-model="form.deviceSn" placeholder="请输入设备序列号" />
        </el-form-item>
        <el-form-item required label="设备别名">
          <el-input v-model="form.deviceAlias" placeholder="请输入设备别名" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="form.deviceType" filterable allow-create placeholder="请输入设备类型" style="width:100%">
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="系统制造商">
          <el-input v-model="form.makerName" placeholder="请输入系统制造商" />
        </el-form-item>
        <el-form-item label="系统品牌">
          <el-input v-model="form.brandName" placeholder="请输入系统品牌" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="form.modelName" placeholder="请输入型号" />
        </el-form-item>
        <el-form-item label="安装位置">
          <el-input v-model="form.installPlace" placeholder="请输入安装位置" />
        </el-form-item>
        <el-form-item label="购置日期">
          <el-date-picker v-model="form.buyDate" type="date" value-format="YYYY-MM-DD" placeholder="请输入购置日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="使用部门">
          <el-select v-model="form.deptId" placeholder="请选择部门" clearable style="width:100%">
            <el-option label="全部" value="" />
            <el-option v-for="dept in board.depts" :key="dept.id" :label="dept.deptName" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备状态">
          <el-select v-model="form.deviceStatus" placeholder="请输入设备状态" clearable style="width:100%">
            <el-option v-for="item in statusList" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备等级">
          <el-select v-model="form.deviceLevel" placeholder="请选择设备等级" clearable style="width:100%">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="包机人">
          <el-select v-model="form.ownerUserId" filterable placeholder="全部" clearable style="width:100%">
            <el-option label="全部" value="" />
            <el-option v-for="item in board.owners" :key="item.userId" :label="item.displayName" :value="item.userId" />
          </el-select>
        </el-form-item>
        <el-form-item class="span2" label="备注">
          <el-input v-model="form.remarkText" type="textarea" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item v-for="field in board.fields" :key="field.fieldId" class="span2" :required="field.requiredFlag === '1'" :label="field.fieldName">
          <el-input v-model="form.extraMap[field.fieldId]" :placeholder="field.hintText || '请输入' + field.fieldName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="org-gold lg" type="button" :disabled="saving" @click="saveDevice">确定</button>
        <button class="org-btn-dark" type="button" @click="formOpen = false">取消</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'
import HeadFilter from './DeviceHeadFilter.vue'

const emptySlice = { total: 0, pageNo: 1, pageSize: 10, records: [] }
const board = ref({ slice: emptySlice, columns: [], fields: [], depts: [], owners: [], deviceTypes: [], nextCode: '' })
const saving = ref(false)
const fieldOpen = ref(false)
const importOpen = ref(false)
const formOpen = ref(false)
const pageNo = ref(1)
const pageSize = ref(10)
const query = reactive({
  deviceCode: '',
  deviceSn: '',
  deviceAlias: '',
  deviceType: '',
  makerName: '',
  brandName: '',
  modelName: '',
  installPlace: '',
  deptIds: '',
  onlineFlag: '',
  deviceLevel: '',
  sortKey: '',
  sortDir: ''
})
const fieldForm = reactive({ fieldId: '', fieldName: '', fieldType: 'TEXT', requiredFlag: '0', hintText: '' })
const fieldRequired = computed({
  get: () => fieldForm.requiredFlag === '1',
  set: (on) => { fieldForm.requiredFlag = on ? '1' : '0' }
})
const form = reactive({
  id: '',
  deviceCode: '',
  deviceSn: '',
  deviceAlias: '',
  deviceType: '',
  makerName: '',
  brandName: '',
  modelName: '',
  installPlace: '',
  buyDate: '',
  deptId: '',
  deviceStatus: '',
  deviceLevel: '',
  ownerUserId: '',
  remarkText: '',
  extraMap: {}
})
const importStep = ref(1)
const importMode = ref('CREATE')
const importFile = ref(null)
const importHeaders = ref([])
const mapping = reactive({})
const importPct = ref(0)
const importResultText = ref('')
const statusList = ['正常使用', '限时使用', '维修中', '调试中', '闲置']
const builtinKeys = [
  'deviceCode', 'deviceSn', 'deviceAlias', 'deviceType', 'makerName', 'brandName', 'modelName',
  'installPlace', 'buyDate', 'deptName', 'onlineText', 'deviceStatus', 'deviceLevel', 'ownerName', 'remarkText', 'crtTime'
]
const slice = computed(() => board.value.slice || emptySlice)
const extraCols = computed(() => (board.value.columns || []).filter((col) => col.visibleFlag === '1' && !builtinKeys.includes(col.colKey)))
const deptOptions = computed(() => (board.value.depts || []).map((item) => ({ label: item.deptName, value: item.id })))
const onlineOptions = [{ label: '是', value: '1' }, { label: '否', value: '0' }]
const levelOptions = [{ label: 'A', value: 'A' }, { label: 'B', value: 'B' }, { label: 'C', value: 'C' }]
const typeOptions = computed(() => {
  const set = new Set(['数控车床', '加工中心', ...(board.value.deviceTypes || [])])
  return [...set]
})
const mapRows = computed(() => {
  const rows = [
    { key: 'deviceCode', label: '设备编号' },
    { key: 'deviceSn', label: '设备序列号' },
    { key: 'deviceAlias', label: '设备别名', required: true },
    { key: 'deviceType', label: '设备类型' },
    { key: 'makerName', label: '系统制造商' },
    { key: 'brandName', label: '系统品牌' },
    { key: 'modelName', label: '型号' },
    { key: 'installPlace', label: '安装位置' },
    { key: 'buyDate', label: '购置日期' },
    { key: 'deptName', label: '使用部门' },
    { key: 'deviceStatus', label: '设备状态' },
    { key: 'deviceLevel', label: '设备等级' },
    { key: 'ownerMobile', label: '包机人手机号' },
    { key: 'remarkText', label: '备注' }
  ]
  for (const field of board.value.fields || []) {
    rows.push({ key: field.fieldId, label: field.fieldName, required: field.requiredFlag === '1' })
  }
  return rows
})

function shown(key) {
  const col = (board.value.columns || []).find((item) => item.colKey === key)
  return !col || col.visibleFlag === '1'
}

function extraVal(row, fieldId) {
  const hit = (row.extras || []).find((item) => item.fieldId === fieldId)
  return hit?.fieldValue || ''
}

function paramsOf(page = pageNo.value) {
  const params = { pageNo: page, pageSize: pageSize.value }
  for (const [key, value] of Object.entries(query)) {
    if (value) params[key] = value
  }
  return params
}

async function load(page = pageNo.value) {
  pageNo.value = page
  board.value = await http.get('/console/devices', { params: paramsOf(page) })
}

async function syncBoard() {
  saving.value = true
  try {
    board.value = await http.post('/console/devices/sync', null, { params: paramsOf(pageNo.value) })
    ElMessage.success('同步成功!')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

function applyFilter(next) {
  Object.assign(query, next)
  load(1)
}

function onSort({ prop, order }) {
  query.sortKey = prop || ''
  query.sortDir = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  load(1)
}

async function toggleCol(col, on) {
  col.visibleFlag = on ? '1' : '0'
  const keys = (board.value.columns || []).filter((item) => item.visibleFlag === '1').map((item) => item.colKey)
  try {
    board.value = await http.post('/console/devices/columns', { keys })
    await load(pageNo.value)
  } catch {
    /* http 已提示 */
  }
}

function openFields() {
  fieldForm.fieldId = ''
  fieldForm.fieldName = ''
  fieldForm.fieldType = 'TEXT'
  fieldForm.requiredFlag = '0'
  fieldForm.hintText = ''
  fieldOpen.value = true
}

function newField() {
  fieldForm.fieldId = ''
  fieldForm.fieldName = ''
  fieldForm.fieldType = 'TEXT'
  fieldForm.requiredFlag = '0'
  fieldForm.hintText = ''
}

function pickField(item) {
  fieldForm.fieldId = item.fieldId
  fieldForm.fieldName = item.fieldName
  fieldForm.fieldType = item.fieldType || 'TEXT'
  fieldForm.requiredFlag = item.requiredFlag || '0'
  fieldForm.hintText = item.hintText || ''
}

async function saveField() {
  saving.value = true
  try {
    board.value = await http.post('/console/devices/fields', { ...fieldForm })
    const id = fieldForm.fieldId
    const hit = id
      ? (board.value.fields || []).find((item) => item.fieldId === id)
      : (board.value.fields || [])[(board.value.fields || []).length - 1]
    if (hit) pickField(hit)
    ElMessage.success('已保存')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

async function removeField() {
  if (!fieldForm.fieldId) return
  saving.value = true
  try {
    board.value = await http.post('/console/devices/fields/remove', { fieldId: fieldForm.fieldId })
    newField()
    ElMessage.success('已删除')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

function blankForm() {
  form.id = ''
  form.deviceCode = board.value.nextCode || ''
  form.deviceSn = ''
  form.deviceAlias = ''
  form.deviceType = ''
  form.makerName = ''
  form.brandName = ''
  form.modelName = ''
  form.installPlace = ''
  form.buyDate = ''
  form.deptId = ''
  form.deviceStatus = ''
  form.deviceLevel = ''
  form.ownerUserId = ''
  form.remarkText = ''
  form.extraMap = {}
}

function openCreate() {
  blankForm()
  formOpen.value = true
}

function openEdit(row) {
  form.id = row.id
  form.deviceCode = row.deviceCode
  form.deviceSn = row.deviceSn
  form.deviceAlias = row.deviceAlias
  form.deviceType = row.deviceType
  form.makerName = row.makerName
  form.brandName = row.brandName
  form.modelName = row.modelName
  form.installPlace = row.installPlace
  form.buyDate = row.buyDate
  form.deptId = row.deptId || ''
  form.deviceStatus = row.deviceStatus
  form.deviceLevel = row.deviceLevel
  form.ownerUserId = row.ownerUserId || ''
  form.remarkText = row.remarkText
  const extraMap = {}
  for (const extra of row.extras || []) extraMap[extra.fieldId] = extra.fieldValue
  form.extraMap = extraMap
  formOpen.value = true
}

async function saveDevice() {
  saving.value = true
  try {
    const extras = (board.value.fields || []).map((field) => ({
      fieldId: field.fieldId,
      fieldValue: form.extraMap[field.fieldId] || ''
    }))
    board.value = await http.post('/console/devices', { ...form, extras })
    formOpen.value = false
    ElMessage.success('已保存')
    await load(pageNo.value)
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

async function askRemove(row) {
  try {
    await ElMessageBox.confirm(`确认删除 ${row.deviceAlias || row.deviceSn}？`, '删除设备', { type: 'warning' })
  } catch {
    return
  }
  try {
    board.value = await http.post('/console/devices/remove', { id: row.id })
    ElMessage.success('已删除')
    await load(pageNo.value)
  } catch {
    /* http 已提示 */
  }
}

function openImport() {
  importStep.value = 1
  importMode.value = 'CREATE'
  importFile.value = null
  importHeaders.value = []
  importPct.value = 0
  importResultText.value = ''
  importOpen.value = true
}

async function downloadTemplate() {
  try {
    const blob = await http.get('/console/devices/template', { responseType: 'blob' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '设备导入模版.xlsx'
    link.click()
    URL.revokeObjectURL(url)
  } catch {
    /* http 已提示 */
  }
}

function onPickFile(ev) {
  const file = ev.target.files?.[0]
  importFile.value = file || null
  ev.target.value = ''
}

async function toMap() {
  if (!importFile.value || !importMode.value) return
  try {
    const body = new FormData()
    body.append('file', importFile.value)
    const data = await http.post('/console/devices/import-headers', body)
    importHeaders.value = data.headers || []
    for (const row of mapRows.value) {
      const hit = importHeaders.value.find((head) => head === row.label)
      mapping[row.key] = hit || importHeaders.value[0] || row.label
    }
    importStep.value = 2
  } catch {
    /* http 已提示 */
  }
}

async function runImport() {
  saving.value = true
  importStep.value = 3
  importPct.value = 8
  try {
    const body = new FormData()
    body.append('file', importFile.value)
    body.append('importMode', importMode.value)
    body.append('mappingJson', JSON.stringify({ ...mapping }))
    const tick = setInterval(() => {
      if (importPct.value < 90) importPct.value += 8
    }, 120)
    const result = await http.post('/console/devices/import', body, { timeout: 60000 })
    clearInterval(tick)
    importPct.value = 100
    importResultText.value = `新增 ${result.createdCount}，更新 ${result.updatedCount}，跳过 ${result.skippedCount}，失败 ${result.failedCount}`
    if (result.messages?.length) ElMessage.warning(result.messages[0])
    await load(1)
  } catch {
    importResultText.value = '导入失败'
  } finally {
    saving.value = false
  }
}

onMounted(() => load(1))
</script>
