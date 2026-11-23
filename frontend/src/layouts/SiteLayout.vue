<template>
  <div class="site">
    <SiteHeader />
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import SiteHeader from '../components/SiteHeader.vue'
import { useSessionStore } from '../stores/session'

const session = useSessionStore()
onMounted(async () => {
  try {
    await session.loadSite()
    if (session.loggedIn) {
      try {
        await session.loadSession()
      } catch (error) {
        session.logout()
      }
    }
  } catch (error) {
    /* 后端未启动时仍展示首页 */
  }
})
</script>

<style scoped>
.site { height: 100%; background: #fff; }
</style>
