<template>
  <div class="reg-page">
    <SiteHeader />
    <div class="reg-body">
      <div class="tabs">
        <button type="button" :class="{ on: kind === 'enterprise' }" @click="setKind('enterprise')">企业注册</button>
        <button type="button" :class="{ on: kind === 'person' }" @click="setKind('person')">个人注册</button>
      </div>
      <div class="form">
        <div class="row">
          <label><i>*</i> 账号</label>
          <div class="field">
            <el-input v-model="form.mobile" maxlength="11" placeholder="填写您的手机号码即您的平台登录账号">
              <template #prefix><span class="dot" /></template>
            </el-input>
          </div>
        </div>
        <div class="row">
          <label><i>*</i> 验证码</label>
          <div class="field sms">
            <el-input v-model="form.smsCode" maxlength="6" placeholder="填写您收到的短信验证码">
              <template #prefix><span class="dot" /></template>
            </el-input>
            <button class="sms-btn" type="button" :disabled="smsWait > 0" @click="sendSms">
              {{ smsWait > 0 ? smsWait + 's' : '获取验证码' }}
            </button>
          </div>
        </div>
        <div class="row">
          <label><i>*</i> 姓名</label>
          <div class="field">
            <el-input v-model="form.displayName" maxlength="100" placeholder="填写您的姓名">
              <template #prefix><span class="dot" /></template>
            </el-input>
          </div>
        </div>
        <div class="row">
          <label><i>*</i> 设置登录密码</label>
          <div class="field">
            <el-input v-model="form.loginPassword" type="password" show-password maxlength="50" placeholder="填写您的登录密码">
              <template #prefix><span class="dot" /></template>
            </el-input>
          </div>
        </div>
        <div class="row">
          <label><i>*</i> 请再次输入密码</label>
          <div class="field">
            <el-input v-model="form.confirmPassword" type="password" show-password maxlength="50" placeholder="再次确认您的登录密码">
              <template #prefix><span class="dot" /></template>
            </el-input>
          </div>
        </div>
        <div v-if="kind === 'enterprise'" class="row">
          <label><i>*</i> 公司名称</label>
          <div class="field">
            <el-input v-model="form.enterName" maxlength="100" placeholder="填写您的公司名称">
              <template #prefix><span class="dot" /></template>
            </el-input>
          </div>
        </div>
        <div v-else class="row">
          <label><i>*</i> 职能</label>
          <div class="field">
            <el-select v-model="form.jobName" placeholder="请选择">
              <el-option v-for="item in JOB_NAMES" :key="item" :label="item" :value="item" />
            </el-select>
          </div>
        </div>
        <div class="row">
          <label>邀请码</label>
          <div class="field">
            <el-input v-model="form.inviteCode" maxlength="50" placeholder="填写邀请码">
              <template #prefix><span class="dot" /></template>
            </el-input>
          </div>
        </div>
        <div class="agree">
          <el-checkbox v-model="form.agreed">
            阅读并同意
            <button class="doc" type="button" @click="legal = 'service'">《GOOCI平台服务协议》</button>
            和
            <button class="doc" type="button" @click="legal = 'privacy'">《隐私权政策》</button>
          </el-checkbox>
        </div>
        <button class="done" type="button" :disabled="loading" @click="submit">完 成</button>
      </div>
    </div>
    <SiteFooter />
    <el-dialog v-model="legalOpen" :title="legal === 'privacy' ? '隐私权政策' : '平台服务协议'" width="520px" class="org-pick-dialog" header-class="org-pick-head">
      <p>本演示环境用于 GOOCI 控制台交付。账号按登录名隔离，不按独立域名拆分客户。</p>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SiteHeader from '../components/SiteHeader.vue'
import SiteFooter from '../components/SiteFooter.vue'
import { useSessionStore } from '../stores/session'
import { JOB_NAMES } from '../data/regions'
import http from '../api/http'

const session = useSessionStore()
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const smsWait = ref(0)
const legal = ref('')
const kind = ref(route.query.kind === 'person' ? 'person' : 'enterprise')
const form = reactive({
  mobile: '',
  smsCode: '',
  displayName: '',
  loginPassword: '',
  confirmPassword: '',
  enterName: '',
  jobName: '',
  inviteCode: '',
  agreed: false
})

const legalOpen = computed({
  get: () => Boolean(legal.value),
  set: (value) => {
    if (!value) legal.value = ''
  }
})

onMounted(async () => {
  try {
    await session.loadSite()
  } catch {
    /* 后端未启动仍可填表 */
  }
})

watch(() => route.query.kind, (value) => {
  kind.value = value === 'person' ? 'person' : 'enterprise'
})

function setKind(next) {
  kind.value = next
  router.replace({ path: '/register', query: { kind: next } })
}

let smsTimer
async function sendSms() {
  if (!/^1\d{10}$/.test(form.mobile)) {
    ElMessage.warning('请填写正确的手机号')
    return
  }
  try {
    const data = await http.post('/auth/sms', { mobile: form.mobile })
    ElMessage.success(`演示验证码 ${data.demoCode}`)
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

async function submit() {
  loading.value = true
  try {
    const data = await http.post('/auth/register', {
      registerKind: kind.value === 'person' ? 'PERSON' : 'ENTERPRISE',
      mobile: form.mobile,
      smsCode: form.smsCode,
      displayName: form.displayName,
      loginPassword: form.loginPassword,
      confirmPassword: form.confirmPassword,
      enterName: form.enterName,
      jobName: form.jobName,
      inviteCode: form.inviteCode,
      agreed: form.agreed
    })
    session.applySession(data)
    ElMessage.success('注册成功')
    router.push('/workbench/home')
  } catch {
    /* http 已提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reg-page { min-height: 100%; background: #fff; display: flex; flex-direction: column; }
.reg-body { flex: 1; padding: 28px 0 48px; }
.tabs {
  display: flex;
  justify-content: center;
  gap: 72px;
  border-bottom: 1px solid #eef2f4;
  max-width: 560px;
  margin: 0 auto 36px;
}
.tabs button {
  border: 0;
  background: none;
  color: #8a97a0;
  font-size: 16px;
  padding: 10px 4px 12px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
}
.tabs button.on { color: var(--gooci-text); border-bottom-color: var(--gooci-primary); }
.form { width: 560px; margin: 0 auto; }
.row {
  display: grid;
  grid-template-columns: 128px 1fr;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.row label { text-align: right; color: #5b6b74; font-size: 13px; }
.row label i { color: #e25c5c; font-style: normal; margin-right: 2px; }
.field :deep(.el-input__wrapper),
.field :deep(.el-select__wrapper) {
  border-radius: 22px;
  min-height: 42px;
  box-shadow: 0 0 0 1px #e6eaed inset;
}
.field :deep(.el-select) { width: 100%; }
.sms { display: flex; align-items: center; gap: 0; }
.sms :deep(.el-input) { flex: 1; }
.sms-btn {
  border: 0;
  background: none;
  color: #8a97a0;
  white-space: nowrap;
  padding: 0 16px;
  cursor: pointer;
  height: 42px;
}
.dot {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--gooci-soft);
  display: inline-block;
}
.agree { margin: 8px 0 22px 128px; font-size: 13px; color: #5b6b74; }
.doc {
  border: 0;
  background: none;
  color: var(--gooci-primary);
  cursor: pointer;
  padding: 0;
  font-size: 13px;
}
.done {
  display: block;
  width: 220px;
  height: 44px;
  margin: 0 auto;
  border: 0;
  border-radius: 22px;
  background: var(--gooci-primary);
  color: #fff;
  font-size: 16px;
  cursor: pointer;
}
.done:disabled { opacity: .6; cursor: default; }
</style>
