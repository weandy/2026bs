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
      { path: 'queue-progress', name: 'ResidentQueueProgress', component: () => import('@/views/resident/appointment/QueueProgressPage.vue'), meta: { title: '候诊进度' } },
      { path: 'visit-records', name: 'ResidentVisitRecords', component: () => import('@/views/resident/visit-record/VisitRecordPage.vue'), meta: { title: '就诊记录' } },
      { path: 'health-record', name: 'ResidentHealthRecord', component: () => import('@/views/resident/health-record/HealthRecordPage.vue'), meta: { title: '我的档案' } },
      { path: 'vaccine', name: 'ResidentVaccine', component: () => import('@/views/resident/vaccine/VaccinePage.vue'), meta: { title: '疫苗接种' } },
      { path: 'family', name: 'ResidentFamily', component: () => import('@/views/resident/family/FamilyPage.vue'), meta: { title: '家庭成员' } },
      { path: 'profile', name: 'ResidentProfile', component: () => import('@/views/resident/ProfilePage.vue'), meta: { title: '个人中心' } }
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
      { path: 'my-schedule', name: 'MedicalMySchedule', component: () => import('@/views/medical/schedule/MySchedulePage.vue'), meta: { title: '我的排班' } }
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
      { path: 'staff', name: 'AdminStaff', component: () => import('@/views/admin/user/StaffManagePage.vue'), meta: { title: '用户管理' } },
      { path: 'schedule', name: 'AdminSchedule', component: () => import('@/views/admin/schedule/ScheduleManagePage.vue'), meta: { title: '排班管理' } },
      { path: 'dept', name: 'AdminDept', component: () => import('@/views/admin/dept/DeptManagePage.vue'), meta: { title: '科室管理' } },
      { path: 'resident', name: 'AdminResident', component: () => import('@/views/admin/resident/ResidentManagePage.vue'), meta: { title: '居民管理' } },
      { path: 'contract', name: 'AdminContract', component: () => import('@/views/admin/contract/ContractManagePage.vue'), meta: { title: '签约管理' } }
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
    const domain = userInfo.domain || ''   // 'resident' | 'admin'
    const role = userInfo.roleCode || userInfo.role || ''

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
