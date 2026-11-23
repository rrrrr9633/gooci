<template>
  <div class="ops-page">
    <h2>变更申请</h2>
    <p class="ops-lead">企业只提交工商/银行申请。这里先看待处理清单，过审写主数据下个版本做，不自动通过。</p>
    <el-table :data="rows" stripe>
      <el-table-column prop="enterName" label="企业" min-width="180" />
      <el-table-column prop="kind" label="类型" width="140" />
      <el-table-column prop="remark" label="说明" min-width="260" />
    </el-table>
    <p v-if="!rows.length" class="ops-empty">暂无待处理申请</p>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import opsHttp from '../../api/opsHttp'

const rows = ref([])

onMounted(async () => {
  rows.value = await opsHttp.get('/changes')
})
</script>
