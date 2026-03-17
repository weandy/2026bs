<template>
  <div class="vaccine-page">
    <div class="page-header">
      <h2>疫苗接种</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="可接种疫苗" name="available" />
      <el-tab-pane label="我的预约" name="appointments" />
      <el-tab-pane label="接种记录" name="records" />
    </el-tabs>

    <!-- 可接种疫苗列表 -->
    <template v-if="activeTab === 'available'">
      <el-empty v-if="vaccineList.length === 0 && !loadingVaccines" description="暂无可接种疫苗" />
      <el-row :gutter="12" v-loading="loadingVaccines">
        <el-col :span="12" v-for="v in vaccineList" :key="v.id">
          <el-card shadow="hover" class="vaccine-card">
            <div class="vaccine-name">{{ v.vaccineName }}</div>
            <div class="vaccine-meta">
              <span>厂家: {{ v.manufacturer || '—' }}</span>
              <span>库存: <strong :style="{ color: v.quantity <= (v.alertQty || 0) ? 'var(--danger)' : 'var(--good)' }">{{ v.quantity }}</strong></span>
            </div>
            <div class="vaccine-meta" v-if="v.expiryDate">
              有效期至: {{ v.expiryDate }}
            </div>
            <el-button type="primary" size="small" style="margin-top:8px;width:100%" @click="openBooking(v)">
              预约接种
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 我的预约 -->
    <template v-if="activeTab === 'appointments'">
      <el-card v-for="a in appointments" :key="a.id" class="item-card" shadow="hover">
        <div class="item-header">
          <strong>{{ a.vaccineName }}</strong>
          <el-tag :type="a.status === 1 ? '' : a.status === 2 ? 'success' : 'info'" size="small">
            {{ { 1: '待接种', 2: '已完成', 3: '已取消' }[a.status] }}
          </el-tag>
        </div>
        <div class="item-meta">第{{ a.doseNumber }}针 | {{ a.apptDate }}</div>
        <el-button v-if="a.status === 1" type="danger" size="small" text
                   @click="cancelAppt(a.id)" style="margin-top: 4px;">取消预约</el-button>
      </el-card>
      <el-empty v-if="appointments.length === 0" description="暂无疫苗预约" />
    </template>

    <!-- 接种记录 -->
    <template v-if="activeTab === 'records'">
      <el-timeline>
        <el-timeline-item v-for="r in records" :key="r.id" :timestamp="r.vaccinationDate" placement="top"
          :color="r.adverseReaction ? 'var(--danger)' : 'var(--good)'">
          <el-card shadow="hover">
            <strong>{{ r.vaccineName }}</strong> 第{{ r.doseNumber }}针<br/>
            批号: {{ r.batchNumber }} | 接种人: {{ r.operatorName }}
            <div v-if="r.adverseReaction" style="color:var(--danger);margin-top:4px;font-size:12px">
              不良反应: {{ r.adverseReaction }}
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="records.length === 0" description="暂无接种记录" />
    </template>

    <!-- 预约接种弹窗 -->
    <el-dialog v-model="showBooking" title="预约接种" width="420px">
      <el-form :model="bookForm" label-width="80px">
        <el-form-item label="疫苗">{{ bookForm.vaccineName }}</el-form-item>
        <el-form-item label="剂次">
          <el-input-number v-model="bookForm.doseNumber" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="预约日期">
          <el-date-picker v-model="bookForm.apptDate" type="date" value-format="YYYY-MM-DD"
            :disabled-date="d => d < new Date()" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="bookForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBooking = false">取消</el-button>
        <el-button type="primary" @click="doBooking" :loading="booking">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('available')
const vaccineList = ref([])
const loadingVaccines = ref(false)
const appointments = ref([])
const records = ref([])
const showBooking = ref(false)
const booking = ref(false)
const bookForm = ref({ vaccineId: null, vaccineName: '', doseNumber: 1, apptDate: '', remark: '' })

async function loadAvailable() {
  loadingVaccines.value = true
  try {
    const { data } = await request.get('/resident/vaccine/available')
    vaccineList.value = data || []
  } finally { loadingVaccines.value = false }
}

async function loadAppointments() {
  try {
    const res = await request.get('/resident/vaccine/appointments', { params: { page: 1, size: 50 } })
    appointments.value = res.data?.records || []
  } catch (e) { console.warn(e) }
}

async function loadRecords() {
  try {
    const res = await request.get('/resident/vaccine/records')
    records.value = res.data || []
  } catch (e) { console.warn(e) }
}

function onTabChange(tab) {
  if (tab === 'available') loadAvailable()
  else if (tab === 'appointments') loadAppointments()
  else loadRecords()
}

function openBooking(v) {
  bookForm.value = { vaccineId: v.id, vaccineName: v.vaccineName, doseNumber: 1, apptDate: '', remark: '' }
  showBooking.value = true
}

async function doBooking() {
  if (!bookForm.value.apptDate) { ElMessage.warning('请选择预约日期'); return }
  booking.value = true
  try {
    await request.post('/resident/vaccine/appointment', bookForm.value)
    ElMessage.success('预约成功')
    showBooking.value = false
    activeTab.value = 'appointments'
    loadAppointments()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '预约失败')
  } finally { booking.value = false }
}

async function cancelAppt(id) {
  try {
    await request.put(`/resident/vaccine/appointment/${id}/cancel`)
    ElMessage.success('已取消')
    loadAppointments()
  } catch (e) { console.warn(e) }
}

onMounted(() => loadAvailable())
</script>

<style scoped>
.vaccine-card { margin-bottom: 12px; }
.vaccine-name { font-size: 16px; font-weight: 600; margin-bottom: 6px; }
.vaccine-meta { font-size: 13px; color: var(--muted); display: flex; justify-content: space-between; margin-bottom: 2px; }
.item-card { margin-bottom: 12px; }
.item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.item-meta { font-size: 13px; color: var(--muted); }

/* 移动端适配 */
@media (max-width: 768px) {
  .vaccine-page { padding: 12px; }
  .vaccine-page :deep(.el-col) {
    max-width: 100% !important;
    flex: 0 0 100% !important;
    margin-bottom: 10px;
  }
}
</style>
