<template>
  <div class="health-record-page">
    <!-- 头部：使用全局 page-header 类 -->
    <div class="page-header">
      <div class="page-header-main">
        <h2>我的档案</h2>
        <p>建档日期: {{ formatDate(record?.createdAt) }}，最近更新: {{ formatDate(record?.updatedAt) }}</p>
      </div>
      <div class="page-header-actions">
        <el-button type="success" size="small" @click="downloadPdf" :loading="pdfLoading" plain>导出健康摘要 PDF</el-button>
      </div>
    </div>

    <template v-if="record">
      <!-- 四张骨干网格卡 -->
      <div class="metric-grid">
        <div class="metric-card-v2 mc-primary">
          <div class="mc-label">慢性病 (共{{ record.chronicTags ? record.chronicTags.split(',').length : 0 }}项)</div>
          <div class="mc-value">
            <template v-if="record.chronicTags">
              <span v-for="tag in record.chronicTags.split(',')" :key="tag"
                :class="['chronic-tag', tag.trim()]" style="margin: 2px 4px 2px 0; font-size:13px;">
                {{ chronicLabel(tag) }}
              </span>
            </template>
            <template v-else><span style="font-size:15px;">无慢病</span></template>
          </div>
        </div>
        <div class="metric-card-v2 mc-warn">
          <div class="mc-label">过敏史</div>
          <div class="mc-value">{{ record.allergyHistory || '无过敏史' }}</div>
        </div>
        <div class="metric-card-v2 mc-danger">
          <div class="mc-label">家族史</div>
          <div class="mc-value tags-display">{{ record.familyHistory || '无' }}</div>
        </div>
        <div class="metric-card-v2 mc-info">
          <div class="mc-label">紧急联系人</div>
          <div class="mc-value">{{ record.emergencyContact || '--' }}</div>
          <div class="mc-trend mc-trend-flat" style="margin-top:4px">{{ record.emergencyPhone || '--' }}</div>
        </div>
      </div>

      <!-- 三列面板 (生命体征、病史信息、近期用药) -->
      <div class="three-col-grid" style="margin-top:16px;">
        
        <!-- 栏 1：生命体征 (最新) -->
        <div class="panel">
          <h4 class="panel-header">生命体征 <span class="header-hint">(最新测定)</span></h4>
          
          <div class="stat-line">
            <span class="stat-label">近期血压</span>
            <div class="stat-val-group">
              <strong class="stat-val" :class="bpLevel.cls">{{ latestVitals.bp || '--' }}</strong>
              <span class="stat-unit">mmHg</span>
              <span class="indicator-badge" :class="bpLevel.bg">{{ bpLevel.text }}</span>
            </div>
          </div>

          <div class="stat-line">
            <span class="stat-label">空腹血糖</span>
            <div class="stat-val-group">
              <strong class="stat-val" :class="glucoseLevel.cls">{{ latestVitals.glucose || '--' }}</strong>
              <span class="stat-unit">mmol/L</span>
              <span class="indicator-badge" :class="glucoseLevel.bg">{{ glucoseLevel.text }}</span>
            </div>
          </div>

          <div class="stat-line">
            <span class="stat-label">近期体重</span>
            <div class="stat-val-group">
              <strong class="stat-val normal">{{ latestVitals.weight || '--' }}</strong>
              <span class="stat-unit">kg</span>
            </div>
          </div>
          
          <div v-if="latestVitals.bp && bpLevel.bg === 'warn'" class="alert-box">
            血压处于偏高状态，建议定期监测并坚持用药。
          </div>
        </div>

        <!-- 栏 2：病史信息 -->
        <div class="panel">
          <h4 class="panel-header">重疾病史信息</h4>
          <div class="history-block">
            <div class="history-label">既往重疾史：</div>
            <p class="history-text">{{ record.pastMedicalHistory || '体健，暂无重大既往病史记录。' }}</p>
          </div>
          <div class="history-block" style="margin-top:16px;">
            <div class="history-label">家族病史概览：</div>
            <p class="history-text">{{ record.familyHistory || '否认高血压、糖尿病等常见家族遗传病史。' }}</p>
          </div>
        </div>

        <!-- 栏 3：近期就诊 -->
        <div class="panel">
          <h4 class="panel-header">近期就诊记录</h4>
          <div class="visit-list">
            <el-empty v-if="recentVisits.length === 0" description="暂无记录" :image-size="40" />
            <div v-for="v in recentVisits.slice(0,3)" :key="v.id" class="visit-item">
              <div class="visit-head">
                <strong>{{ v.deptName }}</strong>
                <span class="visit-date">{{ v.visitDate?.substring(0,10) }}</span>
              </div>
              <div class="visit-diag">诊断：{{ v.diagnosisNames || '待下诊断' }}</div>
              <div class="visit-comp">主诉：{{ v.chiefComplaint || '--' }}</div>
            </div>
          </div>
        </div>

      </div>

      <!-- 底部图表：健康趋势 -->
      <div class="panel" style="margin-top:16px;">
        <h4 class="panel-header">健康指标随访趋势</h4>
        <div class="chart-wrapper">
          <v-chart v-if="vitalChartOption.series?.length" :option="vitalChartOption" style="height:280px;" autoresize />
          <el-empty v-else description="暂无充足的体征趋势数据" :image-size="40" />
        </div>
      </div>

    </template>
    
    <el-empty v-else description="暂未建立居民健康档案" :image-size="80" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])

const record = ref(null)
const recentVisits = ref([])
const pdfLoading = ref(false)
const vitalChartOption = ref({})

const latestVitals = reactive({ bp: null, glucose: null, weight: null })

function formatDate(dt) {
  return dt ? dt.replace('T', ' ').substring(0, 16) : ''
}

function chronicLabel(c) {
  const map = { hypertension: '高血压', diabetes: '糖尿病', chd: '冠心病', copd: '慢阻肺', stroke: '脑卒中' }
  return map[c?.trim()] || c?.trim()
}

// 简单的体征预警评判
const bpLevel = computed(() => {
  const v = latestVitals.bp
  if (!v) return { text: '未知', cls: '', bg: 'normal' }
  const sys = parseInt(v.split('/')[0]) || 0
  if (sys > 140) return { text: '偏高', cls: 'warn-text', bg: 'warn' }
  if (sys < 90)  return { text: '偏低', cls: 'warn-text', bg: 'warn' }
  return { text: '正常', cls: 'normal-text', bg: 'normal' }
})

const glucoseLevel = computed(() => {
  const v = parseFloat(latestVitals.glucose)
  if (!v) return { text: '未知', cls: '', bg: 'normal' }
  if (v > 6.1) return { text: '偏高', cls: 'warn-text', bg: 'warn' }
  if (v < 3.9) return { text: '偏低', cls: 'warn-text', bg: 'warn' }
  return { text: '正常', cls: 'normal-text', bg: 'normal' }
})

async function downloadPdf() {
  pdfLoading.value = true
  try {
    const res = await request.get('/resident/health-summary/pdf', {
      responseType: 'blob'
    })
    const blob = new Blob([res.data || res], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    // 动态拼接文件名：用户名_日期.pdf
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const userName = userInfo.name || userInfo.username || '居民'
    const today = new Date().toISOString().slice(0, 10)
    a.download = `${userName}_${today}.pdf`
    // 必须挂载到 DOM，Chrome 才能正确识别 download 属性
    a.style.display = 'none'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (e) {
    ElMessage.error('下载失败，请稍后重试')
  } finally {
    pdfLoading.value = false
  }
}

onMounted(async () => {
  try {
    const res = await request.get('/resident/health-record')
    record.value = res.data
  } catch (e) { console.warn(e) }

  try {
    const res = await request.get('/resident/visit-record', { params: { page: 1, size: 20 } })
    recentVisits.value = res.data?.records || []
  } catch (e) { console.warn(e) }

  // 从 health_vital 表获取趋势数据（创新点①）
  try {
    const [bpRes, glucoseRes, weightRes, latestRes] = await Promise.all([
      request.get('/resident/vital', { params: { type: 'blood_pressure', days: 180 } }),
      request.get('/resident/vital', { params: { type: 'blood_glucose', days: 180 } }),
      request.get('/resident/vital', { params: { type: 'weight', days: 180 } }),
      request.get('/resident/vital/latest')
    ])

    // 最新指标
    const latestMap = latestRes.data || {}
    if (latestMap.blood_pressure) latestVitals.bp = latestMap.blood_pressure.vitalValue
    if (latestMap.blood_glucose)  latestVitals.glucose = latestMap.blood_glucose.vitalValue
    if (latestMap.weight)         latestVitals.weight = latestMap.weight.vitalValue

    // 趋势图数据
    const bpList = (bpRes.data || []).reverse()
    const glucoseList = (glucoseRes.data || []).reverse()
    const weightList = (weightRes.data || []).reverse()

    // 合并所有日期，X 轴
    const allDates = new Set()
    ;[bpList, glucoseList, weightList].forEach(list =>
      list.forEach(v => allDates.add(v.measureTime?.substring(0, 10)))
    )
    const dates = [...allDates].sort()

    if (dates.length > 0) {
      const mapByDate = (list) => {
        const m = {}
        list.forEach(v => { m[v.measureTime?.substring(0, 10)] = v.vitalValue })
        return dates.map(d => {
          const val = m[d]
          if (!val) return null
          // 血压取收缩压
          if (val.includes('/')) return parseInt(val.split('/')[0]) || null
          return parseFloat(val) || null
        })
      }

      vitalChartOption.value = {
        tooltip: { trigger: 'axis' },
        legend: { data: ['收缩压(mmHg)', '血糖(mmol/L)', '体重(kg)'], bottom: 0 },
        grid: { left: 40, right: 30, top: 40, bottom: 40 },
        xAxis: { type: 'category', data: dates, boundaryGap: false },
        yAxis: { type: 'value' },
        series: [
          { name: '收缩压(mmHg)', type: 'line', data: mapByDate(bpList), smooth: true, connectNulls: true, itemStyle: { color: '#c0392b' }, lineStyle: { width: 3 } },
          { name: '血糖(mmol/L)', type: 'line', data: mapByDate(glucoseList), smooth: true, connectNulls: true, itemStyle: { color: '#e67e22' }, lineStyle: { width: 3 } },
          { name: '体重(kg)', type: 'line', data: mapByDate(weightList), smooth: true, connectNulls: true, itemStyle: { color: '#2f6b57' }, lineStyle: { width: 3 } }
        ]
      }
    }
  } catch (e) { console.warn('加载健康指标趋势失败', e) }
})
</script>

<style scoped>
.health-record-page {
  padding: 16px;
  margin: 0 auto;
}
.divider { margin: 0 10px; opacity: 0.3; }
.tags-display { font-size: 16px; word-break: break-all; }

/* 慢病标签行（使用全局 chronic-tag） */
.chronic-tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 4px;
}

/* 三列表局 */
.three-col-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

/* 生命体征条目（核心定义已提升至 components.css，以下为页面补充） */
.warn-text { color: var(--text-warning); }
.normal-text { color: var(--text); }
.header-hint { margin-left: auto; font-size: 12px; font-weight: 400; color: var(--muted); }

/* 病史信息区 */
.history-block { display: flex; flex-direction: column; gap: 6px; }
.history-label { font-size: 13px; font-weight: 600; color: var(--text); }
.history-text { margin: 0; font-size: 14px; color: var(--muted); line-height: 1.6; }

/* 近期就诊列表 */
.visit-list { display: flex; flex-direction: column; gap: 12px; }
.visit-item {
  background: var(--surface-soft);
  border-radius: 8px;
  padding: 12px;
}
.visit-head { display: flex; justify-content: space-between; margin-bottom: 6px; }
.visit-head strong { font-size: 14px; color: var(--text-strong); }
.visit-date { font-size: 12px; color: var(--muted); }
.visit-diag { font-size: 13px; color: var(--text); margin-bottom: 4px; }
.visit-comp { font-size: 12px; color: var(--muted); }

/* 响应式 */
@media screen and (max-width: 900px) {
  .three-col-grid { grid-template-columns: 1fr; }
  .panel { height: auto; }
}
</style>
