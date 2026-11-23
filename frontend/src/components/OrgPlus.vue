<template>
  <div class="org-plus">
    <button class="box" type="button" @click="pick">
      <img v-if="preview && image" :src="preview" alt="" />
      <span v-else-if="preview" class="pdf">PDF</span>
      <span v-else class="add">+</span>
    </button>
    <input ref="input" class="hidden" type="file" accept=".jpg,.jpeg,.gif,.png,.pdf" @change="onPick" />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const props = defineProps({
  modelValue: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue'])
const input = ref(null)
const preview = ref('')
const image = ref(true)

function pick() {
  input.value?.click()
}

function isImageName(name) {
  return /\.(png|jpe?g|gif)$/i.test(name || '')
}

async function hydrate(fileId) {
  if (!fileId) {
    preview.value = ''
    return
  }
  const token = localStorage.getItem('gooci-token')
  const res = await fetch(`/api/console/org/cert/file/${fileId}`, {
    headers: { Authorization: `Bearer ${token}` }
  })
  if (!res.ok) {
    preview.value = ''
    return
  }
  const blob = await res.blob()
  image.value = blob.type.startsWith('image/')
  preview.value = URL.createObjectURL(blob)
}

async function onPick(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (file.size > 4 * 1024 * 1024) {
    ElMessage.warning('附件大小不超过4M')
    return
  }
  const body = new FormData()
  body.append('file', file)
  try {
    const data = await http.post('/console/org/cert/file', body)
    image.value = isImageName(file.name)
    preview.value = URL.createObjectURL(file)
    emit('update:modelValue', data.fileId)
  } catch {
    /* http 已提示 */
  }
}

watch(() => props.modelValue, (value) => {
  hydrate(value)
}, { immediate: true })
</script>
