<template>
  <div class="schedule-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>我的排班</span>
          <el-button type="warning" size="small" @click="showTransfer = true">申请调班</el-button>
        </div>
      </template>

      <!-- 周视图 -->
      <el-form :inline="true" style="margin-bottom:12px">
        <el-form-item label="查看周">
          <el-date-picker v-model="weekStart" type="week" format="YYYY 第 ww 周" placeholder="选择周" @change="loadSchedule" />
        </el-form-item>
      </el-form>

      <el-table :data="scheduleList" stripe v-loading="loading" border>
        <el-table-column prop="scheduleDate" label="日期" width="120" />
        <el-table-column prop="deptName" label="科室" width="120" />
        <el-table-column label="时段" width="200">
          <template #default="{ row }">
            <el-tag v-for="s in row.slots" :key="s.id" size="small" style="margin:2px"
              :type="s.remaining === s.maxCount ? 'info' : s.remaining === 0 ? 'danger' : 'success'">
              {{ { 1:'上午', 2:'下午', 3:'晚上' }[s.timePeriod] }} {{ s.remaining }}/{{ s.maxCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '停诊' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 调班记录 -->
    <el-card shadow="never" style="margin-top:16px">
      <template #header><span>调班记录</span></template>
      <el-table :data="transfers" stripe>
        <el-table-column prop="originalDate" label="原日期" width="120" />
        <el-table-column prop="targetDate" label="目标日期" width="120" />
        <el-table-column prop="reason" label="原因" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="{ 0:'warning', 1:'success', 2:'danger' }[row.status]" size="small">
              {{ { 0:'待审批', 1:'已通过', 2:'已拒绝' }[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="170">
          <template #default="{ row }">{{ row.createdAt?.replace('T',' ')?.substring(0,16) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="transfers.length === 0" description="暂无调班记录" :image-size="48" />
    </el-card>

    <!-- 调班申请弹窗 -->
    <el-dialog v-model="showTransfer" title="申请调班" width="420px">
      <el-form :rules="transferRules" ref="transferFormRef" label-width="80px">
        <el-form-item label="原日期" prop="originalDate">
          <el-date-picker v-model="transferForm.originalDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="目标日期" prop="targetDate">
          <el-date-picker v-model="transferForm.targetDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="调班原因" prop="reason">
          <el-input v-model="transferForm.reason" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTransfer = false">取消</el-button>
        <el-button type="primary" @click="submitTransfer" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const scheduleList = ref([])
const loading = ref(false)
const weekStart = ref(null)
const transfers = ref([])
const showTransfer = ref(false)
const submitting = ref(false)
const transferForm = ref({ originalDate: '', targetDate: '', reason: '' })
const transferFormRef = ref(null)
const transferRules = {
  originalDate: [{ required: true, message: '请选择原日期', trigger: 'change' }],
  targetDate: [{ required: true, message: '请选择目标日期', trigger: 'change' }],
  reason: [{ required: true, message: '请输入调班原因', trigger: 'blur' }]
}

async function loadSchedule() {
  loading.value = true
  try {
    const params = {}
    if (weekStart.value) {
      const d = new Date(weekStart.value)
      const day = d.getDay() || 7
      d.setDate(d.getDate() - day + 1)
      params.startDate = d.toISOString().split('T')[0]
      const end = new Date(d)
      end.setDate(end.getDate() + 6)
      params.endDate = end.toISOString().split('T')[0]
    }
    const { data } = await request.get('/medical/schedule/my', { params })
    scheduleList.value = data || []
  } finally { loading.value = false }
}

async function loadTransfers() {
  try {
    const { data } = await request.get('/admin/schedule/transfer', { params: { status: 0 } })
    transfers.value = data || []
  } catch (e) { console.warn(e) }
}

async function submitTransfer() {
  await transferFormRef.value.validate()
  submitting.value = true
  try {
    await request.post('/admin/schedule/transfer', transferForm.value)
    ElMessage.success('调班申请已提交')
    showTransfer.value = false
    loadTransfers()
  } catch (e) {
    ElMessage.error('提交失败')
  } finally { submitting.value = false }
}

onMounted(() => { loadSchedule(); loadTransfers() })
</script>

<style scoped>
.schedule-page { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

/* 响应式 */
@media (max-width: 768px) {
  .schedule-page { padding: 12px; }
  .card-header { flex-direction: column; align-items: flex-start; gap: 8px; }
}
</style>
