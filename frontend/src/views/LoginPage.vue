<template>
  <div class="login-page">
    <SiteHeader />
    <div class="box">
      <BrandLogo />
      <h2>登录工作台</h2>
      <el-form :model="form" @submit.prevent="submit">
        <el-form-item><el-input v-model="form.loginName" placeholder="账号" size="large" /></el-form-item>
        <el-form-item><el-input v-model="form.loginPassword" type="password" show-password placeholder="密码" size="large" /></el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="submit">登录</el-button>
      </el-form>
      <p class="hint">演示账号 admin / GooCi@2026</p>
      <p class="hint"><router-link to="/register">企业 / 个人注册</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BrandLogo from '../components/BrandLogo.vue'
import SiteHeader from '../components/SiteHeader.vue'
import { useSessionStore } from '../stores/session'

const session = useSessionStore()
const router = useRouter()
const route = useRoute()
const loading = ref(false)
const form = reactive({ loginName: 'admin', loginPassword: 'GooCi@2026' })

onMounted(async () => {
  try {
    await session.loadSite()
  } catch (error) {
    /* 后端未启动时仍可填写登录 */
  }
})

async function submit() {
  loading.value = true
  try {
    await session.login(form.loginName, form.loginPassword)
    router.push(route.query.redirect || '/workbench/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { height: 100%; background: linear-gradient(180deg, #d7f0f8, #fff); }
.box {
  width: 380px;
  margin: 80px auto;
  background: #fff;
  padding: 32px;
  border-radius: 12px;
  box-shadow: 0 12px 32px #3ba8d01a;
}
h2 { margin: 16px 0 20px; }
.hint { margin-top: 16px; color: #88a; font-size: 12px; text-align: center; }
.hint a { color: var(--gooci-primary); }
</style>
