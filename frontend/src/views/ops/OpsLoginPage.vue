<template>
  <div class="login-page">
    <header class="ops-login-bar">
      <BrandLogo light />
      <span>厂商运营台</span>
    </header>
    <div class="box">
      <h2>登录运营台</h2>
      <p class="sub">与企业工作台分开。这里配置客服电话、企业模块与申请。</p>
      <el-form :model="form" @submit.prevent="submit">
        <el-form-item><el-input v-model="form.loginName" placeholder="账号" size="large" /></el-form-item>
        <el-form-item><el-input v-model="form.loginPassword" type="password" show-password placeholder="密码" size="large" /></el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">登录</el-button>
      </el-form>
      <p class="hint">演示账号 ops / GooCi@2026</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BrandLogo from '../../components/BrandLogo.vue'
import opsHttp from '../../api/opsHttp'

const router = useRouter()
const loading = ref(false)
const form = reactive({ loginName: 'ops', loginPassword: 'GooCi@2026' })

async function submit() {
  loading.value = true
  try {
    const data = await opsHttp.post('/auth/login', form)
    localStorage.setItem('gooci-ops-token', data.token)
    router.push('/ops/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { height: 100%; background: linear-gradient(180deg, #d7f0f8, #fff); }
.ops-login-bar {
  height: 56px;
  background: var(--gooci-header);
  color: #fff;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
}
.box {
  width: 380px;
  margin: 80px auto;
  background: #fff;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 12px 32px #3ba8d01a;
}
h2 { margin: 0 0 8px; }
.sub { color: #88a; font-size: 13px; margin: 0 0 20px; line-height: 1.6; }
.hint { margin-top: 16px; color: #88a; font-size: 12px; text-align: center; }
</style>
