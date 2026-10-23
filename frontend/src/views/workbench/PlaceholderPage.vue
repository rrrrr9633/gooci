<template>
  <div class="hold">
    <h3>{{ title }}</h3>
    <p>页面设计暂留，后续按页开发。</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta.title || '页面')
</script>

<style scoped>
.hold {
  background: #fff;
  min-height: 420px;
  border-radius: 8px;
  padding: 48px;
  color: #667;
}
h3 { color: #123445; margin: 0 0 8px; }
</style>
