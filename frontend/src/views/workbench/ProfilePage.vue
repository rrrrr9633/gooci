<template>
  <div class="profile">
    <section class="card">
      <div class="card-title">个人信息</div>
      <div class="grid">
        <div class="cell">
          <span class="lab">姓名</span>
          <span class="val">{{ profile.displayName || '—' }}</span>
          <button class="link" type="button" @click="openName">修改</button>
        </div>
        <div class="cell">
          <span class="lab">手机号</span>
          <span class="val">{{ profile.mobile || '—' }}</span>
        </div>
        <div class="cell">
          <span class="lab">密码</span>
          <span class="val">************</span>
          <button class="link" type="button" @click="openPassword">修改</button>
        </div>
        <div class="cell">
          <span class="lab">角色</span>
          <span class="val">{{ profile.roleName || '—' }}</span>
          <button class="link" type="button" @click="$router.push('/workbench/org/structure')">管理</button>
        </div>
        <div class="cell">
          <span class="lab">邮箱</span>
          <span class="val" :class="{ mute: !profile.mailAddr }">{{ profile.mailAddr || '请添加邮箱' }}</span>
          <button class="link" type="button" @click="openMail">修改</button>
        </div>
        <div class="cell">
          <span class="lab">所属部门</span>
          <span class="val">{{ deptText }}</span>
          <button class="link" type="button" @click="$router.push('/workbench/org/dept')">管理</button>
        </div>
        <div class="cell wide">
          <span class="lab">公司名称</span>
          <span class="val">{{ profile.enterName || '—' }}</span>
        </div>
      </div>
    </section>

    <section class="card">
      <div class="card-title">
        业务权限
        <small>如您需要开通某个APP应用的访问权限，请联系对应的业务管理员，或企业管理员为您分配</small>
      </div>
      <el-table :data="profile.grants" empty-text="暂无已开通应用">
        <el-table-column prop="appName" label="已开通APP" min-width="200" />
        <el-table-column prop="funcRoleName" label="功能角色" min-width="160" />
        <el-table-column prop="dataRoleName" label="数据角色" min-width="200" />
      </el-table>
    </section>

    <el-dialog v-model="nameOpen" title="修改姓名" width="400px" class="org-pick-dialog" header-class="org-pick-head">
      <el-input v-model="nameDraft" maxlength="100" />
      <template #footer>
        <el-button @click="nameOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveName">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="mailOpen" title="修改邮箱" width="400px" class="org-pick-dialog" header-class="org-pick-head">
      <el-input v-model="mailDraft" maxlength="100" placeholder="留空则清除" />
      <template #footer>
        <el-button @click="mailOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveMail">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pwdOpen" title="修改密码" width="400px" class="org-pick-dialog" header-class="org-pick-head">
      <el-form label-width="80px">
        <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
        <el-form-item label="确认"><el-input v-model="pwdForm.confirm" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import { useSessionStore } from '../../stores/session'

const session = useSessionStore()
const profile = ref({
  displayName: '',
  mobile: '',
  mailAddr: '',
  roleName: '',
  enterName: '',
  deptNames: [],
  grants: []
})
const saving = ref(false)
const nameOpen = ref(false)
const mailOpen = ref(false)
const pwdOpen = ref(false)
const nameDraft = ref('')
const mailDraft = ref('')
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })

const deptText = computed(() => {
  const names = profile.value.deptNames || []
  return names.length ? names.join(',') : '—'
})

async function load() {
  profile.value = await http.get('/console/profile')
}

function openName() {
  nameDraft.value = profile.value.displayName || ''
  nameOpen.value = true
}

function openMail() {
  mailDraft.value = profile.value.mailAddr || ''
  mailOpen.value = true
}

function openPassword() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirm = ''
  pwdOpen.value = true
}

async function saveName() {
  saving.value = true
  try {
    profile.value = await http.post('/console/profile/name', { displayName: nameDraft.value })
    nameOpen.value = false
    await session.loadSession()
    ElMessage.success('已保存')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

async function saveMail() {
  saving.value = true
  try {
    profile.value = await http.post('/console/profile/mail', { mailAddr: mailDraft.value })
    mailOpen.value = false
    ElMessage.success('已保存')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

async function savePassword() {
  if (pwdForm.newPassword !== pwdForm.confirm) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  saving.value = true
  try {
    await http.post('/console/profile/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    pwdOpen.value = false
    ElMessage.success('密码已更新')
  } catch {
    /* http 已提示 */
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.profile { display: flex; flex-direction: column; gap: 16px; }
.card {
  background: #fff;
  border-radius: 4px;
  padding: 18px 22px 8px;
  box-shadow: 0 1px 4px #0000000d;
}
.card-title {
  font-size: 14px;
  color: #667;
  margin-bottom: 8px;
}
.card-title small {
  margin-left: 12px;
  font-size: 12px;
  color: #9aa;
}
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 40px;
}
.cell {
  display: grid;
  grid-template-columns: 88px 1fr auto;
  align-items: center;
  min-height: 52px;
  border-bottom: 1px solid #f2f4f6;
  gap: 8px;
}
.cell.wide { grid-column: 1 / -1; }
.lab { color: #8a97a0; font-size: 13px; }
.val { color: var(--gooci-text); font-size: 14px; }
.val.mute { color: #b0bcc4; }
.link {
  border: 0;
  background: none;
  color: var(--gooci-primary);
  cursor: pointer;
  font-size: 13px;
  padding: 0;
}
.profile :deep(.el-table th.el-table__cell) {
  background: #eef7fb;
  color: #4a6270;
  font-weight: 500;
}
</style>
