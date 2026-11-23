<template>
  <div class="home">
    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">工业互联网平台</p>
        <h1>GOOCI</h1>
        <p class="desc">数字化工厂控制台</p>
        <el-button type="primary" size="large" @click="goConsole">进入工作台</el-button>
      </div>
      <div class="hero-art">
        <div class="hex-map">
          <span class="node n1">组织权限</span>
          <span class="node n2">设备接入</span>
          <span class="node n3">人机交互</span>
          <div class="hex">GOOCI</div>
          <span class="node n4">数据采集</span>
          <span class="node n5">边缘计算</span>
          <span class="node n6">待办协同</span>
        </div>
      </div>
      <aside class="welcome">
        <template v-if="session.loggedIn && session.profile">
          <div class="hello">欢迎您，{{ session.profile.displayName }}</div>
          <div class="tag">{{ session.profile.roleName }}</div>
          <div class="meta">{{ session.profile.enterName }}</div>
          <el-button type="primary" @click="$router.push('/workbench/home')">业务中心入口</el-button>
        </template>
        <template v-else>
          <div class="hello">欢迎使用 GOOCI</div>
          <div class="tag">登录后进入工作台</div>
          <el-button type="primary" @click="$router.push('/login')">登录</el-button>
        </template>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useSessionStore } from '../stores/session'

const session = useSessionStore()
const router = useRouter()

function goConsole() {
  if (session.loggedIn) router.push('/workbench/home')
  else router.push('/login')
}
</script>

<style scoped>
.hero {
  min-height: calc(100vh - 56px);
  background:
    linear-gradient(180deg, #cfeef8 0%, #f4fbfd 52%, #ffffff 100%);
  display: grid;
  grid-template-columns: minmax(200px, 240px) 1fr minmax(240px, 280px);
  gap: 0;
  padding: 56px 8%;
  align-items: center;
}
.hero-copy { z-index: 2; }
.hero-art {
  display: flex;
  justify-content: center;
}
.welcome { z-index: 2; }
.eyebrow { color: #2b8fb6; letter-spacing: 3px; margin: 0 0 8px; font-size: 13px; }
h1 { font-size: 56px; line-height: 1.08; margin: 0 0 16px; color: #123445; }
.desc { color: #5b7380; margin-bottom: 24px; max-width: 420px; }
.hex-map {
  position: relative;
  width: 460px;
  height: 360px;
  display: grid;
  place-items: center;
}
.hex {
  width: 168px;
  height: 188px;
  clip-path: polygon(50% 0, 100% 25%, 100% 75%, 50% 100%, 0 75%, 0 25%);
  background: linear-gradient(180deg, #4eb7db, #2b8fb6);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 22px;
  font-weight: 700;
  box-shadow: 0 16px 32px #3ba8d033;
  z-index: 1;
}
.node {
  position: absolute;
  background: #fff;
  color: #2b8fb6;
  border: 1px solid #c5e6f2;
  border-radius: 20px;
  padding: 6px 14px;
  font-size: 13px;
}
.n1 { top: 18px; left: 18%; }
.n2 { top: 18px; right: 16%; }
.n3 { top: 46%; left: 0; }
.n4 { top: 46%; right: 0; }
.n5 { bottom: 28px; left: 16%; }
.n6 { bottom: 28px; right: 14%; }
.welcome {
  background: #fff;
  border-radius: 12px;
  padding: 22px;
  box-shadow: 0 8px 24px #3ba8d01f;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 180px;
}
.hello { font-weight: 600; font-size: 16px; }
.tag { color: #2b8fb6; font-size: 13px; }
.meta { color: #6b7c86; font-size: 13px; }
@media (max-width: 1100px) {
  .hero { grid-template-columns: 1fr; padding: 40px 8%; }
  .hero-art { justify-content: center; }
  .hex-map { width: 100%; max-width: 460px; height: 280px; }
}
</style>
