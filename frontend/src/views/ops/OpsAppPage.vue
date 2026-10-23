<template>
  <div class="ops-page">
    <h2>模块图标</h2>
    <p class="ops-lead">当前按模块代码内置矢量图标。单独上传覆盖下个版本做，不在企业侧配置。</p>
    <div class="ops-icons">
      <div v-for="item in rows" :key="item.appCode" class="ops-icon-card">
        <AppMark :code="item.appCode" />
        <div>
          <b>{{ item.appName }}</b>
          <small>{{ item.appCode }}</small>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import AppMark from '../../components/AppMark.vue'
import opsHttp from '../../api/opsHttp'

const rows = ref([])

onMounted(async () => {
  rows.value = await opsHttp.get('/icons')
})
</script>
