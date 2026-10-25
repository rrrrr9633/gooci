<template>
  <div class="ops-shell">
    <header class="ops-top">
      <router-link to="/ops/home" class="ops-brand">
        <BrandLogo light />
        <span>厂商运营台</span>
      </router-link>
      <div class="ops-right">
        <span>{{ displayName }}</span>
        <el-button link type="info" @click="onLogout">退出</el-button>
      </div>
    </header>
    <div class="ops-body">
      <aside class="ops-nav">
        <router-link v-for="item in menus" :key="item.path" :to="item.path" class="ops-link">
          {{ item.name }}
        </router-link>
      </aside>
      <main class="ops-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BrandLogo from '../components/BrandLogo.vue'
import opsHttp from '../api/opsHttp'

const router = useRouter()
const displayName = ref('厂商运营')
const menus = [
  { path: '/ops/home', name: '企业概览' },
  { path: '/ops/site', name: '客服电话' },
  { path: '/ops/apps', name: '模块图标' },
  { path: '/ops/changes', name: '变更申请' }
]

onMounted(async () => {
  try {
    const data = await opsHttp.get('/session')
    displayName.value = data.displayName || '厂商运营'
  } catch {
    /* 拦截器会跳登录 */
  }
})

function onLogout() {
  localStorage.removeItem('gooci-ops-token')
  router.push('/ops/login')
}
</script>
