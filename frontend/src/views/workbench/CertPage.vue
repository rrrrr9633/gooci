<template>
  <div class="org-page">
    <div style="display:flex;justify-content:flex-end">
      <button class="org-link" type="button">实名认证信息</button>
    </div>
    <div class="org-cert">
      <div>
        <h3>注册会员</h3>
        <p>您已成为 GOOCI 网站用户。</p>
        <p>注册时间：{{ cert.registerTime || '—' }}</p>
      </div>
      <div>
        <h3>实名认证会员</h3>
        <p>
          您的工商信息{{ cert.bizPassed === '1' ? '已通过审核' : '尚未通过审核' }}
          <button class="org-link" type="button" @click="$router.push('/workbench/org/cert/biz')">变更工商认证</button>
        </p>
        <p>
          您的银行账号{{ cert.bankPassed === '1' ? '已通过审核' : '尚未通过审核' }}
          <button class="org-link" type="button" @click="$router.push('/workbench/org/cert/bank')">变更银行账户</button>
        </p>
        <p>认证企业名称：{{ cert.certEnterName || '—' }}</p>
      </div>
    </div>
    <div class="org-line">
      <i class="org-dot left" />
      <i class="org-dot right" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../../api/http'

const cert = ref({
  registerTime: '',
  bizPassed: '0',
  bankPassed: '0',
  certEnterName: ''
})

onMounted(async () => {
  cert.value = await http.get('/console/org/cert')
})
</script>
