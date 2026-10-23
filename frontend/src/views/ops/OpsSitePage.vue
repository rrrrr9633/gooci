<template>
  <div class="ops-page">
    <h2>客服电话</h2>
    <p class="ops-lead">写到官网和工作台顶栏。空则显示「预留」。</p>
    <el-form label-width="96px" style="max-width:420px">
      <el-form-item label="客服电话">
        <el-input v-model="form.servicePhone" maxlength="30" placeholder="例如 400-000-0000" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="save">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import opsHttp from '../../api/opsHttp'

const loading = ref(false)
const form = reactive({ servicePhone: '' })

onMounted(async () => {
  const data = await opsHttp.get('/site')
  form.servicePhone = data.servicePhone || ''
})

async function save() {
  loading.value = true
  try {
    const data = await opsHttp.post('/site', { servicePhone: form.servicePhone })
    form.servicePhone = data.servicePhone || ''
    ElMessage.success('已保存')
  } finally {
    loading.value = false
  }
}
</script>
