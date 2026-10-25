<template>
  <div class="org-page org-change-page">
    <section class="sheet">
      <div class="org-block-h">银行账户信息</div>
      <div class="org-change">
        <div class="org-change-row">
          <label><i>*</i>选择银行</label>
          <el-select v-model="form.bankName" placeholder="请选择银行" filterable>
            <el-option v-for="item in BANK_NAMES" :key="item" :label="item" :value="item" />
          </el-select>
        </div>
        <div class="org-change-row">
          <label><i>*</i>选择省份</label>
          <el-select v-model="form.bankProvince" placeholder="请选择省份" @change="onProvince">
            <el-option v-for="item in REGION_TREE" :key="item.name" :label="item.name" :value="item.name" />
          </el-select>
        </div>
        <div class="org-change-row">
          <label><i>*</i>选择城市</label>
          <el-select v-model="form.bankCity" placeholder="请选择城市">
            <el-option v-for="item in cities" :key="item.name" :label="item.name" :value="item.name" />
          </el-select>
        </div>
        <div class="org-change-row">
          <label><i>*</i>开户支行</label>
          <el-input v-model="form.bankBranch" maxlength="150" />
        </div>
        <div class="org-change-row">
          <label><i>*</i>企业对公账户</label>
          <el-input v-model="form.bankAccount" maxlength="50" />
        </div>
        <div class="org-change-row">
          <label><i>*</i>联系电话</label>
          <el-input v-model="form.bankPhone" maxlength="30" />
        </div>
        <div class="org-change-row">
          <label><i>*</i>备注</label>
          <el-input v-model="form.bankRemark" type="textarea" :rows="4" maxlength="200" placeholder="最多输入200个字符" show-word-limit />
        </div>
        <div class="org-note">
          <span class="mark">i</span>
          提示：若您在 GOOCI 网站上没有找到贵公司的开户银行，请联系我们，我们的客户服务电话
          <b>{{ phone }}</b>
          ，我们将竭诚为您服务！
        </div>
      </div>
    </section>
    <div class="org-actions">
      <button class="org-gold lg" type="button" :disabled="saving" @click="save">提交</button>
      <button class="org-gold lg" type="button" @click="goBack">取消</button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import { BANK_NAMES, REGION_TREE } from '../../data/regions'

const router = useRouter()
const saving = ref(false)
const servicePhone = ref('')
const form = reactive({
  bankName: '',
  bankProvince: '',
  bankCity: '',
  bankBranch: '',
  bankAccount: '',
  bankPhone: '',
  bankRemark: ''
})

const cities = computed(() => {
  const province = REGION_TREE.find((item) => item.name === form.bankProvince)
  return province ? province.cities : []
})
const phone = computed(() => servicePhone.value || '预留')

function onProvince() {
  form.bankCity = ''
}

function fill(data) {
  Object.assign(form, {
    bankName: data.bankName || '',
    bankProvince: data.bankProvince || '',
    bankCity: data.bankCity || '',
    bankBranch: data.bankBranch || '',
    bankAccount: data.bankAccount || '',
    bankPhone: data.bankPhone || '',
    bankRemark: data.bankRemark || ''
  })
  servicePhone.value = data.servicePhone || ''
}

async function save() {
  saving.value = true
  try {
    fill(await http.post('/console/org/cert/bank', { ...form }))
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
  fill(await http.get('/console/org/cert/bank'))
})
</script>
