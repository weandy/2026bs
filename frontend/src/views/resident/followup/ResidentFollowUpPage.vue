<template>
  <div class="follow-up-page">
    <h2>签约与随访</h2>

    <!-- ══ 签约状态卡片 ══ -->
    <div :class="['contract-card', contractStatusClass]">
      <template v-if="!contract">
        <div class="cc-empty">
          <Users :size="28" />
          <p>暂无家庭医生签约</p>
          <button class="btn-primary" @click="showApplyDialog = true">发起签约申请</button>
        </div>
      </template>
      <template v-else-if="contract.status === 'PENDING'">
        <div class="cc-info">
          <Clock :size="20" class="cc-icon" />
          <div>
            <strong>签约申请审核中</strong>
            <p>申请医生：{{ contract.doctorName || '--' }} · {{ contract.applyTime?.split('T')[0] || '' }}</p>
          </div>
        </div>
      </template>
      <template v-else-if="contract.status === 'ACTIVE'">
        <div class="cc-info">
          <CheckCircle :size="20" class="cc-icon active" />
          <div>
            <strong>家庭医生：{{ contract.doctorName }}</strong>
            <p>
              {{ contract.teamName || '' }} ·
              有效期 {{ contract.startDate }} ~ {{ contract.endDate }}
              <span v-if="daysToExpiry <= 30" class="expiry-warn">（{{ daysToExpiry }}天后到期）</span>
            </p>
          </div>
          <span class="cc-action" @click="handleCancel">申请解约</span>
        </div>
      </template>
      <template v-else-if="contract.status === 'REJECTED'">
        <div class="cc-info">
          <XCircle :size="20" class="cc-icon rejected" />
          <div>
            <strong>签约申请已驳回</strong>
            <p>原因：{{ contract.rejectReason || '未说明' }}</p>
          </div>
          <button class="btn-primary btn-sm" @click="showApplyDialog = true">重新申请</button>
        </div>
      </template>
      <template v-else>
        <div class="cc-info">
          <AlertCircle :size="20" class="cc-icon expired" />
          <div>
            <strong>{{ contract.status === 'EXPIRED' ? '签约已到期' : '签约已解除' }}</strong>
            <p>{{ contract.doctorName }} · {{ contract.endDate || '' }}</p>
          </div>
          <button class="btn-primary btn-sm" @click="showApplyDialog = true">发起新签约</button>
        </div>
      </template>
    </div>

    <!-- ══ 随访 Tab ══ -->
    <div class="tab-bar">
      <span :class="{ active: activeTab === 'plans' }"   @click="activeTab = 'plans'">随访计划</span>
      <span :class="{ active: activeTab === 'records' }" @click="activeTab = 'records'">随访记录</span>
      <span :class="{ active: activeTab === 'trend' }"   @click="activeTab = 'trend'">健康趋势</span>
    </div>

    <!-- Tab: 随访计划 -->
    <div v-if="activeTab === 'plans'" class="tab-content">
      <div v-if="plans.length === 0" class="empty-tip">暂无随访计划</div>
      <div v-for="p in plans" :key="p.id" class="plan-card" @click="selectPlan(p)">
        <div class="pc-top">
          <span :class="['chronic-tag', p.chronicType]">{{ chronicLabel(p.chronicType) }}</span>
          <span :class="['status-badge', p.status === 1 ? 'active' : 'stopped']">
            {{ p.status === 1 ? '管理中' : '已停止' }}
          </span>
        </div>
        <div class="pc-body">
          <div class="pc-row"><span class="pc-label">责任医生</span><span>{{ p.doctorName || '--' }}</span></div>
          <div class="pc-row"><span class="pc-label">随访频次</span><span>{{ freqLabel(p.frequency) }}</span></div>
          <div class="pc-row"><span class="pc-label">随访方式</span><span>{{ methodLabel(p.followUpMethod) }}</span></div>
          <div class="pc-row">
            <span class="pc-label">下次随访</span>
            <span :class="{ 'due-soon': isDueSoon(p.nextFollowDate) }">
              {{ p.nextFollowDate || '--' }}
              <template v-if="isDueSoon(p.nextFollowDate)"> · 即将到期</template>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab: 随访记录 -->
    <div v-if="activeTab === 'records'" class="tab-content">
      <div v-if="!selectedPlan" class="empty-tip">请先在"随访计划"中选择一个计划</div>
      <template v-else>
        <div class="selected-plan-bar">
          <span>当前计划：{{ chronicLabel(selectedPlan.chronicType) }} · {{ selectedPlan.doctorName }}</span>
          <span class="muted link" @click="activeTab = 'plans'">切换计划</span>
        </div>
        <div v-if="records.length === 0" class="empty-tip">医生尚未录入随访记录</div>
        <div class="timeline">
          <div v-for="r in records" :key="r.id" class="timeline-item">
            <div class="tl-dot" />
            <div class="tl-content">
              <div class="tl-header">
                <strong>{{ r.followDate || '--' }}</strong>
                <span class="tl-staff">{{ r.staffName || '系统' }}</span>
              </div>
              <div class="tl-body">
                <span v-if="r.systolicBp">血压 {{ r.systolicBp }}/{{ r.diastolicBp }} mmHg</span>
                <span v-if="r.fastingGlucose">空腹血糖 {{ r.fastingGlucose }} mmol/L</span>
                <span v-if="r.medicationCompliance">用药 {{ complianceLabel(r.medicationCompliance) }}</span>
              </div>
              <div v-if="r.healthGuidance" class="tl-guidance">📋 {{ r.healthGuidance }}</div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Tab: 健康趋势 -->
    <div v-if="activeTab === 'trend'" class="tab-content">
      <div v-if="!selectedPlan" class="empty-tip">请先在"随访计划"中选择一个计划</div>
      <template v-else>
        <div class="selected-plan-bar">
          <span>当前计划：{{ chronicLabel(selectedPlan.chronicType) }} · {{ selectedPlan.doctorName }}</span>
        </div>
        <div v-if="trendData.length === 0" class="empty-tip">暂无趋势数据</div>
        <div v-else class="chart-wrap">
          <v-chart :option="chartOption" style="height: 320px;" autoresize />
        </div>
      </template>
    </div>

    <!-- ══ 签约申请弹窗 ══ -->
    <div v-if="showApplyDialog" class="modal-overlay" @click.self="showApplyDialog = false">
      <div class="modal-box">
        <h3>发起签约申请</h3>

        <label>选择科室</label>
        <select v-model="applyForm.deptCode" @change="loadDoctors">
          <option value="">全部科室</option>
          <option v-for="d in depts" :key="d.deptCode" :value="d.deptCode">{{ d.deptName }}</option>
        </select>

        <label>选择医生</label>
        <div v-if="doctorList.length === 0" class="empty-tip">暂无可签约医生</div>
        <div v-for="doc in doctorList" :key="doc.doctorId"
             :class="['doctor-item', { selected: applyForm.doctorId === doc.doctorId }]"
             @click="applyForm.doctorId = doc.doctorId; applyForm.doctorName = doc.doctorName">
          <strong>{{ doc.doctorName }}</strong>
          <span class="muted">{{ doc.deptName || '' }} · 已签约 {{ doc.contractCount || 0 }} 人</span>
        </div>

        <label>签约原因（选填）</label>
        <textarea v-model="applyForm.applyReason" rows="2" placeholder="如：高血压长期管理"></textarea>

        <div class="modal-footer">
          <button @click="showApplyDialog = false">取消</button>
          <button class="btn-primary" @click="submitApply" :disabled="!applyForm.doctorId">提交申请</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import request from '@/utils/request'
import { Users, Clock, CheckCircle, XCircle, AlertCircle } from 'lucide-vue-next'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const contract = ref(null)
const plans = ref([])
const records = ref([])
const trendData = ref([])
const selectedPlan = ref(null)
const activeTab = ref('plans')

// 签约申请
const showApplyDialog = ref(false)
const depts = ref([])
const doctorList = ref([])
const applyForm = ref({ doctorId: null, doctorName: '', deptCode: '', applyReason: '' })

// ── 工具函数 ──
function chronicLabel(c) {
  return { hypertension: '高血压', diabetes: '糖尿病', chd: '冠心病', copd: '慢阻肺', stroke: '脑卒中' }[c] || c || '--'
}
function freqLabel(f) {
  return { 1: '每月', 2: '每季度', 3: '每半年' }[f] || `${f}天`
}
function methodLabel(m) {
  return { 1: '电话', 2: '门诊', 3: '上门' }[m] || '--'
}
function complianceLabel(c) {
  return { 1: '规律', 2: '间断', 3: '不服药' }[c] || '--'
}
function isDueSoon(dateStr) {
  if (!dateStr) return false
  const diff = (new Date(dateStr) - new Date()) / 86400000
  return diff >= 0 && diff <= 7
}

const daysToExpiry = computed(() => {
  if (!contract.value?.endDate) return 999
  return Math.ceil((new Date(contract.value.endDate) - new Date()) / 86400000)
})

const contractStatusClass = computed(() => {
  if (!contract.value) return 'no-contract'
  return contract.value.status?.toLowerCase() || 'no-contract'
})

// ── 数据加载 ──
async function loadContract() {
  try {
    const { data } = await request.get('/resident/follow-up/contract')
    contract.value = data
  } catch { /* 静默 */ }
}

async function loadPlans() {
  try {
    const { data } = await request.get('/resident/follow-up/plans')
    plans.value = data || []
    if (plans.value.length > 0 && !selectedPlan.value) {
      selectPlan(plans.value[0])
    }
  } catch { /* 静默 */ }
}

function selectPlan(plan) {
  selectedPlan.value = plan
  loadRecords(plan.id)
  loadTrend(plan.id)
}

async function loadRecords(planId) {
  try {
    const { data } = await request.get('/resident/follow-up/records', { params: { planId } })
    records.value = data || []
  } catch { /* 静默 */ }
}

async function loadTrend(planId) {
  try {
    const { data } = await request.get('/resident/follow-up/trend', { params: { planId } })
    trendData.value = data || []
  } catch { /* 静默 */ }
}

// ── 签约操作 ──
async function loadDepts() {
  try {
    const { data } = await request.get('/public/dept')
    depts.value = data || []
  } catch { /* 静默 */ }
}

async function loadDoctors() {
  try {
    const params = applyForm.value.deptCode ? { deptCode: applyForm.value.deptCode } : {}
    const { data } = await request.get('/resident/follow-up/doctors', { params })
    doctorList.value = data || []
  } catch { /* 静默 */ }
}

async function submitApply() {
  if (!applyForm.value.doctorId) return
  try {
    await request.post('/resident/follow-up/contract/apply', {
      doctorId: applyForm.value.doctorId,
      doctorName: applyForm.value.doctorName,
      applyReason: applyForm.value.applyReason
    })
    showApplyDialog.value = false
    loadContract()
  } catch (e) {
    alert(e.response?.data?.msg || '申请失败')
  }
}

async function handleCancel() {
  if (!confirm('确定要申请解约吗？')) return
  try {
    await request.put(`/resident/follow-up/contract/${contract.value.id}/cancel`, { reason: '居民主动解约' })
    loadContract()
  } catch (e) {
    alert(e.response?.data?.msg || '操作失败')
  }
}

// ── ECharts ──
const chartOption = computed(() => {
  const sorted = [...trendData.value].sort((a, b) => (a.followDate || '').localeCompare(b.followDate || ''))
  const dates = sorted.map(r => (r.followDate || '').slice(5))
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['收缩压', '舒张压', '空腹血糖'] },
    grid: { left: 50, right: 40, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: dates },
    yAxis: [
      { type: 'value', name: 'mmHg', position: 'left' },
      { type: 'value', name: 'mmol/L', position: 'right' }
    ],
    series: [
      { name: '收缩压', type: 'line', data: sorted.map(r => r.systolicBp), smooth: true, itemStyle: { color: '#e74c3c' } },
      { name: '舒张压', type: 'line', data: sorted.map(r => r.diastolicBp), smooth: true, itemStyle: { color: '#e67e22' } },
      { name: '空腹血糖', type: 'line', data: sorted.map(r => r.fastingGlucose), smooth: true, yAxisIndex: 1, itemStyle: { color: '#2f6b57' } }
    ]
  }
})

watch(showApplyDialog, (val) => {
  if (val) { loadDepts(); loadDoctors() }
})

onMounted(() => { loadContract(); loadPlans() })
</script>

<style scoped>
.follow-up-page { padding: 16px; }
.follow-up-page h2 { font-size: 20px; margin: 0 0 16px; color: var(--text); }

/* ── 签约状态卡 ── */
.contract-card {
  border-radius: 16px; padding: 16px 20px; margin-bottom: 16px;
  border: 1px solid var(--border);
}
.contract-card.no-contract { background: var(--surface-soft); }
.contract-card.active { background: linear-gradient(135deg, rgba(39,174,96,0.08), rgba(47,107,87,0.08)); border-color: rgba(39,174,96,0.2); }
.contract-card.pending { background: rgba(243,156,18,0.06); border-color: rgba(243,156,18,0.2); }
.contract-card.rejected { background: rgba(231,76,60,0.06); border-color: rgba(231,76,60,0.2); }
.contract-card.expired, .contract-card.cancelled { background: var(--surface-soft); }

.cc-empty { text-align: center; padding: 20px 0; color: var(--muted); }
.cc-empty p { margin: 8px 0 12px; font-size: 14px; }

.cc-info { display: flex; align-items: center; gap: 12px; }
.cc-info > div { flex: 1; }
.cc-info strong { font-size: 15px; color: var(--text); display: block; margin-bottom: 2px; }
.cc-info p { font-size: 13px; color: var(--muted); margin: 0; }
.cc-icon { flex-shrink: 0; }
.cc-icon.active { color: #27ae60; }
.cc-icon.rejected { color: #e74c3c; }
.cc-icon.expired { color: var(--neutral-500); }
.cc-action { font-size: 12px; color: var(--muted); cursor: pointer; white-space: nowrap; }
.cc-action:hover { color: #e74c3c; }
.expiry-warn { color: #e74c3c; font-weight: 600; }

/* ── Tab 栏 ── */
.tab-bar {
  display: flex; gap: 0; border-bottom: 2px solid var(--border); margin-bottom: 16px;
}
.tab-bar span {
  padding: 10px 20px; font-size: 14px; font-weight: 600; color: var(--muted);
  cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all .2s;
}
.tab-bar span.active { color: var(--primary); border-color: var(--primary); }
.tab-bar span:hover { color: var(--text); }

.tab-content { min-height: 200px; }

/* ── 随访计划卡片 ── */
.plan-card {
  background: var(--surface); border: 1px solid var(--border); border-radius: 14px;
  padding: 14px 16px; margin-bottom: 10px; cursor: pointer; transition: all .2s;
}
.plan-card:hover { border-color: var(--primary); box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.pc-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.pc-body { display: grid; grid-template-columns: 1fr 1fr; gap: 6px 16px; }
.pc-row { display: flex; gap: 6px; font-size: 13px; }
.pc-label { color: var(--muted); min-width: 60px; }
.due-soon { color: #e74c3c; font-weight: 600; }

.chronic-tag { font-size: 12px; padding: 2px 10px; border-radius: 6px; font-weight: 600; background: rgba(47,107,87,0.1); color: var(--primary); }
.status-badge { font-size: 11px; padding: 2px 8px; border-radius: 6px; font-weight: 600; }
.status-badge.active { background: rgba(39,174,96,0.1); color: #27ae60; }
.status-badge.stopped { background: rgba(93,109,126,0.1); color: var(--neutral-500); }

.selected-plan-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 12px; background: var(--surface-soft); border-radius: 10px; margin-bottom: 12px; font-size: 13px;
}

/* ── 时间线 ── */
.timeline { padding-left: 20px; border-left: 2px solid var(--border); }
.timeline-item { position: relative; padding: 0 0 16px 16px; }
.tl-dot {
  position: absolute; left: -27px; top: 4px; width: 10px; height: 10px;
  border-radius: 50%; background: var(--primary); border: 2px solid var(--surface);
}
.tl-header { display: flex; justify-content: space-between; margin-bottom: 4px; }
.tl-header strong { font-size: 14px; color: var(--text); }
.tl-staff { font-size: 12px; color: var(--muted); }
.tl-body { display: flex; flex-wrap: wrap; gap: 8px; font-size: 13px; color: var(--text-secondary); }
.tl-body span { background: var(--surface-soft); padding: 2px 8px; border-radius: 6px; }
.tl-guidance { font-size: 12px; color: var(--muted); margin-top: 4px; line-height: 1.5; }

/* ── 图表 ── */
.chart-wrap { background: var(--surface); border: 1px solid var(--border); border-radius: 14px; padding: 12px; }

/* ── 通用 ── */
.empty-tip { font-size: 13px; color: var(--muted); padding: 24px 0; text-align: center; }
.muted { color: var(--muted); font-size: 12px; }
.link { cursor: pointer; }
.link:hover { color: var(--primary); }
.btn-primary {
  background: var(--primary); color: #fff; border: none; padding: 8px 18px;
  border-radius: 10px; font-size: 13px; font-weight: 600; cursor: pointer; transition: .2s;
}
.btn-primary:hover { filter: brightness(1.1); }
.btn-primary:disabled { opacity: .5; cursor: not-allowed; }
.btn-sm { padding: 4px 12px; font-size: 12px; }

/* ── 弹窗 ── */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4); z-index: 1000;
  display: flex; align-items: center; justify-content: center;
}
.modal-box {
  background: var(--surface); border-radius: 18px; padding: 24px; width: 440px; max-height: 80vh; overflow-y: auto;
  box-shadow: 0 12px 40px rgba(0,0,0,0.15);
}
.modal-box h3 { margin: 0 0 16px; font-size: 18px; }
.modal-box label { display: block; font-size: 13px; font-weight: 600; color: var(--muted); margin: 12px 0 4px; }
.modal-box select, .modal-box textarea {
  width: 100%; padding: 8px 12px; border: 1px solid var(--border); border-radius: 10px;
  font-size: 13px; background: var(--surface); color: var(--text); font-family: inherit;
}
.doctor-item {
  padding: 10px 12px; border: 1px solid var(--border); border-radius: 10px;
  margin-bottom: 6px; cursor: pointer; transition: .2s;
}
.doctor-item:hover { border-color: var(--primary); }
.doctor-item.selected { border-color: var(--primary); background: rgba(47,107,87,0.06); }
.doctor-item strong { font-size: 14px; display: block; }
.modal-footer { display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px; }
.modal-footer button {
  padding: 8px 16px; border-radius: 10px; border: 1px solid var(--border);
  background: var(--surface); cursor: pointer; font-size: 13px;
}
</style>
