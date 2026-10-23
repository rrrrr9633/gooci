<template>
  <header class="topbar">
    <div class="left">
      <router-link to="/" class="logo-link">
        <BrandLogo light />
        <span class="platform">工业互联网平台</span>
      </router-link>
      <nav class="nav">
        <router-link to="/" class="nav-item" :class="{ on: $route.path === '/' }">首页</router-link>
      </nav>
    </div>
    <div class="right">
      <span class="phone">客服电话 {{ phoneText }}</span>
      <template v-if="session.loggedIn && session.profile">
        <el-button type="primary" size="small" @click="$router.push('/workbench/home')">进入工作台</el-button>
        <span class="user">{{ session.profile.displayName }}</span>
        <el-button link type="info" @click="onLogout">退出</el-button>
      </template>
      <el-button v-else size="small" @click="$router.push('/login')">登录</el-button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import BrandLogo from './BrandLogo.vue'
import { useSessionStore } from '../stores/session'

const session = useSessionStore()
const router = useRouter()

const phoneText = computed(() => session.siteMeta.servicePhone || '预留')

function onLogout() {
  session.logout()
  router.push('/')
}
</script>

<style scoped>
.topbar {
  height: 56px;
  background: var(--gooci-header);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.left, .right { display: flex; align-items: center; gap: 18px; }
.logo-link { display: flex; align-items: center; gap: 10px; }
.platform { font-size: 12px; color: #f4fcff; white-space: nowrap; }
.nav { display: flex; gap: 8px; }
.nav-item {
  padding: 6px 12px;
  border-bottom: 2px solid transparent;
  color: #f4fcff;
  font-size: 14px;
}
.nav-item.on { color: #fff; border-bottom-color: #fff; }
.phone { color: #f4fcff; font-size: 13px; }
.user { font-size: 13px; }
.topbar :deep(.el-button--primary) {
  --el-button-bg-color: #fff;
  --el-button-border-color: #fff;
  --el-button-text-color: #2b8fb6;
  --el-button-hover-bg-color: #e8f6fb;
  --el-button-hover-border-color: #e8f6fb;
  --el-button-hover-text-color: #2b8fb6;
}
.topbar :deep(.el-button.is-link) { color: #fff; }
</style>
