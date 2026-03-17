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
      { path: 'health-record', name: 'ResidentHealthRecord', component: () => import('@/views/resident/health-record/HealthRecordPage.vue'), meta: { title: '健康档案' } },
      { path: 'visit-records', name: 'ResidentVisitRecords', component: () => import('@/views/resident/visit-record/VisitRecordPage.vue'), meta: { title: '就诊记录' } },
      { path: 'vaccine', name: 'ResidentVaccine', component: () => import('@/views/resident/vaccine/VaccinePage.vue'), meta: { title: '疫苗接种' } },
      { path: 'message', name: 'ResidentMessage', component: () => import('@/views/resident/message/MessagePage.vue'), meta: { title: '消息通知' } },
      { path: 'profile', name: 'ResidentProfile', component: () => import('@/views/resident/ProfilePage.vue'), meta: { title: '个人中心' } },
      { path: 'queue-progress', name: 'ResidentQueueProgress', component: () => import('@/views/resident/appointment/QueueProgressPage.vue'), meta: { title: '候诊进度' } },
      { path: 'family', name: 'ResidentFamily', component: () => import('@/views/resident/family/FamilyPage.vue'), meta: { title: '家庭成员' } },
      { path: 'article', name: 'ResidentArticle', component: () => import('@/views/resident/article/ArticleListPage.vue'), meta: { title: '健康资讯' } },
      { path: 'follow-up', name: 'ResidentFollowUp', component: () => import('@/views/resident/follow-up/FollowUpPage.vue'), meta: { title: '我的随访' } }
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
      { path: 'prescription', name: 'MedicalPrescription', component: () => import('@/views/medical/prescription/PrescriptionPage.vue'), meta: { title: '处方管理' } },
      { path: 'record-manage', name: 'MedicalRecordManage', component: () => import('@/views/medical/record/RecordManagePage.vue'), meta: { title: '档案管理' } },
      { path: 'follow-up', name: 'MedicalFollowUp', component: () => import('@/views/medical/followup/FollowUpPage.vue'), meta: { title: '随访公卫' } },
      { path: 'dispense', name: 'MedicalDispense', component: () => import('@/views/medical/dispense/DispensePage.vue'), meta: { title: '药房发药' } },
      { path: 'vaccination', name: 'MedicalVaccination', component: () => import('@/views/medical/vaccination/VaccinationManagePage.vue'), meta: { title: '接种管理' } },
      { path: 'my-schedule', name: 'MedicalMySchedule', component: () => import('@/views/medical/schedule/MySchedulePage.vue'), meta: { title: '我的排班' } },
      { path: 'my-stats', name: 'MedicalMyStats', component: () => import('@/views/medical/stats/MyStatsPage.vue'), meta: { title: '工作量统计' } },
      { path: 'referral', name: 'MedicalReferral', component: () => import('@/views/medical/referral/ReferralPage.vue'), meta: { title: '转诊管理' } },
      { path: 'consultation', name: 'MedicalConsultation', component: () => import('@/views/medical/consultation/ConsultationPage.vue'), meta: { title: '会诊管理' } }
    ]
  },
  // 管理员端
  {
    path: '/admin',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { domain: 'admin', roles: ['admin'] },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/dashboard/DashboardPage.vue'), meta: { title: '管理首页' } },
      { path: 'staff', name: 'AdminStaff', component: () => import('@/views/admin/user/StaffManagePage.vue'), meta: { title: '用户管理' } },
      { path: 'schedule', name: 'AdminSchedule', component: () => import('@/views/admin/schedule/ScheduleManagePage.vue'), meta: { title: '排班管理' } },
      { path: 'drug', name: 'AdminDrug', component: () => import('@/views/admin/drug/DrugStockPage.vue'), meta: { title: '药品库存' } },
      { path: 'config', name: 'AdminConfig', component: () => import('@/views/admin/config/SysConfigPage.vue'), meta: { title: '系统配置' } },
      { path: 'audit-log', name: 'AdminAuditLog', component: () => import('@/views/admin/audit/AuditLogPage.vue'), meta: { title: '审计日志' } },
      { path: 'dept', name: 'AdminDept', component: () => import('@/views/admin/dept/DeptManagePage.vue'), meta: { title: '科室管理' } },
      { path: 'vaccine-stock', name: 'AdminVaccineStock', component: () => import('@/views/admin/vaccine/VaccineStockPage.vue'), meta: { title: '疫苗库存' } },
      { path: 'dict', name: 'AdminDict', component: () => import('@/views/admin/dict/DictManagePage.vue'), meta: { title: '数据字典' } },
      { path: 'drug-log', name: 'AdminDrugLog', component: () => import('@/views/admin/drug/DrugLogPage.vue'), meta: { title: '出入库日志' } },
      { path: 'schedule-transfer', name: 'AdminScheduleTransfer', component: () => import('@/views/admin/schedule/ScheduleTransferPage.vue'), meta: { title: '调班审批' } },
      { path: 'resident', name: 'AdminResident', component: () => import('@/views/admin/resident/ResidentManagePage.vue'), meta: { title: '居民管理' } },
      { path: 'contract', name: 'AdminContract', component: () => import('@/views/admin/contract/ContractManagePage.vue'), meta: { title: '签约管理' } },
      { path: 'article', name: 'AdminArticle', component: () => import('@/views/admin/article/ArticleManagePage.vue'), meta: { title: '文章管理' } },
      { path: 'performance', name: 'AdminPerformance', component: () => import('@/views/admin/performance/PerformancePage.vue'), meta: { title: '医生绩效' } }
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
