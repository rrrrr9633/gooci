import { createRouter, createWebHistory } from 'vue-router'
import SiteLayout from '../layouts/SiteLayout.vue'
import WorkbenchLayout from '../layouts/WorkbenchLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: SiteLayout,
      children: [
        { path: '', name: 'home', component: () => import('../views/HomePage.vue') }
      ]
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginPage.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterPage.vue')
    },
    {
      path: '/workbench',
      component: WorkbenchLayout,
      meta: { auth: true },
      children: [
        { path: '', redirect: '/workbench/home' },
        { path: 'home', name: 'dashboard', component: () => import('../views/workbench/DashboardPage.vue'), meta: { title: '工作台', auth: true } },
        { path: 'profile', name: 'profile', component: () => import('../views/workbench/ProfilePage.vue'), meta: { title: '个人信息', auth: true } },
        { path: 'org/enterprise', name: 'orgEnterprise', component: () => import('../views/workbench/EnterprisePage.vue'), meta: { title: '企业信息', auth: true } },
        { path: 'org/structure', name: 'orgStructure', component: () => import('../views/workbench/OrgStructurePage.vue'), meta: { title: '企业组织管理', auth: true } },
        { path: 'org/cert', name: 'orgCert', component: () => import('../views/workbench/CertPage.vue'), meta: { title: '实名认证', auth: true } },
        { path: 'org/cert/biz', name: 'orgCertBiz', component: () => import('../views/workbench/BizChangePage.vue'), meta: { title: '工商登记变更', auth: true } },
        { path: 'org/cert/bank', name: 'orgCertBank', component: () => import('../views/workbench/BankChangePage.vue'), meta: { title: '变更银行账户', auth: true } },
        { path: 'org/dept', name: 'orgDept', component: () => import('../views/workbench/DeptPage.vue'), meta: { title: '企业部门管理', auth: true } },
        { path: 'resource/staff', name: 'staff', component: () => import('../views/workbench/StaffPage.vue'), meta: { title: '员工管理', auth: true } },
        { path: 'resource/device', name: 'device', component: () => import('../views/workbench/DevicePage.vue'), meta: { title: '设备管理', auth: true } },
        { path: 'perm/app-func', name: 'appFunc', component: () => import('../views/workbench/AppRolePage.vue'), meta: { title: 'APP功能角色', auth: true, roleKind: 'FUNC' } },
        { path: 'perm/app-data', name: 'appData', component: () => import('../views/workbench/AppRolePage.vue'), meta: { title: 'APP数据角色', auth: true, roleKind: 'DATA' } },
        { path: 'perm/app-auth', name: 'appAuth', component: () => import('../views/workbench/AppAuthPage.vue'), meta: { title: 'APP账号授权', auth: true } },
        { path: 'biz/app', name: 'bizApp', component: () => import('../views/workbench/IndustrialAppPage.vue'), meta: { title: '工业APP', auth: true } },
        { path: 'biz/todo', name: 'bizTodo', component: () => import('../views/workbench/TodoPage.vue'), meta: { title: '待办任务', auth: true } },
        { path: 'biz/inbox', name: 'bizInbox', component: () => import('../views/workbench/InboxPage.vue'), meta: { title: '站内信', auth: true } },
        { path: 'app/:appCode', name: 'appShell', component: () => import('../views/workbench/PlaceholderPage.vue'), meta: { title: '模块', auth: true } },
        { path: 'ext/:appCode', name: 'extModule', component: () => import('../views/workbench/ExtModulePage.vue'), meta: { title: '外接模块', auth: true } }
      ]
    },
    {
      path: '/ops/login',
      name: 'opsLogin',
      component: () => import('../views/ops/OpsLoginPage.vue')
    },
    {
      path: '/ops',
      component: () => import('../layouts/OpsLayout.vue'),
      meta: { ops: true },
      children: [
        { path: '', redirect: '/ops/home' },
        { path: 'home', name: 'opsHome', component: () => import('../views/ops/OpsHomePage.vue'), meta: { ops: true } },
        { path: 'site', name: 'opsSite', component: () => import('../views/ops/OpsSitePage.vue'), meta: { ops: true } },
        { path: 'apps', name: 'opsApps', component: () => import('../views/ops/OpsAppPage.vue'), meta: { ops: true } },
        { path: 'changes', name: 'opsChanges', component: () => import('../views/ops/OpsChangePage.vue'), meta: { ops: true } },
        { path: 'enter/:id', name: 'opsEnter', component: () => import('../views/ops/OpsEnterPage.vue'), meta: { ops: true } }
      ]
    }
  ]
})

router.beforeEach((to) => {
  if (to.meta.ops && !localStorage.getItem('gooci-ops-token')) {
    return { path: '/ops/login', query: { redirect: to.fullPath } }
  }
  const token = localStorage.getItem('gooci-token')
  if (to.meta.auth && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
