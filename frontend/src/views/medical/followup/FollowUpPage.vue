<template>
  <div class="follow-up-page">
    <!-- 签约居民 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header>
        <div class="card-header">
          <span>我的签约居民</span>
          <el-tag size="small" type="info">{{ myResidents.length }} 人</el-tag>
        </div>
      </template>
      <div v-if="myResidents.length === 0" style="color:var(--el-text-color-secondary);text-align:center;padding:12px 0;">暂无签约居民</div>
      <el-table v-else :data="myResidents" stripe size="small">
        <el-table-column prop="residentId" label="居民ID" width="80" />
        <el-table-column prop="teamName" label="团队" />
        <el-table-column prop="startDate" label="签约日期" width="110" />
        <el-table-column prop="endDate" label="到期日期" width="110" />
        <el-table-column label="剩余天数" width="100">
          <template #default="{ row }">
            <span :style="{ color: daysLeft(row.endDate) <= 30 ? '#e74c3c' : '' }">
              {{ daysLeft(row.endDate) }} 天
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 随访计划 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <template #header>
        <div class="card-header">
          <span>慢病随访计划</span>
          <el-button type="primary" size="small" @click="showPlanDialog = true">新建计划</el-button>
        </div>
      </template>
      <el-form :inline="true" @submit.prevent="loadPlans">
        <el-form-item label="慢病类型">
          <el-select v-model="chronicFilter" clearable placeholder="全部">
            <el-option label="高血压" value="hypertension" />
            <el-option label="糖尿病" value="diabetes" />
            <el-option label="冠心病" value="chd" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadPlans">查询</el-button>
          <el-button @click="loadTodayDue">今日到期</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="plans" stripe v-loading="planLoading">
        <el-table-column prop="residentName" label="居民" width="100" />
        <el-table-column prop="chronicType" label="慢病类型" width="100" />
        <el-table-column prop="doctorName" label="责任医生" width="100" />
        <el-table-column prop="frequency" label="频次" width="80">
          <template #default="{ row }">{{ ['', '每月', '每季度', '每半年'][row.frequency] }}</template>
        </el-table-column>
        <el-table-column prop="followUpMethod" label="方式" width="70">
          <template #default="{ row }">{{ ['', '电话', '门诊', '上门'][row.followUpMethod] }}</template>
        </el-table-column>
        <el-table-column prop="nextFollowDate" label="下次随访" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '管理中' : '已停止' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewRecords(row)">记录</el-button>
            <el-button link type="primary" @click="openAddRecord(row)">添加随访</el-button>
            <el-button link type="danger" v-if="row.status === 1" @click="stopPlan(row.id)">停止</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="planPage" :page-size="10" :total="planTotal"
        layout="prev, pager, next" @current-change="loadPlans" style="margin-top:12px;justify-content:flex-end" />
    </el-card>

    <!-- 公卫服务记录 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>公卫服务记录</span>
          <el-button type="primary" size="small" @click="showPhDialog = true">新建记录</el-button>
        </div>
      </template>
      <el-form :inline="true" @submit.prevent="loadPh">
        <el-form-item label="服务类型">
          <el-select v-model="phTypeFilter" clearable placeholder="全部">
            <el-option label="老年人体检" value="elder_exam" />
            <el-option label="产前检查" value="prenatal" />
            <el-option label="儿童保健" value="child_care" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="loadPh">查询</el-button></el-form-item>
      </el-form>
      <el-table :data="phRecords" stripe v-loading="phLoading">
        <el-table-column prop="residentName" label="居民" width="100" />
        <el-table-column prop="serviceType" label="服务类型" width="120" />
        <el-table-column prop="serviceDate" label="服务日期" width="120" />
        <el-table-column prop="staffName" label="服务人员" width="100" />
        <el-table-column prop="conclusion" label="结论" width="80">
          <template #default="{ row }">
            <el-tag :type="row.conclusion === 1 ? 'success' : 'warning'" size="small">{{ row.conclusion === 1 ? '正常' : '异常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="conclusionDesc" label="描述" />
        <el-table-column prop="referralNeeded" label="转诊" width="60">
          <template #default="{ row }">{{ row.referralNeeded ? '是' : '否' }}</template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="phPage" :page-size="10" :total="phTotal"
        layout="prev, pager, next" @current-change="loadPh" style="margin-top:12px;justify-content:flex-end" />
    </el-card>

    <!-- 新建计划对话框 -->
    <el-dialog v-model="showPlanDialog" title="新建随访计划" width="500px">
      <el-form :model="planForm" :rules="planRules" ref="planFormRef" label-width="100px">
        <el-form-item label="居民ID" prop="residentId"><el-input-number v-model="planForm.residentId" :min="1" /></el-form-item>
        <el-form-item label="居民姓名"><el-input v-model="planForm.residentName" /></el-form-item>
        <el-form-item label="慢病类型" prop="chronicType">
          <el-select v-model="planForm.chronicType">
            <el-option label="高血压" value="hypertension" />
            <el-option label="糖尿病" value="diabetes" />
            <el-option label="冠心病" value="chd" />
          </el-select>
        </el-form-item>
        <el-form-item label="责任医生ID"><el-input-number v-model="planForm.doctorId" :min="1" /></el-form-item>
        <el-form-item label="医生姓名"><el-input v-model="planForm.doctorName" /></el-form-item>
        <el-form-item label="频次">
          <el-select v-model="planForm.frequency">
            <el-option :label="'每月'" :value="1" />
            <el-option :label="'每季度'" :value="2" />
            <el-option :label="'每半年'" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="方式">
          <el-select v-model="planForm.followUpMethod">
            <el-option :label="'电话'" :value="1" />
            <el-option :label="'门诊'" :value="2" />
            <el-option :label="'上门'" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="下次随访"><el-date-picker v-model="planForm.nextFollowDate" type="date" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPlanDialog = false">取消</el-button>
        <el-button type="primary" @click="createPlan">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加随访记录对话框 -->
    <el-dialog v-model="showRecordDialog" title="添加随访记录" width="550px">
      <el-form :model="recordForm" :rules="recordRules" ref="recordFormRef" label-width="100px">
        <el-form-item label="收缩压" prop="systolicBp"><el-input-number v-model="recordForm.systolicBp" :min="60" :max="300" /></el-form-item>
        <el-form-item label="舒张压"><el-input-number v-model="recordForm.diastolicBp" :min="30" :max="200" /></el-form-item>
        <el-form-item label="空腹血糖"><el-input-number v-model="recordForm.fastingGlucose" :precision="2" :step="0.1" /></el-form-item>
        <el-form-item label="用药依从">
          <el-select v-model="recordForm.medicationCompliance">
            <el-option :label="'规律'" :value="1" />
            <el-option :label="'间断'" :value="2" />
            <el-option :label="'不服药'" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="健康指导"><el-input v-model="recordForm.healthGuidance" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="下次随访"><el-date-picker v-model="recordForm.nextFollowDate" type="date" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRecordDialog = false">取消</el-button>
        <el-button type="primary" @click="addRecord">保存</el-button>
      </template>
    </el-dialog>

    <!-- 随访记录查看 -->
    <el-dialog v-model="showRecordList" title="随访记录" width="700px">
      <el-table :data="records" stripe>
        <el-table-column prop="followDate" label="日期" width="120" />
        <el-table-column prop="staffName" label="人员" width="80" />
        <el-table-column prop="systolicBp" label="收缩压" width="70" />
        <el-table-column prop="diastolicBp" label="舒张压" width="70" />
        <el-table-column prop="fastingGlucose" label="血糖" width="60" />
        <el-table-column prop="healthGuidance" label="指导" />
      </el-table>
      <el-divider v-if="records.length > 0">趋势图表</el-divider>
      <v-chart v-if="records.length > 0" :option="trendChartOption" style="height: 280px;" autoresize />
    </el-dialog>

    <!-- 新建公卫记录 -->
    <el-dialog v-model="showPhDialog" title="新建公卫记录" width="500px">
      <el-form :model="phForm" :rules="phRules" ref="phFormRef" label-width="100px">
        <el-form-item label="居民ID" prop="residentId"><el-input-number v-model="phForm.residentId" :min="1" /></el-form-item>
        <el-form-item label="居民姓名"><el-input v-model="phForm.residentName" /></el-form-item>
        <el-form-item label="服务类型" prop="serviceType">
          <el-select v-model="phForm.serviceType">
            <el-option label="老年人体检" value="elder_exam" />
            <el-option label="产前检查" value="prenatal" />
            <el-option label="儿童保健" value="child_care" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务日期"><el-date-picker v-model="phForm.serviceDate" type="date" /></el-form-item>
        <el-form-item label="结论">
          <el-select v-model="phForm.conclusion">
            <el-option :label="'正常'" :value="1" />
            <el-option :label="'异常'" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="phForm.conclusionDesc" type="textarea" /></el-form-item>
        <el-form-item label="建议转诊"><el-switch v-model="phForm.referralNeeded" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPhDialog = false">取消</el-button>
        <el-button type="primary" @click="createPh">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

// 随访计划
const plans = ref([])
const planLoading = ref(false)
const planPage = ref(1)
const planTotal = ref(0)
const chronicFilter = ref('')
const showPlanDialog = ref(false)
const planForm = ref({ residentId: null, residentName: '', chronicType: '', doctorId: null, doctorName: '', frequency: 1, followUpMethod: 1, nextFollowDate: '' })
const planFormRef = ref(null)
const planRules = {
  residentId: [{ required: true, message: '请输入居民ID', trigger: 'blur' }],
  chronicType: [{ required: true, message: '请选择慢病类型', trigger: 'change' }]
}

const loadPlans = async () => {
  planLoading.value = true
  try {
    const params = { page: planPage.value, size: 10 }
    if (chronicFilter.value) params.chronicType = chronicFilter.value
    const { data } = await request.get('/medical/follow-up/plan', { params })
    plans.value = data.records || []
    planTotal.value = data.total || 0
  } finally { planLoading.value = false }
}

const loadTodayDue = async () => {
  const { data } = await request.get('/medical/follow-up/plan/today-due')
  plans.value = data || []
  planTotal.value = plans.value.length
}

const createPlan = async () => {
  await planFormRef.value.validate()
  await request.post('/medical/follow-up/plan', planForm.value)
  ElMessage.success('创建成功')
  showPlanDialog.value = false
  loadPlans()
}

const stopPlan = async (id) => {
  await request.put(`/medical/follow-up/plan/${id}/stop`)
  ElMessage.success('已停止')
  loadPlans()
}

// 随访记录
const showRecordDialog = ref(false)
const showRecordList = ref(false)
const records = ref([])
const currentPlan = ref(null)
const recordForm = ref({ systolicBp: null, diastolicBp: null, fastingGlucose: null, medicationCompliance: 1, healthGuidance: '', nextFollowDate: '' })
const recordFormRef = ref(null)
const recordRules = {
  systolicBp: [{ required: true, message: '请输入收缩压', trigger: 'blur' }]
}

const viewRecords = async (plan) => {
  currentPlan.value = plan
  const [recRes, trendRes] = await Promise.all([
    request.get(`/medical/follow-up/record/${plan.id}`),
    request.get(`/medical/follow-up/trend/${plan.id}`, { params: { limit: 50 } })
  ])
  records.value = recRes.data || []
  trendData.value = trendRes.data || []
  showRecordList.value = true
}

const openAddRecord = (plan) => {
  currentPlan.value = plan
  recordForm.value = { systolicBp: null, diastolicBp: null, fastingGlucose: null, medicationCompliance: 1, healthGuidance: '', nextFollowDate: '' }
  showRecordDialog.value = true
}

const addRecord = async () => {
  await recordFormRef.value.validate()
  const payload = { ...recordForm.value, planId: currentPlan.value.id, residentId: currentPlan.value.residentId, followDate: new Date().toISOString().slice(0, 10), followMethod: currentPlan.value.followUpMethod }
  await request.post('/medical/follow-up/record', payload)
  ElMessage.success('添加成功')
  showRecordDialog.value = false
  loadPlans()
}

// 公卫
const phRecords = ref([])
const phLoading = ref(false)
const phPage = ref(1)
const phTotal = ref(0)
const phTypeFilter = ref('')
const showPhDialog = ref(false)
const phForm = ref({ residentId: null, residentName: '', serviceType: '', serviceDate: '', conclusion: 1, conclusionDesc: '', referralNeeded: 0, indicators: '{}' })
const phFormRef = ref(null)
const phRules = {
  residentId: [{ required: true, message: '请输入居民ID', trigger: 'blur' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }]
}

const loadPh = async () => {
  phLoading.value = true
  try {
    const params = { page: phPage.value, size: 10 }
    if (phTypeFilter.value) params.serviceType = phTypeFilter.value
    const { data } = await request.get('/medical/public-health', { params })
    phRecords.value = data.records || []
    phTotal.value = data.total || 0
  } finally { phLoading.value = false }
}

// 趋势图表
const trendData = ref([])
const trendChartOption = computed(() => {
  const sortedData = [...trendData.value].sort((a, b) => (a.followDate || '').localeCompare(b.followDate || ''))
  const dates = sortedData.map(r => r.followDate || '')
  const systolic = sortedData.map(r => r.systolicBp)
  const diastolic = sortedData.map(r => r.diastolicBp)
  const glucose = sortedData.map(r => r.fastingGlucose)
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['收缩压', '舒张压', '空腹血糖'] },
    grid: { left: 50, right: 30, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates.map(d => d.slice(5)) },
    yAxis: [
      { type: 'value', name: 'mmHg', position: 'left' },
      { type: 'value', name: 'mmol/L', position: 'right' }
    ],
    series: [
      { name: '收缩压', type: 'line', data: systolic, smooth: true, lineStyle: { color: 'var(--danger)', width: 2 }, itemStyle: { color: 'var(--danger)' } },
      { name: '舒张压', type: 'line', data: diastolic, smooth: true, lineStyle: { color: 'var(--warn)', width: 2 }, itemStyle: { color: 'var(--warn)' } },
      { name: '空腹血糖', type: 'line', data: glucose, smooth: true, yAxisIndex: 1, lineStyle: { color: 'var(--primary)', width: 2 }, itemStyle: { color: 'var(--primary)' } }
    ]
  }
})

const createPh = async () => {
  await phFormRef.value.validate()
  await request.post('/medical/public-health', phForm.value)
  ElMessage.success('创建成功')
  showPhDialog.value = false
  loadPh()
}

// 签约居民
const myResidents = ref([])
async function loadMyResidents() {
  try {
    const { data } = await request.get('/medical/contract/my-residents')
    myResidents.value = data || []
  } catch {}
}
function daysLeft(endDate) {
  if (!endDate) return 0
  return Math.max(0, Math.ceil((new Date(endDate) - new Date()) / 86400000))
}

onMounted(() => { loadPlans(); loadPh(); loadMyResidents() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }

/* 响应式 */
@media (max-width: 768px) {
  .follow-up-page { padding: 12px; }
  .card-header { flex-direction: column; align-items: flex-start; gap: 8px; }
}
</style>
