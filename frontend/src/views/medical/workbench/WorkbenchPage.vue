<template>
  <div class="workbench">

    <!-- 一行状态条 -->
    <div class="status-bar">
      <div class="status-items">
        <span class="sb-item"><strong>{{ queue.filter(q => q.status <= 2).length }}</strong> 候诊</span>
        <span class="sb-divider">|</span>
        <span class="sb-item sb-success"><strong>{{ completeCount }}</strong> 已接诊</span>
        <span class="sb-divider">|</span>
        <span class="sb-item">全科门诊 · {{ timeNow }}</span>
      </div>
      <el-button type="primary" text size="small" @click="refreshQueue" data-testid="btn-refresh">
        <RefreshCcw :size="14" style="margin-right:4px" /> 刷新
      </el-button>
    </div>

    <!-- 左窄右宽双栏 -->
    <div class="workspace-two-col">

      <!-- 左栏：候诊队列 -->
      <div class="queue-panel">
        <div class="queue-tabs">
          <button :class="{ active: queueTab === 'waiting' }" @click="queueTab = 'waiting'">
            候诊中 ({{ queue.filter(q => q.status <= 2).length }})
          </button>
          <button :class="{ active: queueTab === 'done' }" @click="queueTab = 'done'">
            就诊中 ({{ queue.filter(q => q.status === 3).length }})
          </button>
        </div>
        <div class="queue-list">
          <div v-for="appt in filteredQueue" :key="appt.id"
            class="q-item" :class="{ active: currentVisit?.appointmentId === appt.id }"
            @click="appt.status === 2 && startVisit(appt.id)"
          >
            <div class="q-info">
              <strong>{{ appt.patientName }}</strong>
              <span class="q-sub">{{ timePeriodText(appt.timePeriod) }} · {{ appt.apptNo }}</span>
            </div>
            <div class="q-right">
              <span :class="['status-tag', queueStatusClass(appt.status)]">{{ queueStatusLabel(appt.status) }}</span>
              <el-button v-if="appt.status === 1" size="small" @click.stop="callPatient(appt.id)" type="primary" plain>叫号</el-button>
              <el-button v-if="appt.status === 2" size="small" @click.stop="startVisit(appt.id)" type="success" plain>接诊</el-button>
            </div>
          </div>
          <el-empty v-if="filteredQueue.length === 0" description="暂无患者" :image-size="48" />
        </div>
      </div>

      <!-- 右栏：接诊主面板 -->
      <div class="visit-panel">
        <template v-if="currentVisit">
          <!-- 患者信息横条 -->
          <div class="patient-banner">
            <div class="pb-left">
              <strong class="pb-name">{{ currentVisit.patientName || '患者' }}</strong>
              <span v-if="currentVisit.age" class="pb-age">{{ currentVisit.age }}岁</span>
              <span v-for="c in (currentVisit.chronicles || [])" :key="c"
                :class="['chronic-tag', c]">{{ chronicLabel(c) }}</span>
            </div>
            <span class="pb-visit-no">{{ currentVisit.visitNo }}</span>
          </div>

          <!-- 高风险警告 -->
          <div class="alert-bar alert-warn" v-if="currentVisit.highRisk">
            <span class="alert-icon"><TriangleAlert :size="14" /></span>
            <span>高风险患者，请关注血压和心率变化</span>
          </div>

          <!-- M9 患者健康摘要 -->
          <div v-if="patientSummary" class="patient-summary-card">
            <div class="summary-row">
              <span v-if="patientSummary.bloodPressure" class="summary-tag">血压: {{ patientSummary.bloodPressure }}</span>
              <span v-if="patientSummary.bloodGlucose" class="summary-tag">血糖: {{ patientSummary.bloodGlucose }}</span>
              <span v-if="patientSummary.weight" class="summary-tag">体重: {{ patientSummary.weight }}kg</span>
              <span v-if="patientSummary.lastVisitDate" class="summary-tag">最近就诊: {{ patientSummary.lastVisitDate }}</span>
            </div>
            <div v-if="patientSummary.allergyHistory" class="summary-allergy">
              过敏史: {{ patientSummary.allergyHistory }}
            </div>
            <div v-if="patientSummary.pastHistory" class="summary-past">
              既往史: {{ patientSummary.pastHistory }}
            </div>
          </div>

          <!-- 体征：一行只读标签（可点击编辑） -->
          <div class="vitals-bar" @click="showVitalsEditor = !showVitalsEditor">
            <Activity :size="14" />
            <span v-if="visitForm.bloodPressure">BP: {{ visitForm.bloodPressure }}</span>
            <span v-if="visitForm.temperature">T: {{ visitForm.temperature }}℃</span>
            <span v-if="visitForm.pulse">P: {{ visitForm.pulse }}</span>
            <span v-if="visitForm.spo2">SpO2: {{ visitForm.spo2 }}%</span>
            <span v-if="!visitForm.bloodPressure && !visitForm.temperature" class="vitals-placeholder">点击录入体征...</span>
            <ChevronDown :size="14" :class="{ 'rotate-180': showVitalsEditor }" />
          </div>
          <!-- 体征编辑：折叠面板 -->
          <div v-if="showVitalsEditor" class="vitals-editor">
            <el-input v-for="v in vitalFields" :key="v.key"
              v-model="visitForm[v.key]" :placeholder="v.placeholder" :type="v.type || 'text'" size="small">
              <template #prefix><component :is="v.icon" :size="14" /></template>
            </el-input>
          </div>

          <!-- 核心书写区 -->
          <el-form :model="visitForm" :rules="visitRules" ref="visitFormRef" label-position="top" class="visit-form">
            <el-form-item label="主诉" prop="chiefComplaint">
              <el-input v-model="visitForm.chiefComplaint" type="textarea" :rows="4"
                placeholder="患者主要症状及持续时间..." data-testid="input-complaint" />
            </el-form-item>
            <el-form-item label="诊断" prop="diagnosis">
              <IcdSearchSelect v-model="visitForm.diagnosis" data-testid="input-diagnosis" />
            </el-form-item>
            <el-form-item label="治疗方案">
              <el-input v-model="visitForm.treatmentPlan" type="textarea" :rows="5"
                placeholder="处置意见与用药方案..." data-testid="input-plan" />
            </el-form-item>
          </el-form>

          <!-- 底部操作条（固定） -->
          <div class="action-footer">
            <el-button @click="completeVisit" type="primary" :loading="completing" data-testid="btn-complete">
              完成接诊
            </el-button>
          </div>
        </template>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <el-empty description="请从左侧候诊列表选择患者开始接诊" :image-size="100" />
        </div>
      </div>
    </div>

    <!-- 快速开方弹窗（保留，后续迭代可改内嵌） -->
    <el-dialog v-model="showPrescDlg" title="快速开方" width="700px">
      <el-form :model="prescForm" :rules="prescRules" ref="prescFormRef" label-width="80px">
        <el-form-item label="就诊记录"><el-input :value="lastVisitId" disabled /></el-form-item>
        <el-form-item label="备注"><el-input v-model="prescForm.notes" type="textarea" :rows="2" /></el-form-item>
        <div v-for="(item, idx) in prescForm.items" :key="idx" class="presc-item-row">
          <el-row :gutter="8">
            <el-col :span="7"><el-input v-model="item.drugName" placeholder="药品名称" /></el-col>
            <el-col :span="3"><el-input v-model="item.quantity" placeholder="数量" type="number" /></el-col>
            <el-col :span="4"><el-input v-model="item.usageMethod" placeholder="用法" /></el-col>
            <el-col :span="4"><el-input v-model="item.frequency" placeholder="频次" /></el-col>
            <el-col :span="3"><el-input v-model="item.days" placeholder="天数" type="number" /></el-col>
            <el-col :span="3">
              <el-button type="danger" link @click="prescForm.items.splice(idx,1)">删除</el-button>
            </el-col>
          </el-row>
        </div>
        <el-button type="primary" plain size="small" style="margin-top:8px"
          @click="prescForm.items.push({drugName:'',quantity:1,usageMethod:'口服',frequency:'每日三次',days:7})"
        >+ 添加药品</el-button>
      </el-form>
      <template #footer>
        <el-button @click="showPrescDlg = false">跳过</el-button>
        <el-button type="primary" @click="submitPresc" :loading="prescSubmitting">提交处方</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import {
  Activity, Stethoscope, Thermometer, Heart, Weight, Droplets,
  TriangleAlert, RefreshCcw, ChevronDown
} from 'lucide-vue-next'
import IcdSearchSelect from '@/components/IcdSearchSelect.vue'

/* ── 体征字段配置 ── */
const vitalFields = [
  { key: 'bloodPressure', placeholder: '血压 mmHg',   icon: Stethoscope },
  { key: 'temperature',   placeholder: '体温 ℃',     icon: Thermometer, type: 'number' },
  { key: 'pulse',         placeholder: '脉搏 次/分',  icon: Heart,       type: 'number' },
  { key: 'weight',        placeholder: '体重 kg',     icon: Weight,      type: 'number' },
  { key: 'spo2',          placeholder: '血氧 %',      icon: Droplets,    type: 'number' },
]

const queue = ref([])
const currentVisit = ref(null)
const completing = ref(false)
const showVitalsEditor = ref(false)
const queueTab = ref('waiting')
const visitForm = reactive({
  chiefComplaint: '', diagnosis: '', treatmentPlan: '',
  bloodPressure: '', temperature: '', pulse: '', weight: '', spo2: ''
})
const visitFormRef = ref()
const visitRules = {
  chiefComplaint: [{ required: true, message: '请输入主诉', trigger: 'blur' }],
  diagnosis: [{ required: true, message: '请输入诊断', trigger: 'blur' }]
}
const prescFormRef = ref()
const prescRules = {
  notes: [{ required: false }]
}

const timeNow = computed(() => new Date().toLocaleTimeString('zh', { hour: '2-digit', minute: '2-digit' }))

const filteredQueue = computed(() => {
  if (queueTab.value === 'waiting') return queue.value.filter(q => q.status <= 2)
  return queue.value.filter(q => q.status === 3)
})

function timePeriodText(p) { return { 1: '上午', 2: '下午', 3: '晚上' }[p] || '' }
const completeCount = ref(0)
const patientSummary = ref(null)

function queueStatusLabel(s) { return { 1: '待叫号', 2: '候诊中', 3: '就诊中' }[s] || '未知' }
function queueStatusClass(s) { return { 1: 'pending', 2: 'pending', 3: 'in-progress' }[s] || 'pending' }
function chronicLabel(c) {
  return { hypertension: '高血压', diabetes: '糖尿病', chd: '冠心病', copd: '慢阻肺', stroke: '脑卒中' }[c] || c
}

async function refreshQueue() {
  try { const res = await request.get('/medical/workbench/queue'); queue.value = res.data || [] }
  catch (e) { console.warn(e) }
}

async function callPatient(id) {
  try { await request.put(`/medical/workbench/call/${id}`); ElMessage.success('已叫号'); refreshQueue() }
  catch (e) { console.warn(e) }
}

async function startVisit(id) {
  try {
    const res = await request.post(`/medical/workbench/start/${id}`)
    currentVisit.value = res.data
    showVitalsEditor.value = false
    patientSummary.value = null
    Object.assign(visitForm, {
      chiefComplaint: '', diagnosis: '', treatmentPlan: '',
      bloodPressure: '', temperature: '', pulse: '', weight: '', spo2: ''
    })
    refreshQueue()
    // M9: 加载患者健康摘要
    if (res.data?.residentId) {
      try {
        const summaryRes = await request.get('/medical/health-summary/pdf', { params: { residentId: res.data.residentId } })
        patientSummary.value = summaryRes.data || null
      } catch { /* 静默 */ }
    }
  } catch (e) { console.warn(e) }
}

async function completeVisit() {
  if (!currentVisit.value) return
  const valid = await visitFormRef.value?.validate().catch(() => false)
  if (!valid) return
  completing.value = true
  try {
    await request.put(`/medical/workbench/complete/${currentVisit.value.id}`, visitForm)
    ElMessage.success('接诊完成，可就此为患者开处方')
    lastVisitId.value = currentVisit.value.id
    prescForm.notes = ''
    prescForm.items = []
    showPrescDlg.value = true
    currentVisit.value = null
    refreshQueue()
  } catch (e) { console.warn(e) }
  finally { completing.value = false }
}

const showPrescDlg = ref(false)
const prescSubmitting = ref(false)
const lastVisitId = ref(null)
const prescForm = ref({ notes: '', items: [] })

async function submitPresc() {
  if (prescForm.value.items.length === 0) { showPrescDlg.value = false; return }
  prescSubmitting.value = true
  try {
    await request.post('/medical/prescription', {
      visitId: lastVisitId.value,
      notes: prescForm.value.notes,
      items: prescForm.value.items
    })
    ElMessage.success('处方已提交，请药房发药')
    showPrescDlg.value = false
  } catch (e) { ElMessage.error('开方失败') }
  finally { prescSubmitting.value = false }
}

onMounted(() => refreshQueue())
</script>

<style scoped>
.workbench {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 0;
}

/* ── 一行状态条 ── */
.status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background: var(--surface-soft);
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}
.status-items { display: flex; align-items: center; gap: 8px; font-size: 14px; color: var(--muted); }
.sb-item strong { color: var(--text); font-size: 16px; margin-right: 2px; font-variant-numeric: tabular-nums; }
.sb-success strong { color: var(--good); }
.sb-divider { color: var(--border-strong); }

/* ── 双栏布局 ── */
.workspace-two-col {
  display: grid;
  grid-template-columns: 280px 1fr;
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

/* ── 左栏：队列面板 ── */
.queue-panel {
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.queue-tabs {
  display: flex;
  gap: 0;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}
.queue-tabs button {
  flex: 1;
  padding: 10px 0;
  border: none;
  background: none;
  font-size: 13px;
  font-weight: 600;
  color: var(--muted);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  font-family: var(--font-sans);
}
.queue-tabs button.active {
  color: var(--primary-strong);
  border-bottom-color: var(--primary);
}

.queue-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  scrollbar-width: thin;
}

.q-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s;
  gap: 8px;
}
.q-item:hover { background: var(--primary-light); }
.q-item.active { background: var(--primary-light-hover); border: 1px solid var(--primary); }

.q-info { min-width: 0; }
.q-info strong { display: block; font-size: 14px; color: var(--text); }
.q-sub { font-size: 12px; color: var(--muted); }

.q-right { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; flex-shrink: 0; }

/* ── 右栏：接诊面板 ── */
.visit-panel {
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding: 16px 24px;
  gap: 12px;
}

/* 患者信息横条 */
.patient-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
  border-radius: 10px;
  flex-shrink: 0;
}
.pb-left { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.pb-name { font-size: 17px; }
.pb-age { font-size: 14px; color: var(--muted); }
.pb-visit-no { font-size: 13px; color: var(--muted); }

/* 体征横条 */
.vitals-bar {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 14px;
  background: var(--surface-soft);
  border: 1px dashed var(--border);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text);
  cursor: pointer;
  transition: background 0.15s;
  flex-shrink: 0;
}
.vitals-bar:hover { background: var(--primary-light); }

/* M9 患者健康摘要 */
.patient-summary-card {
  padding: 10px 14px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 13px;
}
.summary-row { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 4px; }
.summary-tag {
  padding: 2px 8px; border-radius: 4px;
  background: rgba(47,107,87,0.08); color: var(--primary-strong);
  font-size: 12px; font-weight: 500;
}
.summary-allergy { color: var(--danger); font-size: 12px; margin-top: 4px; }
.summary-past { color: var(--muted); font-size: 12px; margin-top: 2px; }
.vitals-bar svg:last-child { margin-left: auto; color: var(--muted); transition: transform 0.2s; }
.rotate-180 { transform: rotate(180deg); }
.vitals-placeholder { color: var(--muted); font-style: italic; }

/* 体征编辑折叠区 */
.vitals-editor {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  padding: 10px 14px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
  border-radius: 0 0 8px 8px;
  margin-top: -8px;
  flex-shrink: 0;
}

/* 核心书写区 */
.visit-form { flex: 1; min-height: 0; }
.visit-form :deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}
.visit-form :deep(.el-textarea__inner) {
  font-size: 14px;
  line-height: 1.7;
}

/* 底部操作条 */
.action-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 0 4px;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
}
.action-footer .el-button { min-width: 140px; height: 40px; font-size: 14px; }

/* 空状态 */
.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 处方弹窗行 */
.presc-item-row { margin-bottom: 8px; }

/* 响应式 */
@media (max-width: 1024px) {
  .workspace-two-col { grid-template-columns: 1fr; }
  .queue-panel { max-height: 200px; border-right: none; border-bottom: 1px solid var(--border); }
  .vitals-editor { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .workbench-page { padding: 12px; }
  .queue-panel { max-height: 160px; padding: 10px; }
  .queue-row { padding: 8px 10px; font-size: 13px; }
  .vitals-bar { flex-wrap: wrap; gap: 8px; font-size: 12px; padding: 6px 10px; }
  .vitals-editor { grid-template-columns: 1fr; gap: 6px; padding: 8px 10px; }
  .action-footer { flex-direction: column; }
  .action-footer .el-button { width: 100%; min-width: unset; }
  .presc-item-row { margin-bottom: 6px; }
}
</style>
