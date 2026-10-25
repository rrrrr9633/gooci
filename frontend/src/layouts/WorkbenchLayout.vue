<template>
  <div class="shell">
    <SiteHeader />
    <div class="body">
      <div class="nav" :class="{ pinned }">
        <aside class="rail">
          <button
            v-for="item in rootMenus"
            :key="item.menuCode"
            class="rail-btn"
            :class="{ on: activeRoot === item.menuCode }"
            @click="openRoot(item)"
          >
            <el-icon><component :is="iconOf(item.iconCode)" /></el-icon>
          </button>
          <div class="rail-spacer" />
          <button class="rail-btn pin" :class="{ on: pinned }" @click="pinned = !pinned">
            <el-icon><Fold v-if="pinned" /><Expand v-else /></el-icon>
          </button>
        </aside>
        <aside class="submenu">
          <el-menu
            v-if="rootMenus.length"
            :default-active="route.path"
            :default-openeds="opened"
            router
            class="tree"
          >
            <el-sub-menu v-for="item in rootMenus" :key="item.menuCode" :index="item.menuCode">
              <template #title>{{ item.menuName }}</template>
              <el-menu-item
                v-for="child in item.children || []"
                :key="child.menuCode"
                :index="child.routePath"
              >
                {{ child.menuName }}
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </aside>
      </div>
      <main class="main" :class="{ 'main-ext': isExt }">
        <router-view v-slot="{ Component, route: viewRoute }">
          <transition name="sheet">
            <component :is="Component" :key="viewRoute.fullPath" />
          </transition>
        </router-view>
      </main>
      <aside v-if="$route.path === '/workbench/home'" class="side">
        <div class="card user">
          <div class="name">{{ session.profile?.displayName }} {{ session.profile?.mobile }}</div>
          <div class="meta">{{ session.profile?.regionText }} · {{ session.profile?.enterName }}</div>
          <div class="badge">{{ session.profile?.certStatus === '1' ? '已认证' : '未认证' }} · {{ session.profile?.roleName }}</div>
          <div class="nums">
            <div><b>{{ overview.unreadMessageCount }}</b><span>未读信息</span></div>
            <div><b>{{ overview.pendingTaskCount }}</b><span>待办任务</span></div>
          </div>
        </div>
        <div class="card">
          <div class="card-h">消息动态</div>
          <div v-if="ticker.length" class="ticker" @click="router.push('/workbench/biz/inbox')">
            <div class="ticker-track" :class="{ roll: ticker.length > 1 }">
              <div v-for="(msg, idx) in tickerLoop" :key="idx" class="ticker-item">{{ msg.content }}</div>
            </div>
          </div>
          <div v-else class="empty">暂无数据</div>
        </div>
        <div class="card nums two">
          <div><b>{{ overview.linkedDeviceCount }}</b><span>已接入设备数量</span></div>
          <div><b>{{ overview.onlineDeviceCount }}</b><span>在线设备数量</span></div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Collection, Expand, Fold, Grid, Lock, Monitor, OfficeBuilding } from '@element-plus/icons-vue'
import SiteHeader from '../components/SiteHeader.vue'
import { useSessionStore } from '../stores/session'
import http from '../api/http'

const session = useSessionStore()
const route = useRoute()
const router = useRouter()
const pinned = ref(false)
const overview = ref({
  unreadMessageCount: 0,
  pendingTaskCount: 0,
  linkedDeviceCount: 0,
  onlineDeviceCount: 0,
  recentMessages: []
})
const ticker = computed(() => overview.value.recentMessages || [])
const tickerLoop = computed(() => ticker.value.length > 1 ? ticker.value.concat(ticker.value) : ticker.value)
const activeRoot = ref('workbench')
const isExt = computed(() => route.path.startsWith('/workbench/ext/'))

const rootMenus = computed(() => session.profile?.menus || [])
const opened = computed(() => rootMenus.value.map((item) => item.menuCode))

function iconOf(code) {
  if (code === 'office') return OfficeBuilding
  if (code === 'collection') return Collection
  if (code === 'lock') return Lock
  if (code === 'grid') return Grid
  return Monitor
}

function openRoot(item) {
  activeRoot.value = item.menuCode
  const first = item.children?.[0] || item
  if (first.routePath) router.push(first.routePath)
}

function syncRootByPath() {
  const path = route.path
  for (const item of rootMenus.value) {
    const hit = (item.children || []).some((child) => child.routePath === path) || item.routePath === path
    if (hit) {
      activeRoot.value = item.menuCode
      return
    }
  }
}

watch(() => route.path, syncRootByPath)

async function loadOverview() {
  try {
    overview.value = await http.get('/console/overview')
  } catch {
    /* 概览占位 */
  }
}

onMounted(async () => {
  try {
    await session.loadSite()
  } catch (error) {
    /* 官网元数据失败不挡工作台 */
  }
  await session.loadSession()
  await loadOverview()
  syncRootByPath()
})

watch(() => route.path, (path) => {
  if (path === '/workbench/home') loadOverview()
})
</script>

<style scoped>
.shell { height: 100%; display: flex; flex-direction: column; }
.body { flex: 1; display: flex; min-height: 0; background: var(--gooci-canvas); }
.nav {
  display: flex;
  position: relative;
  z-index: 30;
  flex-shrink: 0;
}
.rail {
  width: 56px;
  background: var(--gooci-rail);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0;
  gap: 8px;
  z-index: 2;
}
.rail-spacer { flex: 1; }
.rail-btn {
  width: 40px;
  height: 40px;
  border: 0;
  background: transparent;
  color: #f4fcff;
  border-radius: 8px;
  cursor: pointer;
}
.rail-btn.on, .rail-btn:hover { background: var(--gooci-rail-deep); color: #fff; }
.submenu {
  width: 208px;
  background: #fff;
  border-right: 1px solid var(--gooci-line);
  overflow: auto;
  transform-origin: left center;
  transition: width .22s ease, opacity .18s ease, transform .22s ease, box-shadow .22s ease;
}
.nav:not(.pinned) .submenu {
  position: absolute;
  left: 56px;
  top: 0;
  bottom: 0;
  width: 208px;
  opacity: 0;
  transform: translateX(-12px);
  pointer-events: none;
  box-shadow: none;
}
.nav:not(.pinned):hover .submenu {
  opacity: 1;
  transform: translateX(0);
  pointer-events: auto;
  box-shadow: 10px 0 24px #1a6a8820;
}
.tree {
  border-right: none;
  --el-menu-active-color: var(--gooci-primary-dark);
  --el-menu-hover-bg-color: var(--gooci-soft);
}
.tree :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  background: var(--gooci-primary);
  color: #fff;
}
.tree :deep(.el-sub-menu.is-active > .el-sub-menu__title .el-sub-menu__icon-arrow) {
  color: #fff;
}
.tree :deep(.el-menu-item.is-active) {
  background: var(--gooci-soft);
  color: var(--gooci-primary-dark);
}
.main { flex: 1; overflow: auto; padding: 20px 24px; min-width: 0; }
.main-ext { padding: 0; overflow: hidden; display: flex; flex-direction: column; }
.side { width: 280px; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.card { background: #fff; border-radius: 8px; padding: 14px; box-shadow: 0 1px 4px #0000000d; }
.user .name { font-weight: 600; }
.user .meta, .user .badge { color: var(--gooci-muted); font-size: 12px; margin-top: 4px; }
.nums { display: grid; grid-template-columns: 1fr 1fr; margin-top: 12px; text-align: center; }
.nums b { display: block; font-size: 22px; color: #3ba8d0; }
.nums span { font-size: 12px; color: var(--gooci-muted); }
.card-h { font-weight: 600; margin-bottom: 8px; }
.empty { color: #99a; font-size: 13px; text-align: center; padding: 20px 0; }
.ticker {
  height: 168px;
  overflow: hidden;
  cursor: pointer;
}
.ticker-track.roll { animation: ticker-roll 14s linear infinite; }
.ticker-item {
  font-size: 13px;
  color: var(--gooci-text);
  padding: 8px 2px;
  border-bottom: 1px dashed var(--gooci-line);
  line-height: 1.5;
}
@keyframes ticker-roll {
  from { transform: translateY(0); }
  to { transform: translateY(-50%); }
}
</style>
