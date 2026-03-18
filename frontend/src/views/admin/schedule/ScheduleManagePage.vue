<template>
  <div class="schedule-mgmt">
    <el-tabs v-model="activeTab">
      <!-- Tab 1: 排班列表 -->
      <el-tab-pane label="排班管理" name="schedule">
        <div class="filter-bar">
          <el-select v-model="deptFilter" placeholder="选择科室" clearable style="width: 160px;" @change="loadSchedules">
            <el-option v-for="d in departments" :key="d.code" :label="d.name" :value="d.code" />
          </el-select>
          <el-date-picker v-model="dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束" @change="loadSchedules" />
          <el-button type="primary" @click="dialogVisible = true">新增排班</el-button>
        </div>

        <el-table :data="schedules" border stripe v-loading="loading">
          <el-table-column prop="staffName"    label="医生"  width="100" />
          <el-table-column prop="deptName"     label="科室" />
          <el-table-column prop="scheduleDate" label="日期"  width="120" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.isStopped ? 'danger' : 'success'" size="small">
                {{ row.isStopped ? '停诊' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button v-if="!row.isStopped" type="danger" text size="small" @click="stopSchedule(row.id)">停诊</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination v-model:current-page="page" :page-size="20" :total="total"
          layout="total, prev, pager, next" style="margin-top: 16px;" @current-change="loadSchedules" />
      </el-tab-pane>

      <!-- Tab 2: 换班申请 -->
      <el-tab-pane name="transfer">
        <template #label>
          换班申请
          <el-badge v-if="pendingCount > 0" :value="pendingCount" type="danger" style="margin-left:4px" />
        </template>

        <div class="filter-bar">
          <el-select v-model="transferStatus" placeholder="审批状态" clearable style="width:130px" @change="loadTransfers">
            <el-option label="待审批" :value="0" />
            <el-option label="已同意" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
          <el-button @click="loadTransfers">刷新</el-button>
        </div>

        <el-table :data="transfers" border stripe v-loading="tLoading" empty-text="暂无换班申请">
          <el-table-column prop="applicantName" label="申请人"  width="90" />
          <el-table-column prop="originalDate"  label="原班次"  width="120" />
          <el-table-column prop="targetDate"    label="调至日期" width="120" />
          <el-table-column prop="targetName"    label="代班人"  width="90" />
          <el-table-column prop="reason"        label="申请原因" min-width="180" show-overflow-tooltip />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="申请时间" width="145" />
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 0">
                <el-button type="success" text size="small" @click="review(row, 1)">同意</el-button>
                <el-button type="danger"  text size="small" @click="review(row, 2)">拒绝</el-button>
              </template>
              <span v-else class="muted-text">已处理</span>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination v-model:current-page="tPage" :page-size="10" :total="tTotal"
          layout="total, prev, pager, next" small style="margin-top:14px;justify-content:flex-end"
          @current-change="loadTransfers" />
      </el-tab-pane>
    </el-tabs>

    <!-- 新增排班弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增排班" width="500px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="医生ID"    prop="staffId"><el-input v-model="form.staffId" /></el-form-item>
        <el-form-item label="医生姓名">                <el-input v-model="form.staffName" /></el-form-item>
        <el-form-item label="科室编码"  prop="deptCode"><el-input v-model="form.deptCode" /></el-form-item>
        <el-form-item label="科室名称">                <el-input v-model="form.deptName" /></el-form-item>
        <el-form-item label="排班日期"  prop="scheduleDate"><el-date-picker v-model="form.scheduleDate" type="date" /></el-form-item>
        <el-form-item label="上午号源"><el-input-number v-model="form.amQuota" :min="0" :max="50" /></el-form-item>
        <el-form-item label="下午号源"><el-input-number v-model="form.pmQuota" :min="0" :max="50" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSchedule">保存</el-button>
      </template>
    </el-dialog>

    <!-- 审批备注弹窗 -->
    <el-dialog v-model="reviewDialog.show" :title="reviewDialog.action === 1 ? '同意换班' : '拒绝换班'" width="400px">
      <el-form label-width="70px">
        <el-form-item label="审批意见"><el-input v-model="reviewDialog.comment" type="textarea" :rows="3" placeholder="可不填" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialog.show = false">取消</el-button>
        <el-button :type="reviewDialog.action === 1 ? 'success' : 'danger'" @click="submitReview">确认{{ reviewDialog.action === 1 ? '同意' : '拒绝' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('schedule')

const departments = [
  { code: 'QKMZ', name: '全科门诊' }, { code: 'ZYMZ', name: '中医门诊' },
  { code: 'KQMZ', name: '口腔门诊' }, { code: 'FKMZ', name: '妇科门诊' },
  { code: 'EKMZ', name: '儿科门诊' }, { code: 'YFJZ', name: '预防接种' }
]

// ── 排班 ──
const schedules    = ref([])
const page         = ref(1)
const total        = ref(0)
const deptFilter   = ref(null)
const dateRange    = ref(null)
const loading      = ref(false)
const dialogVisible = ref(false)
const form = reactive({ staffId: '', staffName: '', deptCode: '', deptName: '', scheduleDate: null, amQuota: 10, pmQuota: 10 })
const formRef  = ref(null)
const formRules = {
  staffId:      [{ required: true, message: '请输入医生ID', trigger: 'blur' }],
  deptCode:     [{ required: true, message: '请输入科室编码', trigger: 'blur' }],
  scheduleDate: [{ required: true, message: '请选择排班日期', trigger: 'change' }]
}

async function loadSchedules() {
  loading.value = true
  try {
    const params = { page: page.value, size: 20 }
    if (deptFilter.value)     params.deptCode = deptFilter.value
    if (dateRange.value?.[0]) params.from = dateRange.value[0].toISOString().split('T')[0]
    if (dateRange.value?.[1]) params.to   = dateRange.value[1].toISOString().split('T')[0]
    const res = await request.get('/admin/schedule', { params })
    schedules.value = res.data?.records || []
    total.value     = res.data?.total   || 0
  } finally { loading.value = false }
}

async function saveSchedule() {
  await formRef.value.validate()
  const slots = []
  if (form.amQuota > 0) slots.push({ timePeriod: 1, totalQuota: form.amQuota })
  if (form.pmQuota > 0) slots.push({ timePeriod: 2, totalQuota: form.pmQuota })
  await request.post('/admin/schedule', {
    staffId: form.staffId, staffName: form.staffName,
    deptCode: form.deptCode, deptName: form.deptName,
    scheduleDate: form.scheduleDate?.toISOString().split('T')[0], slots
  })
  ElMessage.success('排班已创建')
  dialogVisible.value = false
  loadSchedules()
}

async function stopSchedule(id) {
  const { value } = await ElMessageBox.prompt('请输入停诊原因', '停诊确认')
  await request.put(`/admin/schedule/${id}/stop`, { reason: value })
  ElMessage.success('已停诊')
  loadSchedules()
}

// ── 换班申请 ──
const transfers      = ref([])
const tLoading       = ref(false)
const tPage          = ref(1)
const tTotal         = ref(0)
const transferStatus = ref(null)
const reviewDialog   = reactive({ show: false, id: null, action: 1, comment: '' })

const pendingCount = computed(() => transfers.value.filter(t => t.status === 0).length)

const statusLabel = (s) => ({ 0: '待审批', 1: '已同意', 2: '已拒绝' }[s] ?? '未知')
const statusType  = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] ?? '')

async function loadTransfers() {
  tLoading.value = true
  try {
    const params = { page: tPage.value, size: 10 }
    if (transferStatus.value !== null && transferStatus.value !== '') params.status = transferStatus.value
    const res = await request.get('/admin/schedule/transfer', { params })
    const d = res.data
    if (Array.isArray(d)) { transfers.value = d; tTotal.value = d.length }
    else { transfers.value = d?.records || d?.list || []; tTotal.value = d?.total || transfers.value.length }
  } finally { tLoading.value = false }
}

function review(row, action) {
  reviewDialog.id      = row.id
  reviewDialog.action  = action
  reviewDialog.comment = ''
  reviewDialog.show    = true
}

async function submitReview() {
  await request.put(`/admin/schedule/transfer/${reviewDialog.id}`, {
    status: reviewDialog.action, comment: reviewDialog.comment
  })
  ElMessage.success(reviewDialog.action === 1 ? '已同意换班申请' : '已拒绝换班申请')
  reviewDialog.show = false
  loadTransfers()
}

onMounted(() => { loadSchedules(); loadTransfers() })
</script>

<style scoped>
.schedule-mgmt { padding: 0 }
.filter-bar { display:flex; gap:12px; margin-bottom:16px; flex-wrap:wrap; align-items:center }
.muted-text { color: var(--muted); font-size:12px }
@media (max-width: 768px) {
  .schedule-mgmt { padding: 0 }
  .filter-bar { flex-direction: column }
}
</style>
