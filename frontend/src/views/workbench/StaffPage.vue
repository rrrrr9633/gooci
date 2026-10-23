<template>
  <div class="org-page">
    <div class="org-staff-bar">
      <button class="org-gold" type="button" @click="openRule">设置员工编号规则</button>
      <el-popover placement="bottom-end" :width="160" trigger="click">
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
    <el-table class="org-staff-table" :data="slice.records" stripe empty-text="暂无员工">
      <el-table-column v-if="shown('staffCode')" prop="staffCode" label="人员编号" min-width="140" sortable />
      <el-table-column v-if="shown('displayName')" prop="displayName" label="人员名称" min-width="100" sortable />
      <el-table-column v-if="shown('mobile')" prop="mobile" label="手机号码" min-width="120" sortable />
      <el-table-column v-if="shown('deptName')" prop="deptName" label="所属部门" min-width="160" sortable />
      <el-table-column v-if="shown('mailAddr')" prop="mailAddr" label="电子邮箱" min-width="160" sortable />
      <el-table-column v-if="shown('wechatNo')" prop="wechatNo" label="微信号" min-width="120" />
      <el-table-column v-if="shown('dingNo')" prop="dingNo" label="钉钉号" min-width="120" />
      <el-table-column v-if="shown('remarkText')" prop="remarkText" label="备注" min-width="120" sortable />
      <el-table-column v-if="shown('crtTime')" prop="crtTime" label="创建时间" min-width="160" sortable />
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
    <OrgPager :slice="slice" @change="(page) => load(page)" />

    <el-dialog v-model="ruleOpen" title="设置员工编号规则" width="720px" class="org-pick-dialog" header-class="org-pick-head" :close-on-click-modal="false">
      <div class="org-rule-grid">
        <div>
          <div class="org-muted" style="margin-bottom:6px">编码名称</div>
          <el-input v-model="ruleForm.codeName" />
        </div>
        <div>
          <div class="org-muted" style="margin-bottom:6px">编码预览</div>
          <el-input :model-value="rulePreview" disabled />
        </div>
      </div>
      <table class="org-rule-table">
        <thead>
          <tr><th>值类型</th><th>值内容</th><th style="width:220px">操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="(part, idx) in ruleForm.parts" :key="idx">
            <td>
              <el-select v-model="part.valueType" style="width:100%">
                <el-option label="固定字符" value="FIXED" />
                <el-option label="日期" value="DATE" />
                <el-option label="流水号" value="SEQ" />
              </el-select>
            </td>
            <td>
              <el-select v-if="part.valueType === 'DATE'" v-model="part.partValue" style="width:100%">
                <el-option label="YYMMDD" value="YYMMDD" />
                <el-option label="YYYYMMDD" value="YYYYMMDD" />
                <el-option label="YYMM" value="YYMM" />
                <el-option label="YYYYMM" value="YYYYMM" />
                <el-option label="MMDD" value="MMDD" />
              </el-select>
              <el-input v-else v-model="part.partValue" />
            </td>
            <td>
              <button v-if="idx > 0" class="org-gold" type="button" @click="movePart(idx, -1)">上移</button>
              <button v-if="idx < ruleForm.parts.length - 1" class="org-gold" type="button" @click="movePart(idx, 1)">下移</button>
              <button class="org-gold danger" type="button" :disabled="part.valueType === 'SEQ' || ruleForm.parts.length <= 1" @click="ruleForm.parts.splice(idx, 1)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <button class="org-rule-add" type="button" @click="addPart">+ 添加一行（最多支持添加10行）</button>
      <template #footer>
        <button class="org-gold lg" type="button" :disabled="saving" @click="saveRule">确定</button>
        <button class="org-btn-dark" type="button" @click="ruleOpen = false">取消</button>
      </template>
    </el-dialog>

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

    <el-dialog v-model="importOpen" title="导入员工" width="640px" class="org-pick-dialog" header-class="org-pick-head" :close-on-click-modal="false">
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
          1.下载 <a @click="downloadTemplate">《员工导入模版》</a><br>
          2.打开下载表，将对应信息填入或粘贴进本表。为保证导入成功，请使用纯文本或数字。<br>
          3.信息输入完毕并保存后，点击下方“选择上传文件”按钮选择已保存的 excel 文档。
        </div>
        <label class="org-upload">
          选择上传文件 {{ importFile ? importFile.name : '' }}
          <input type="file" accept=".xlsx" class="hidden" @change="onPickFile" />
        </label>
      </div>
      <div v-else-if="importStep === 2">
        <h4 style="margin:0 0 12px;color:var(--gooci-primary);border-left:3px solid var(--gooci-primary);padding-left:8px">映射导入字段</h4>
        <table class="org-map-table">
          <thead><tr><th>员工表字段</th><th>文本字段</th></tr></thead>
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

    <el-dialog v-model="formOpen" :title="form.userId ? '编辑人员' : '新增人员'" width="460px" class="org-pick-dialog" header-class="org-pick-head" :close-on-click-modal="false">
      <div class="org-dlg-form">
        <el-form label-width="88px">
          <el-form-item required label="手机号码"><el-input v-model="form.mobile" placeholder="请输入手机号码" /></el-form-item>
          <el-form-item required label="人员名称"><el-input v-model="form.displayName" placeholder="请输入人员名称" /></el-form-item>
          <el-form-item :required="!form.userId" label="密码"><el-input v-model="form.loginPassword" type="password" show-password :placeholder="form.userId ? '不改请留空' : '请输入密码'" /></el-form-item>
          <el-form-item label="部门">
            <el-select v-model="form.deptIds" multiple placeholder="请选择部门" style="width:100%">
              <el-option v-for="dept in board.depts" :key="dept.id" :label="dept.deptName" :value="dept.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注"><el-input v-model="form.remarkText" type="textarea" placeholder="请输入备注" /></el-form-item>
          <el-form-item label="电子邮箱"><el-input v-model="form.mailAddr" placeholder="请输入电子邮箱" /></el-form-item>
          <el-form-item label="微信"><el-input v-model="form.wechatNo" placeholder="请输入微信" /></el-form-item>
          <el-form-item label="钉钉"><el-input v-model="form.dingNo" placeholder="请输入钉钉" /></el-form-item>
          <el-form-item v-for="field in board.fields" :key="field.fieldId" :required="field.requiredFlag === '1'" :label="field.fieldName">
            <el-input v-model="form.extraMap[field.fieldId]" :placeholder="field.hintText || '请输入' + field.fieldName" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <button class="org-gold lg" type="button" :disabled="saving" @click="saveStaff">确定</button>
        <button class="org-btn-dark" type="button" @click="formOpen = false">取消</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'
import OrgPager from '../../components/OrgPager.vue'

const emptySlice = { total: 0, pageNo: 1, pageSize: 10, records: [] }
const board = ref({ slice: emptySlice, columns: [], fields: [], depts: [], codeRule: { codeName: '人员编号', parts: [], previewText: '' } })
const saving = ref(false)
const ruleOpen = ref(false)
const fieldOpen = ref(false)
const importOpen = ref(false)
const formOpen = ref(false)
const ruleForm = reactive({ codeName: '人员编号', parts: [] })
const fieldForm = reactive({ fieldId: '', fieldName: '', fieldType: 'TEXT', requiredFlag: '0', hintText: '' })
const fieldRequired = computed({
  get: () => fieldForm.requiredFlag === '1',
  set: (on) => { fieldForm.requiredFlag = on ? '1' : '0' }
})
const form = reactive({
  userId: '',
  mobile: '',
  displayName: '',
  loginPassword: '',
  deptIds: [],
  remarkText: '',
  mailAddr: '',
  wechatNo: '',
  dingNo: '',
  extraMap: {}
})
const importStep = ref(1)
const importMode = ref('')
const importFile = ref(null)
const importHeaders = ref([])
const mapping = reactive({})
const importPct = ref(0)
const importResultText = ref('')

const builtinKeys = ['staffCode', 'displayName', 'mobile', 'deptName', 'mailAddr', 'wechatNo', 'dingNo', 'remarkText', 'crtTime']
const slice = computed(() => board.value.slice || emptySlice)
const extraCols = computed(() => (board.value.columns || []).filter((col) => col.visibleFlag === '1' && !builtinKeys.includes(col.colKey)))
const mapRows = computed(() => {
  const rows = [
    { key: 'mobile', label: '手机号码', required: true },
    { key: 'displayName', label: '人员名称', required: true },
    { key: 'loginPassword', label: '密码' },
    { key: 'deptName', label: '部门' },
    { key: 'remarkText', label: '备注' },
    { key: 'mailAddr', label: '电子邮箱' },
    { key: 'wechatNo', label: '微信' },
    { key: 'dingNo', label: '钉钉' },
    { key: 'cardNo', label: '员工卡号' }
  ]
  for (const field of board.value.fields || []) {
    rows.push({ key: field.fieldId, label: field.fieldName, required: field.requiredFlag === '1' })
  }
  return rows
})
const rulePreview = computed(() => previewCode(ruleForm.parts))

function shown(key) {
  const col = (board.value.columns || []).find((item) => item.colKey === key)
  return !col || col.visibleFlag === '1'
}

function extraVal(row, fieldId) {
  const hit = (row.extras || []).find((item) => item.fieldId === fieldId)
  return hit?.fieldValue || ''
}

function previewCode(parts) {
  const now = new Date()
  const yy = String(now.getFullYear()).slice(-2)
  const yyyy = String(now.getFullYear())
  const mm = String(now.getMonth() + 1).padStart(2, '0')
  const dd = String(now.getDate()).padStart(2, '0')
  let out = ''
  for (const part of parts || []) {
    if (part.valueType === 'FIXED') out += part.partValue || ''
    else if (part.valueType === 'DATE') {
      const p = part.partValue || 'YYMMDD'
      if (p === 'YYYYMMDD') out += yyyy + mm + dd
      else if (p === 'YYMM') out += yy + mm
      else if (p === 'YYYYMM') out += yyyy + mm
      else if (p === 'MMDD') out += mm + dd
      else out += yy + mm + dd
    } else if (part.valueType === 'SEQ') {
      const width = Math.max(1, (part.partValue || '001').length)
      out += '1'.padStart(width, '0')
    }
  }
  return out
}

async function load(page = slice.value.pageNo || 1) {
  board.value = await http.get('/console/staff', { params: { pageNo: page, pageSize: 10 } })
}

function openRule() {
  const rule = board.value.codeRule || {}
  ruleForm.codeName = rule.codeName || '人员编号'
  ruleForm.parts = (rule.parts || []).map((item) => ({ ...item }))
  if (!ruleForm.parts.length) {
    ruleForm.parts = [
      { valueType: 'FIXED', partValue: 'USER' },
      { valueType: 'DATE', partValue: 'YYMMDD' },
      { valueType: 'SEQ', partValue: '001' }
    ]
  }
  ruleOpen.value = true
}

function addPart() {
  if (ruleForm.parts.length >= 10) {
    ElMessage.warning('最多支持添加10行')
    return
  }
  ruleForm.parts.push({ valueType: 'FIXED', partValue: '' })
}

function movePart(idx, delta) {
  const next = idx + delta
  if (next < 0 || next >= ruleForm.parts.length) return
  const list = ruleForm.parts
  const cur = list[idx]
  list[idx] = list[next]
  list[next] = cur
}

async function saveRule() {
  saving.value = true
  try {
    board.value.codeRule = await http.post('/console/staff/code-rule', {
      codeName: ruleForm.codeName,
      parts: ruleForm.parts
    })
    ruleOpen.value = false
    ElMessage.success('已保存编号规则')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

async function toggleCol(col, on) {
  col.visibleFlag = on ? '1' : '0'
  const keys = (board.value.columns || []).filter((item) => item.visibleFlag === '1').map((item) => item.colKey)
  try {
    board.value = await http.post('/console/staff/columns', { keys })
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
    board.value = await http.post('/console/staff/fields', { ...fieldForm })
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
    board.value = await http.post('/console/staff/fields/remove', { fieldId: fieldForm.fieldId })
    newField()
    ElMessage.success('已删除')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

function openCreate() {
  form.userId = ''
  form.mobile = ''
  form.displayName = ''
  form.loginPassword = ''
  form.deptIds = []
  form.remarkText = ''
  form.mailAddr = ''
  form.wechatNo = ''
  form.dingNo = ''
  form.extraMap = {}
  formOpen.value = true
}

function openEdit(row) {
  form.userId = row.userId
  form.mobile = row.mobile
  form.displayName = row.displayName
  form.loginPassword = ''
  form.deptIds = [...(row.deptIds || [])]
  form.remarkText = row.remarkText || ''
  form.mailAddr = row.mailAddr || ''
  form.wechatNo = row.wechatNo || ''
  form.dingNo = row.dingNo || ''
  const extraMap = {}
  for (const extra of row.extras || []) extraMap[extra.fieldId] = extra.fieldValue
  form.extraMap = extraMap
  formOpen.value = true
}

async function saveStaff() {
  saving.value = true
  try {
    const extras = (board.value.fields || []).map((field) => ({
      fieldId: field.fieldId,
      fieldValue: form.extraMap[field.fieldId] || ''
    }))
    board.value = await http.post('/console/staff', { ...form, extras })
    formOpen.value = false
    ElMessage.success('已保存')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

async function askRemove(row) {
  try {
    await ElMessageBox.confirm(`确认删除 ${row.displayName || row.mobile}？`, '删除人员', { type: 'warning' })
  } catch {
    return
  }
  try {
    board.value = await http.post('/console/staff/remove', { userId: row.userId })
    ElMessage.success('已删除')
  } catch {
    /* http 已提示 */
  }
}

function openImport() {
  importStep.value = 1
  importMode.value = ''
  importFile.value = null
  importHeaders.value = []
  importPct.value = 0
  importResultText.value = ''
  importOpen.value = true
}

async function downloadTemplate() {
  try {
    const blob = await http.get('/console/staff/template', { responseType: 'blob' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '员工导入模版.xlsx'
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
    const data = await http.post('/console/staff/import-headers', body)
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
    const result = await http.post('/console/staff/import', body, { timeout: 60000 })
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
