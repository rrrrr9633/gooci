<template>
  <div class="org-page">
    <div class="org-tip">
      <h4>温馨提示</h4>
      <p v-if="enter.certStatus === '1'">您的企业已经成为 GOOCI 实名认证会员。</p>
      <p v-else>完成实名认证后可开通企业组织管理等能力。</p>
      <button class="jump" type="button" @click="$router.push('/workbench/org/cert')">查看实名认证信息</button>
    </div>

    <section class="org-block">
      <div class="org-block-h">企业信息</div>
      <div class="org-form">
        <div class="org-form-row">
          <label>企业名称</label>
          <div class="org-form-field span"><el-input v-model="form.enterName" maxlength="100" /></div>
        </div>
        <div class="org-form-row">
          <label>企业简介</label>
          <div class="org-form-field span"><el-input v-model="form.enterIntro" type="textarea" :rows="3" maxlength="500" placeholder="请输入企业简介" /></div>
        </div>
        <div class="org-form-row">
          <label>企业性质</label>
          <div class="org-form-field">
            <el-select v-model="form.enterNature" placeholder="请选择企业性质" clearable>
              <el-option v-for="item in ENTER_NATURES" :key="item" :label="item" :value="item" />
            </el-select>
          </div>
          <label>成立时间</label>
          <div class="org-form-field">
            <el-date-picker v-model="form.foundDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择成立时间" style="width:100%" />
          </div>
        </div>
        <div class="org-form-row">
          <label>所属行业</label>
          <div class="org-form-field">
            <el-select v-model="form.industryName" placeholder="请选择所属行业" clearable>
              <el-option v-for="item in INDUSTRIES" :key="item" :label="item" :value="item" />
            </el-select>
          </div>
          <label>公司规模</label>
          <div class="org-form-field">
            <el-select v-model="form.enterScale" placeholder="请选择企业规模" clearable>
              <el-option v-for="item in ENTER_SCALES" :key="item" :label="item" :value="item" />
            </el-select>
          </div>
        </div>
      </div>
    </section>

    <section class="org-block">
      <div class="org-block-h">联系信息</div>
      <div class="org-form">
        <div class="org-form-row">
          <label>联系人</label>
          <div class="org-form-field"><el-input v-model="form.contactName" maxlength="100" /></div>
          <label>手机</label>
          <div class="org-form-field"><el-input v-model="form.contactMobile" maxlength="20" /></div>
        </div>
        <div class="org-form-row">
          <label>联系电话</label>
          <div class="org-form-field"><el-input v-model="form.contactPhone" maxlength="30" placeholder="请输入联系人电话" /></div>
          <label>email</label>
          <div class="org-form-field"><el-input v-model="form.contactMail" maxlength="100" placeholder="请输入联系人邮箱" /></div>
        </div>
        <div class="org-form-row">
          <label>企业地址</label>
          <div class="org-form-field trio">
            <el-select v-model="form.provinceName" placeholder="请选择" @change="onProvince">
              <el-option v-for="item in REGION_TREE" :key="item.name" :label="item.name" :value="item.name" />
            </el-select>
            <el-select v-model="form.cityName" placeholder="请选择" @change="onCity">
              <el-option v-for="item in cities" :key="item.name" :label="item.name" :value="item.name" />
            </el-select>
            <el-select v-model="form.districtName" placeholder="请选择">
              <el-option v-for="item in districts" :key="item" :label="item" :value="item" />
            </el-select>
          </div>
        </div>
        <div class="org-form-row">
          <label></label>
          <div class="org-form-field span"><el-input v-model="form.addressText" maxlength="300" placeholder="请输入详细地址" /></div>
        </div>
      </div>
    </section>

    <div class="org-actions">
      <button class="org-gold lg" type="button" :disabled="saving" @click="save">提交</button>
      <button class="org-btn-dark" type="button" @click="goBack">返回</button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import { ENTER_NATURES, ENTER_SCALES, INDUSTRIES, REGION_TREE } from '../../data/regions'

const router = useRouter()
const enter = ref({})
const saving = ref(false)
const form = reactive({
  enterName: '',
  enterIntro: '',
  enterNature: '',
  foundDate: '',
  enterScale: '',
  industryName: '',
  contactName: '',
  contactMobile: '',
  contactPhone: '',
  contactMail: '',
  provinceName: '',
  cityName: '',
  districtName: '',
  addressText: ''
})

const cities = computed(() => {
  const province = REGION_TREE.find((item) => item.name === form.provinceName)
  return province ? province.cities : []
})
const districts = computed(() => {
  const city = cities.value.find((item) => item.name === form.cityName)
  return city ? city.districts : []
})

function fill(data) {
  enter.value = data
  Object.assign(form, {
    enterName: data.enterName || '',
    enterIntro: data.enterIntro || '',
    enterNature: data.enterNature || '',
    foundDate: data.foundDate || '',
    enterScale: data.enterScale || '',
    industryName: data.industryName || '',
    contactName: data.contactName || '',
    contactMobile: data.contactMobile || '',
    contactPhone: data.contactPhone || '',
    contactMail: data.contactMail || '',
    provinceName: data.provinceName || '',
    cityName: data.cityName || '',
    districtName: data.districtName || '',
    addressText: data.addressText || ''
  })
}

function onProvince() {
  form.cityName = ''
  form.districtName = ''
}

function onCity() {
  form.districtName = ''
}

async function load() {
  fill(await http.get('/console/org/enterprise'))
}

async function save() {
  saving.value = true
  try {
    fill(await http.post('/console/org/enterprise', { ...form }))
    ElMessage.success('已保存')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push('/workbench/org/structure')
}

onMounted(load)
</script>
