<template>
  <div class="schedule-mgmt">
    <div class="filter-bar">
      <el-select v-model="deptFilter" placeholder="选择科室" clearable style="width: 160px;" @change="loadSchedules">
        <el-option v-for="d in departments" :key="d.code" :label="d.name" :value="d.code" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束" @change="loadSchedules" />
      <el-button type="primary" @click="dialogVisible = true">新增排班</el-button>
    </div>

    <el-table :data="schedules" border stripe v-loading="loading">
      <el-table-column prop="staffName" label="医生" width="100" />
      <el-table-column prop="deptName" label="科室" />
      <el-table-column prop="scheduleDate" label="日期" width="120" />
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

    <el-pagination v-model:current-page="page" :page-size="20" :total="total" layout="total, prev, pager, next"
                   style="margin-top: 16px;" @current-change="loadSchedules" />

    <el-dialog v-model="dialogVisible" title="新增排班" width="500px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="医生ID" prop="staffId"><el-input v-model="form.staffId" /></el-form-item>
        <el-form-item label="医生姓名"><el-input v-model="form.staffName" /></el-form-item>
        <el-form-item label="科室编码" prop="deptCode"><el-input v-model="form.deptCode" /></el-form-item>
        <el-form-item label="科室名称"><el-input v-model="form.deptName" /></el-form-item>
        <el-form-item label="排班日期" prop="scheduleDate"><el-date-picker v-model="form.scheduleDate" type="date" /></el-form-item>
        <el-form-item label="上午号源"><el-input-number v-model="form.amQuota" :min="0" :max="50" /></el-form-item>
        <el-form-item label="下午号源"><el-input-number v-model="form.pmQuota" :min="0" :max="50" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSchedule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const departments = [
  { code: 'QKMZ', name: '全科门诊' }, { code: 'ZYMZ', name: '中医门诊' },
  { code: 'KQMZ', name: '口腔门诊' }, { code: 'FKMZ', name: '妇科门诊' },
  { code: 'EKMZ', name: '儿科门诊' }, { code: 'YFJZ', name: '预防接种' }
]

const schedules = ref([])
const page = ref(1)
const total = ref(0)
const deptFilter = ref(null)
const dateRange = ref(null)
const loading = ref(false)
const form = reactive({ staffId: '', staffName: '', deptCode: '', deptName: '', scheduleDate: null, amQuota: 10, pmQuota: 10 })
const formRef = ref(null)
const formRules = {
  staffId: [{ required: true, message: '请输入医生ID', trigger: 'blur' }],
  deptCode: [{ required: true, message: '请输入科室编码', trigger: 'blur' }],
  scheduleDate: [{ required: true, message: '请选择排班日期', trigger: 'change' }]
}

async function loadSchedules() {
  const params = { page: page.value, size: 20 }
  if (deptFilter.value) params.deptCode = deptFilter.value
  if (dateRange.value?.[0]) params.from = dateRange.value[0].toISOString().split('T')[0]
  if (dateRange.value?.[1]) params.to = dateRange.value[1].toISOString().split('T')[0]
  const res = await request.get('/admin/schedule', { params })
  schedules.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function saveSchedule() {
  await formRef.value.validate()
  const slots = []
  if (form.amQuota > 0) slots.push({ timePeriod: 1, totalQuota: form.amQuota })
  if (form.pmQuota > 0) slots.push({ timePeriod: 2, totalQuota: form.pmQuota })
  await request.post('/admin/schedule', {
    staffId: form.staffId, staffName: form.staffName,
    deptCode: form.deptCode, deptName: form.deptName,
    scheduleDate: form.scheduleDate?.toISOString().split('T')[0],
    slots
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

onMounted(() => loadSchedules())
</script>

<style scoped>
.schedule-mgmt { padding: 20px; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; }

/* 响应式 */
@media (max-width: 768px) {
  .schedule-mgmt { padding: 12px; }
  .filter-bar { flex-direction: column; }
}
</style>
