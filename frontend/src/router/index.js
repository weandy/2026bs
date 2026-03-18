import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false, trickleSpeed: 200 })

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/public/LoginPage.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/screen/:deptCode',
    name: 'PublicScreen',
    component: () => import('@/views/public/PublicScreen.vue'),
    meta: { title: '候诊公屏', public: true }
  },
  // 居民端
  {
    path: '/resident',
    component: () => import('@/components/layout/ResidentLayout.vue'),
    meta: { domain: 'resident' },
    children: [
      { path: '', redirect: '/resident/home' },
      { path: 'home', name: 'ResidentHome', component: () => import('@/views/resident/HomePage.vue'), meta: { title: '首页' } },
      { path: 'appointment', name: 'ResidentAppointment', component: () => import('@/views/resident/appointment/AppointmentPage.vue'), meta: { title: '预约挂号' } },
      { path: 'visit-records', name: 'ResidentVisitRecords', component: () => import('@/views/resident/visit-record/VisitRecordPage.vue'), meta: { title: '就诊记录' } },
      { path: 'health-record', name: 'ResidentHealthRecord', component: () => import('@/views/resident/health-record/HealthRecordPage.vue'), meta: { title: '我的档案' } },
      { path: 'vaccine', name: 'ResidentVaccine', component: () => import('@/views/resident/vaccine/VaccinePage.vue'), meta: { title: '疫苗接种' } },
      { path: 'follow-up', name: 'ResidentFollowUp', component: () => import('@/views/resident/followup/ResidentFollowUpPage.vue'), meta: { title: '签约与随访' } },
      { path: 'family', name: 'ResidentFamily', component: () => import('@/views/resident/family/FamilyPage.vue'), meta: { title: '家庭成员' } },
      { path: 'profile', name: 'ResidentProfile', component: () => import('@/views/resident/ProfilePage.vue'), meta: { title: '个人中心' } },
      { path: 'message', name: 'ResidentMessage', component: () => import('@/views/resident/message/MessagePage.vue'), meta: { title: '消息通知' } }
    ]
  },
  // 医护端
  {
    path: '/medical',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { domain: 'admin', roles: ['doctor', 'nurse'] },
    children: [
      { path: '', redirect: '/medical/workbench' },
      { path: 'workbench', name: 'MedicalWorkbench', component: () => import('@/views/medical/workbench/WorkbenchPage.vue'), meta: { title: '接诊工作台' } },
      { path: 'record-manage', name: 'MedicalRecordManage', component: () => import('@/views/medical/record/RecordManagePage.vue'), meta: { title: '居民档案' } },
      { path: 'follow-up', name: 'MedicalFollowUp', component: () => import('@/views/medical/followup/FollowUpPage.vue'), meta: { title: '签约与随访' } },
      { path: 'vaccination', name: 'MedicalVaccination', component: () => import('@/views/medical/vaccination/VaccinationManagePage.vue'), meta: { title: '接种管理' } },
      { path: 'prescription', name: 'MedicalPrescription', component: () => import('@/views/medical/prescription/PrescriptionPage.vue'), meta: { title: '处方记录' } },
      { path: 'my-schedule', name: 'MedicalMySchedule', component: () => import('@/views/medical/schedule/MySchedulePage.vue'), meta: { title: '我的排班' } },
      { path: 'stats',       name: 'MedicalStats',      component: () => import('@/views/medical/stats/WorkStatsPage.vue'),       meta: { title: '工作统计' } },
      { path: 'profile',     name: 'MedicalProfile',    component: () => import('@/views/medical/profile/MedicalProfilePage.vue'), meta: { title: '个人中心' } }
    ]
  },
  // 管理员端
  {
    path: '/admin',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { domain: 'admin', roles: ['admin'] },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/dashboard/DashboardPage.vue'), meta: { title: '数据看板' } },
      { path: 'staff', name: 'AdminStaff', component: () => import('@/views/admin/user/StaffManagePage.vue'), meta: { title: '医护管理' } },
      { path: 'schedule', name: 'AdminSchedule', component: () => import('@/views/admin/schedule/ScheduleManagePage.vue'), meta: { title: '排班管理' } },
      { path: 'dept', name: 'AdminDept', component: () => import('@/views/admin/dept/DeptManagePage.vue'), meta: { title: '科室管理' } },
      { path: 'resident', name: 'AdminResident', component: () => import('@/views/admin/resident/ResidentManagePage.vue'), meta: { title: '居民管理' } },
      { path: 'contract', name: 'AdminContract', component: () => import('@/views/admin/contract/ContractManagePage.vue'), meta: { title: '签约管理' } },
      { path: 'notice', name: 'AdminNotice', component: () => import('@/views/admin/article/NoticeMangePage.vue'), meta: { title: '公告管理' } },
      { path: 'drug', name: 'AdminDrug', component: () => import('@/views/admin/drug/DrugStockPage.vue'), meta: { title: '药品管理' } },
      { path: 'vaccine-stock', name: 'AdminVaccineStock', component: () => import('@/views/admin/vaccine/VaccineStockPage.vue'), meta: { title: '疫苗库存' } },
      { path: 'audit-log', name: 'AdminAuditLog', component: () => import('@/views/admin/audit/AuditLogPage.vue'), meta: { title: '操作日志' } }
    ]
  },
  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/public/NotFound.vue'),
    meta: { title: '页面不存在', public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  NProgress.start()
  document.title = to.meta.title ? `${to.meta.title} - 社区卫生服务中心` : '社区卫生服务中心'

  if (to.meta.public) {
    next()
    return
  }

  const token = localStorage.getItem('accessToken')
  if (!token) {
    next('/login')
    return
  }

  // 角色域隔离
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    // Bug4修复：domain缺失时根据role推断，防止守卫失效
    let domain = userInfo.domain || ''
    const role = userInfo.roleCode || userInfo.role || ''
    if (!domain) {
      if (role === 'resident') domain = 'resident'
      else if (['admin', 'doctor', 'nurse'].includes(role)) domain = 'admin'
      else domain = 'unknown'
    }

    const path = to.path
    const isResidentRoute = path.startsWith('/resident')
    const isMedicalRoute = path.startsWith('/medical')
    const isAdminRoute = path.startsWith('/admin')

    if (domain === 'resident' && (isMedicalRoute || isAdminRoute)) {
      next('/resident/home')
      return
    }
    if (domain === 'admin' && isResidentRoute) {
      next(role === 'admin' ? '/admin/dashboard' : '/medical/workbench')
      return
    }
    // domain不明时重定向到登录
    if (domain === 'unknown' && (isResidentRoute || isMedicalRoute || isAdminRoute)) {
      next('/login')
      return
    }

    // 角色权限校验：医护角色不可访问管理员路由
    const requiredRoles = to.matched.reduce((roles, record) => {
      return record.meta?.roles || roles
    }, null)
    if (requiredRoles && !requiredRoles.includes(role)) {
      next(role === 'admin' ? '/admin/dashboard' : '/medical/workbench')
      return
    }
  } catch (e) {
    next('/login')
    return
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
