<template>
  <div class="org-pager">
    <span>显示 {{ from }} 到 {{ to }} 条，共 {{ slice.total || 0 }} 条记录</span>
    <button :disabled="slice.pageNo <= 1" type="button" @click="$emit('change', slice.pageNo - 1)">‹</button>
    <button
      v-for="page in pages"
      :key="page"
      type="button"
      :class="{ on: page === slice.pageNo }"
      @click="$emit('change', page)"
    >{{ page }}</button>
    <button :disabled="slice.pageNo >= pages.length" type="button" @click="$emit('change', slice.pageNo + 1)">›</button>
    <button type="button" @click="$emit('change', 1)">首页</button>
    <button type="button" @click="$emit('change', pages.length)">尾页</button>
    <button type="button" @click="$emit('change', slice.pageNo)">刷新</button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  slice: { type: Object, required: true }
})
defineEmits(['change'])

const pages = computed(() => {
  const size = props.slice.pageSize || 5
  const total = props.slice.total || 0
  const count = Math.max(1, Math.ceil(total / size))
  return Array.from({ length: count }, (_, i) => i + 1)
})
const from = computed(() => {
  if (!props.slice.total) return 0
  return (props.slice.pageNo - 1) * props.slice.pageSize + 1
})
const to = computed(() => Math.min(props.slice.total || 0, props.slice.pageNo * props.slice.pageSize))
</script>
