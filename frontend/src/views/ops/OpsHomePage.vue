<template>
  <div class="ops-page">
    <h2>企业概览</h2>
    <p class="ops-lead">全部客户企业。点一行看已植入模块，不在企业工作台里做开通。</p>
    <el-table :data="rows" stripe @row-click="openEnter">
      <el-table-column prop="enterName" label="企业" min-width="180" />
      <el-table-column prop="enterCode" label="编码" width="120" />
      <el-table-column prop="regionText" label="地区" min-width="160" />
      <el-table-column label="认证" width="90">
        <template #default="{ row }">{{ row.certStatus === '1' ? '已认证' : '未认证' }}</template>
      </el-table-column>
      <el-table-column label="已植入" width="90" prop="appCount" />
      <el-table-column label="对客可见" width="100" prop="visibleAppCount" />
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import opsHttp from '../../api/opsHttp'

const router = useRouter()
const rows = ref([])

onMounted(async () => {
  rows.value = await opsHttp.get('/enters')
})

function openEnter(row) {
  router.push(`/ops/enter/${row.id}`)
}
</script>
