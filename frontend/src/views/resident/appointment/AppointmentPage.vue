<template>
  <div class="appointment-page">

    <!-- Segmented 模式切换 -->
    <div class="segmented-tabs" style="margin-bottom:18px">
      <button :class="{ active: mode === 'new' }" @click="mode = 'new'">新建预约</button>
      <button :class="{ active: mode === 'history' }" @click="mode = 'history'; loadHistory()">我的预约</button>
    </div>

    <!-- ══ 历史预约 ══ -->
    <template v-if="mode === 'history'">
      <div class="filter-bar">
        <el-select v-model="historyStatus" clearable placeholder="全部状态" @change="loadHistory" style="width:140px">
          <el-option label="待就诊" :value="1" />
          <el-option label="已完成" :value="4" />
          <el-option label="已取消" :value="5" />
        </el-select>
      </div>
      <div v-for="appt in historyList" :key="appt.id" class="history-item">
        <div class="history-status-bar" :class="apptStatusClass(appt.status)"></div>
        <div class="history-content">
          <div class="history-title">
            <strong>{{ appt.deptName }}</strong>
            <span class="history-doctor">医生：{{ appt.staffName }}</span>
          </div>
          <div class="history-time">
            <Calendar :size="14" class="inline-icon" />{{ appt.apptDate }} 
            <Clock :size="14" class="inline-icon" style="margin-left: 8px;" />{{ { 1:'上午', 2:'下午', 3:'晚上' }[appt.timePeriod] }}
          </div>
          <div class="history-no">就诊编号：{{ appt.apptNo }}</div>
        </div>
        <div class="history-actions">
          <span :class="['status-tag', apptStatusClass(appt.status)]">{{ apptStatusLabel(appt.status) }}</span>
          <el-button v-if="appt.status === 1" size="small" type="danger" plain @click="cancelAppt(appt.id)">取消预约</el-button>
        </div>
      </div>
      <el-empty v-if="historyList.length === 0" description="暂无预约记录" :image-size="60" />
      <el-pagination v-model:current-page="historyPage" :page-size="10" :total="historyTotal"
        layout="prev, pager, next" @current-change="loadHistory" style="margin-top:12px;justify-content:center" />
    </template>

    <!-- ══ 新建预约 ══ -->
    <template v-if="mode === 'new'">

      <!-- Stepbar 三态进度条 -->
      <div class="stepbar" v-if="step < 4">
        <div v-for="(s, i) in steps" :key="i"
          :class="['step-item', step > i + 1 ? 'completed' : step === i + 1 ? 'active' : '']">
          <div class="step-dot">
            <span class="step-num">{{ i + 1 }}</span>
          </div>
          <span class="step-label">{{ s }}</span>
        </div>
      </div>

      <template v-if="step === 1">
        <p class="step-hint">选择您要就诊的科室，系统将展示对应的排班信息</p>
        <div class="dept-grid">
          <div v-for="dept in departments" :key="dept.code"
            class="dept-card" @click="selectDept(dept)" data-testid="dept-card">
            <div class="dept-icon-wrap">
              <Building2 :size="26" :stroke-width="1.5" />
            </div>
            <strong>{{ dept.name }}</strong>
            <span class="dept-hint">点击选择</span>
          </div>
        </div>
      </template>

      <!-- Step 2: 选择医生和时段 -->
      <template v-if="step === 2">
        <div class="two-col-layout">
          <!-- 左：医生+时段 -->
          <div class="panel list-panel">
            <div class="panel-header">
              <span class="panel-title">{{ selectedDept.name }} · 选择日期</span>
            </div>
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择预约日期"
              :disabled-date="disabledDate"
              style="width:100%;margin-bottom:16px;"
              data-testid="date-picker"
              @change="loadSchedules"
              :clearable="false"
            />
            
            <div v-for="sch in schedules" :key="sch.id" class="doctor-card">
              <div class="doctor-header">
                <strong>{{ sch.staffName }}</strong>
                <span class="status-tag done" v-if="slotsMap[sch.id] && slotsMap[sch.id].filter(s => s.remaining > 0).length > 0">
                  可预约
                </span>
                <span class="status-tag cancelled" v-else>约满</span>
              </div>
              <div class="slot-list">
                <button
                  v-for="slot in slotsMap[sch.id]"
                  :key="slot.id"
                  :class="['slot-btn', selectedSlot?.id === slot.id ? 'selected' : '', slot.remaining === 0 ? 'full' : '']"
                  :disabled="slot.remaining === 0"
                  @click="selectSlot(sch, slot)"
                  :data-testid="'slot-' + slot.id"
                >
                  <span class="slot-time">{{ timePeriodText(slot.timePeriod) }}</span>
                  <span class="slot-rem">余 {{ slot.remaining }}</span>
                </button>
              </div>
            </div>
            
            <div v-if="schedules.length === 0 && selectedDate" class="empty-state">
              <Ghost :size="48" color="var(--border)" stroke-width="1" />
              <p>该日期暂无排班</p>
            </div>
          </div>
          <!-- 右：摘要 -->
          <div class="panel summary-card-panel">
            <div class="panel-header">
              <span class="panel-title">预约摘要</span>
            </div>
            <div class="summary-block">
              <div class="summary-kv">
                <span class="sk-label">科室</span>
                <strong class="sk-val">{{ selectedDept.name }}</strong>
              </div>
              <div class="summary-kv">
                <span class="sk-label">接诊医生</span>
                <strong class="sk-val">{{ selectedSchedule?.staffName || '待选择' }}</strong>
              </div>
              <div class="summary-kv">
                <span class="sk-label">日期</span>
                <strong class="sk-val">{{ selectedDate?.toLocaleDateString('zh-CN') }}</strong>
              </div>
              <div class="summary-kv" style="border:none;">
                <span class="sk-label">时段</span>
                <strong :class="['sk-val', selectedSlot ? 'sk-highlight' : '']">{{ selectedSlot ? timePeriodText(selectedSlot.timePeriod) : '待选择' }}</strong>
              </div>
            </div>
            <el-button type="primary" style="width:100%;margin-top:16px;height:42px" :disabled="!selectedSlot" @click="step = 3" data-testid="btn-next">
              下一步：填写信息
            </el-button>
            <el-button text style="width:100%;margin-top:6px" @click="step = 1">返回重选科室</el-button>
          </div>
        </div>
      </template>

      <!-- Step 3: 填写信息 -->
      <template v-if="step === 3">
        <div class="two-col-layout">
          <!-- 左：表单 -->
          <div class="panel list-panel">
            <div class="panel-header"><span class="panel-title">填写就诊人信息</span></div>
            <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
              <el-form-item label="真实姓名" prop="patientName">
                <el-input v-model="form.patientName" placeholder="请填写就诊人姓名" data-testid="input-patient-name" />
              </el-form-item>
              <el-form-item label="联系手机号" prop="patientPhone">
                <el-input v-model="form.patientPhone" maxlength="11" placeholder="请填写能联系到您的手机号" data-testid="input-patient-phone" />
              </el-form-item>
              <el-form-item label="症状描述 (选填)">
                <el-input v-model="form.symptomDesc" type="textarea" :rows="3" placeholder="简述您当前的症状，方便医生提前了解" data-testid="input-symptom" />
              </el-form-item>
            </el-form>
          </div>
          <!-- 右：摘要确认 -->
          <div class="panel summary-card-panel">
            <div class="panel-header"><span class="panel-title">预约确认</span></div>
            <div class="summary-block">
              <div class="summary-kv"><span class="sk-label">科室</span><strong class="sk-val">{{ selectedDept.name }}</strong></div>
              <div class="summary-kv"><span class="sk-label">医生</span><strong class="sk-val">{{ selectedSchedule.staffName }}</strong></div>
              <div class="summary-kv"><span class="sk-label">日期</span><strong class="sk-val">{{ selectedDate?.toLocaleDateString('zh-CN') }}</strong></div>
              <div class="summary-kv" style="border:none;"><span class="sk-label">时段</span><strong class="sk-val sk-highlight">{{ timePeriodText(selectedSlot.timePeriod) }}</strong></div>
            </div>
            <div class="alert-bar alert-warn" style="margin-top:10px;">
              <span>请仔细核对上方信息，提交后将正式挂号</span>
            </div>
            <el-button type="primary" :loading="submitting" style="width:100%;margin-top:14px;height:42px" @click="submitAppointment" data-testid="btn-submit">
              确认提交
            </el-button>
            <el-button text style="width:100%;margin-top:6px" @click="step = 2">返回上一步</el-button>
          </div>
        </div>
      </template>

      <!-- Step 4: 预约成功 -->
      <template v-if="step === 4">
        <div class="success-screen">
          <div class="success-icon-wrap">
            <CheckCircle2 :size="64" color="var(--primary)" :stroke-width="1.5" />
          </div>
          <h3>预约挂号成功！</h3>
          <p>就诊编号：<strong class="text-primary">{{ createdAppt.apptNo }}</strong></p>

          <!-- 预约摘要卡 -->
          <div class="appt-receipt">
            <div class="receipt-row">
              <span>就诊日期</span>
              <strong>{{ formatDateOnly(createdAppt.apptDate) }}</strong>
            </div>
            <div class="receipt-row">
              <span>时段</span>
              <strong>{{ { 1:'上午', 2:'下午', 3:'晚上' }[createdAppt.timePeriod] }}</strong>
            </div>
            <div class="receipt-row">
              <span>科室</span>
              <strong>{{ createdAppt.deptName }}</strong>
            </div>
            <div class="receipt-row" style="border:none;">
              <span>接诊医生</span>
              <strong>{{ createdAppt.staffName }}</strong>
            </div>
          </div>

          <p class="receipt-tip">请于就诊当日带齐证件，前往 <strong>{{ createdAppt.deptName }}</strong> 签到候诊。</p>
          <el-button type="primary" plain style="margin-top:20px;width:100%;height:44px;max-width:300px" @click="$router.push('/resident/home')">返回服务首页</el-button>
        </div>
      </template>

    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { Building2, Calendar, Clock, Ghost, CheckCircle2 } from 'lucide-vue-next'

const step = ref(1)
const mode = ref('history') // 默认进入历史记录，更符合高频直觉
const selectedDept = ref(null)
const selectedDate = ref(new Date()) // 默认选中今天
const schedules = ref([])
const slotsMap = ref({})
const selectedSchedule = ref(null)
const selectedSlot = ref(null)
const submitting = ref(false)
const formRef = ref()
const createdAppt = ref({})
const departments = ref([])

const steps = ['选择科室', '排班时段', '就诊信息', '完成预约']

function apptStatusLabel(s) {
  return { 1: '待就诊', 2: '候诊中', 3: '就诊中', 4: '已完成', 5: '已取消' }[s] || '未知'
}
function apptStatusClass(s) {
  return { 1: 'pending', 2: 'in-progress', 3: 'in-progress', 4: 'done', 5: 'cancelled' }[s] || 'default'
}

function formatDateOnly(dt) {
  return dt ? dt.split('T')[0] : ''
}

async function loadDepartments() {
  try {
    const { data } = await request.get('/admin/dept/list')
    departments.value = (data || [])
      .filter(d => d.isApptOpen && d.status === 1)
      .map(d => ({ code: d.deptCode, name: d.deptName }))
  } catch (e) {
    departments.value = [{ code: 'QKMZ', name: '全科门诊' }, { code: 'ZYMZ', name: '中医门诊' }, { code: 'KQMZ', name: '口腔门诊' }]
  }
}

const form = reactive({ patientName: '', patientPhone: '', symptomDesc: '' })

const rules = {
  patientName: [{ required: true, message: '真实姓名必填', trigger: 'blur' }],
  patientPhone: [
    { required: true, message: '联系号必填', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入11位手机号码', trigger: 'blur' }
  ]
}

function selectDept(dept) {
  selectedDept.value = dept
  step.value = 2
  loadSchedules() // 自动加载今天的排班
}

function disabledDate(date) {
  return date < new Date(new Date().setHours(0, 0, 0, 0)) || date > new Date(Date.now() + 14 * 86400000)
}

function timePeriodText(p) {
  return { 1: '上午', 2: '下午', 3: '晚上' }[p] || '未知'
}

async function loadSchedules() {
  if (!selectedDate.value) return
  const dateStr = selectedDate.value.toISOString().split('T')[0]
  schedules.value = []
  slotsMap.value = {}
  selectedSchedule.value = null
  selectedSlot.value = null
  
  try {
    const res = await request.get('/resident/appointment/schedules', {
      params: { deptCode: selectedDept.value.code, date: dateStr }
    })
    schedules.value = res.data || []
    for (const sch of schedules.value) {
      const slotRes = await request.get(`/resident/appointment/slots/${sch.id}`)
      slotsMap.value[sch.id] = slotRes.data || []
    }
  } catch (e) { /* handled */ }
}

function selectSlot(sch, slot) {
  if (slot.remaining <= 0) return
  selectedSchedule.value = sch
  selectedSlot.value = slot
}

async function submitAppointment() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const res = await request.post('/resident/appointment', {
      slotId: selectedSlot.value.id,
      deptCode: selectedDept.value.code,
      deptName: selectedDept.value.name,
      staffId: selectedSchedule.value.staffId,
      staffName: selectedSchedule.value.staffName,
      apptDate: selectedDate.value.toISOString().split('T')[0],
      timePeriod: selectedSlot.value.timePeriod,
      patientName: form.patientName,
      patientPhone: form.patientPhone,
      symptomDesc: form.symptomDesc
    })
    createdAppt.value = res.data
    step.value = 4
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

// --- 历史预约 ---
const historyList = ref([])
const historyPage = ref(1)
const historyTotal = ref(0)
const historyStatus = ref(null)

async function loadHistory() {
  try {
    const params = { page: historyPage.value, size: 10 }
    if (historyStatus.value) params.status = historyStatus.value
    const res = await request.get('/resident/appointment', { params })
    historyList.value = res.data?.records || []
    historyTotal.value = res.data?.total || 0
  } catch (e) { console.warn(e) }
}

async function cancelAppt(id) {
  try {
    await request.put(`/resident/appointment/${id}/cancel`)
    ElMessage.success('已作废该挂号单')
    loadHistory()
  } catch (e) {
    ElMessage.error('作废操作失败')
  }
}

onMounted(() => {
  loadDepartments()
  loadHistory()
  
  // 注入已登录用户的默认数据以方便体验
  const tryUser = localStorage.getItem('userInfo')
  if (tryUser) {
    try {
      const u = JSON.parse(tryUser)
      form.patientName = u.name || ''
      form.patientPhone = u.phone || ''
    } catch(e){}
  }
})
</script>

<style scoped>
.appointment-page { padding: 16px; max-width: 100%; margin: 0 auto; }

/* ══ PC 桌面端 (≥ 768px) ══ */
@media (min-width: 768px) {
  .appointment-page {
    max-width: 100%;
    padding: 24px 32px;
  }
}

/* 步骤提示 */
.step-hint { font-size: 13px; color: var(--muted); margin-bottom: 16px; }

/* 历史列表项 */
.history-item {
  display: flex;
  align-items: stretch;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  margin-bottom: 12px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}
.history-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
}
/* 左侧状态竖条 */
.history-status-bar {
  width: 4px;
  flex-shrink: 0;
  background: var(--border);
}
.history-status-bar.pending     { background: var(--info); }
.history-status-bar.in-progress { background: var(--primary); }
.history-status-bar.done        { background: var(--good); }
.history-status-bar.cancelled   { background: var(--neutral-300); }
.history-content { flex: 1; display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; gap: 12px; }
.history-info { display: flex; flex-direction: column; gap: 6px; }
.history-title { display: flex; align-items: baseline; gap: 12px; }
.history-title strong { font-size: 16px; color: var(--text-strong); }
.history-doctor { font-size: 14px; color: var(--text); }
.history-time { display: flex; align-items: center; font-size: 13px; color: var(--muted); }
.history-no { font-size: 12px; color: var(--muted); background: var(--surface-soft); display: inline-block; padding: 2px 8px; border-radius: 4px; width: fit-content; }
.inline-icon { margin-right: 4px; vertical-align: text-bottom; }
.history-actions { display: flex; flex-direction: column; align-items: flex-end; gap: 10px; flex-shrink: 0; }
.filter-bar { margin-bottom: 16px; }

/* 两栏布局 */
.two-col-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  align-items: start;
}

/* 医生卡片 */
.doctor-card {
  border-radius: 8px;
  background: var(--surface-soft);
  padding: 16px;
  margin-bottom: 12px;
}
.doctor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.doctor-header strong { font-size: 15px; color: var(--text-strong); }

/* 时段格子 */
.slot-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
}

/* 摘要确认卡（右侧粘性面板） */
.summary-card-panel {
  position: sticky;
  top: 16px;
  background: var(--surface-soft);
}
.panel-header { margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--border); }
.panel-title { font-size: 16px; font-weight: 600; color: var(--text-strong); }

/* 新版摘要 KV 组（定义已提升至 components.css） */
.summary-block { display: flex; flex-direction: column; gap: 0; }

/* 网格选科 */
.dept-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}
.dept-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px 18px;
  border-radius: 14px;
  border: 1px solid var(--border);
  background: var(--surface);
  cursor: pointer;
  gap: 8px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}
.dept-card:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 16px rgba(47,107,87,0.1);
  transform: translateY(-2px);
}
.dept-icon-wrap {
  width: 52px; height: 52px;
  border-radius: 14px;
  background: var(--primary-light);
  display: flex; align-items: center; justify-content: center;
  color: var(--primary);
  margin-bottom: 4px;
}
.dept-card strong { font-size: 15px; color: var(--text-strong); }
.dept-hint { font-size: 12px; color: var(--muted); }

/* 成功回执 */
.success-screen {
  text-align: center;
  padding: 48px 20px;
  background: var(--surface);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.success-icon-wrap { margin-bottom: 16px; }
.success-screen h3 { font-size: 22px; color: var(--text-strong); margin-bottom: 8px; }
.success-screen > p { font-size: 15px; color: var(--text); }
.text-primary { color: var(--primary); font-weight: 600; font-variant-numeric: tabular-nums; }

/* 预约摘要卡（Step4 回执） */
.appt-receipt {
  margin: 20px auto;
  width: 100%;
  max-width: 340px;
  background: var(--surface-soft);
  border-radius: 12px;
  padding: 4px 20px;
  text-align: left;
}
.receipt-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
}
.receipt-row span { color: var(--muted); }
.receipt-row strong { color: var(--text-strong); font-weight: 600; }
.receipt-tip { font-size: 13px; color: var(--muted); margin-top: 4px; max-width: 340px; line-height: 1.6; }

.empty-state { text-align: center; padding: 40px 0; color: var(--muted); font-size: 14px; display: flex; flex-direction: column; align-items: center; gap: 12px; }

@media (max-width: 768px) {
  .two-col-layout { grid-template-columns: 1fr; }
  .summary-card-panel { position: static; margin-top: 16px; }
  .history-item { flex-direction: row; }
  .history-content { flex-direction: column; align-items: stretch; gap: 8px; }
  .history-actions { flex-direction: row; justify-content: space-between; align-items: center; border-top: 1px solid var(--border); padding-top: 12px; }
}
</style>
