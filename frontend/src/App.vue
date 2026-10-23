<template>
  <router-view v-slot="{ Component, route }">
    <transition :name="viewMove">
      <component :is="Component" :key="route.matched[0]?.path || route.path" />
    </transition>
  </router-view>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const viewMove = ref('view-forward')

function lane(path = '') {
  if (path.startsWith('/workbench')) return 'workbench'
  if (path.startsWith('/ops/login')) return 'login'
  if (path.startsWith('/ops')) return 'ops'
  if (path.startsWith('/login') || path.startsWith('/register')) return 'login'
  return 'home'
}

useRouter().beforeEach((to, from) => {
  const fromLane = lane(from.path)
  const toLane = lane(to.path)
  if (fromLane === toLane) return
  if (toLane === 'login') viewMove.value = 'view-up'
  else if (fromLane === 'login' && (toLane === 'home' || toLane === 'ops')) viewMove.value = 'view-down'
  else if (toLane === 'workbench' || toLane === 'ops') viewMove.value = 'view-forward'
  else viewMove.value = 'view-back'
})
</script>
