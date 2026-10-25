<template>
  <div class="org-page org-change-page">
    <section class="sheet">
      <div class="org-block-h">工商登记信息变更</div>
      <div class="org-change">
        <div class="org-change-row">
          <label><i>*</i>企业名称</label>
          <el-input v-model="form.enterName" maxlength="100" />
        </div>
        <div class="org-change-row">
          <label><i>*</i>统一社会信用代码</label>
          <el-input v-model="form.creditCode" maxlength="32" />
        </div>
        <div class="org-change-row">
          <label><i>*</i>企业证件类型</label>
          <el-select v-model="form.licenseType" placeholder="请选择">
            <el-option v-for="item in LICENSE_TYPES" :key="item" :label="item" :value="item" />
          </el-select>
        </div>
        <div class="org-change-row">
          <label><i>*</i>证件扫描件</label>
          <div>
            <OrgPlus v-model="form.licenseScan" />
            <p class="org-hint">请上传格式为jpg、jpeg、gif、png、pdf的图片，附件大小不超过4M</p>
          </div>
        </div>
        <div class="org-change-row">
          <label>法人代表</label>
          <el-input v-model="form.legalPerson" maxlength="100" />
        </div>
        <div class="org-change-row">
          <label>法人身份证正反面</label>
          <div>
            <div class="org-plus-row">
              <OrgPlus v-model="form.legalIdFront" />
              <OrgPlus v-model="form.legalIdBack" />
            </div>
            <p class="org-hint">请上传格式为jpg、jpeg、gif、png、pdf的图片，附件大小不超过4M</p>
          </div>
        </div>
        <div class="org-change-row">
          <label><i>*</i>变更凭证</label>
          <div>
            <OrgPlus v-model="form.changeProof" />
            <p class="org-hint">请上传变更凭证，单个附件大小不超过4M</p>
          </div>
        </div>
        <div class="org-change-row">
          <label><i>*</i>备注</label>
          <el-input v-model="form.bizRemark" type="textarea" :rows="4" maxlength="200" placeholder="最多输入200个字符" show-word-limit />
        </div>
      </div>
    </section>
    <div class="org-actions">
      <button class="org-gold lg" type="button" :disabled="saving" @click="save">提交</button>
      <button class="org-btn-dark" type="button" @click="goBack">取消</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import OrgPlus from '../../components/OrgPlus.vue'
import { LICENSE_TYPES } from '../../data/regions'

const router = useRouter()
const saving = ref(false)
const form = reactive({
  enterName: '',
  creditCode: '',
  licenseType: '三证合一',
  licenseScan: '',
  legalPerson: '',
  legalIdFront: '',
  legalIdBack: '',
  changeProof: '',
  bizRemark: ''
})

function fill(data) {
  Object.assign(form, {
    enterName: data.enterName || '',
    creditCode: data.creditCode || '',
    licenseType: data.licenseType || '三证合一',
    licenseScan: data.licenseScan || '',
    legalPerson: data.legalPerson || '',
    legalIdFront: data.legalIdFront || '',
    legalIdBack: data.legalIdBack || '',
    changeProof: data.changeProof || '',
    bizRemark: data.bizRemark || ''
  })
}

async function save() {
  saving.value = true
  try {
    fill(await http.post('/console/org/cert/biz', { ...form }))
    ElMessage.success('已提交，待厂商审核')
    router.push('/workbench/org/cert')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push('/workbench/org/cert')
}

onMounted(async () => {
  fill(await http.get('/console/org/cert/biz'))
})
</script>
