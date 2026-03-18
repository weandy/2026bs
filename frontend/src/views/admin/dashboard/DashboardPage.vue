<template>
  <div class="dashboard">
    <!-- 页面头部 -->
    <div class="page-header">
      <p class="page-subtitle">数据概览</p>
      <div class="header-actions">
        <!-- 日期范围选择器 -->
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="small"
          :disabledDate="(d) => d > new Date()"
          value-format="YYYY-MM-DD"
          style="width: 240px; margin-right: 8px;"
          @change="onDateRangeChange"
        />
        <el-button type="success" size="small" :loading="exporting" @click="exportReport">
          <PackageSearch :size="14" style="margin-right:4px" /> 导出报表
        </el-button>
      </div>
    </div>

    <!-- 指标卡（累计数据，无日期限制） -->
    <div class="summary-grid">
      <div v-for="card in statCards" :key="card.label" :class="['metric-card-v2', card.cls]">
        <div class="mc-label">{{ card.label }}</div>
        <div class="mc-value">{{ card.value }}</div>
        <div class="mc-trend" v-if="card.hint">{{ card.hint }}</div>
      </div>
    </div>

    <!-- 图表行 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :sm="14">
        <div class="panel">
          <h4 class="panel-title-row">
            <span>{{ trendTitle }}</span>
            <span class="panel-date-hint">{{ dateRangeLabel }}</span>
          </h4>
          <v-chart :option="trendOption" style="height: 280px;" autoresize />
        </div>
      </el-col>
      <el-col :xs="24" :sm="10">
        <div class="panel">
          <h4 class="panel-title-row">
            <span>科室就诊分布</span>
            <span class="panel-date-hint">{{ dateRangeLabel }}</span>
          </h4>
          <v-chart :option="pieOption" style="height: 280px;" autoresize />
        </div>
      </el-col>
    </el-row>

    <!-- 摘要行 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :sm="8">
        <div class="panel">
          <h4>近期公告</h4>
          <div class="panel-scroll-body" style="max-height:220px">
            <el-empty v-if="notices.length === 0" description="暂无公告" :image-size="40" />
            <div v-for="n in notices" :key="n.id" class="notice-item">
              <span class="notice-title">{{ n.title }}</span>
              <span class="notice-time">{{ formatDate(n.createdAt) }}</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="panel">
          <h4>签约统计</h4>
          <div class="panel-scroll-body" style="max-height:220px">
            <div class="list-row"><span>当前生效签约</span><strong>{{ contractStats.active || 0 }}</strong></div>
            <div class="list-row"><span>本月新增</span><strong class="text-good">{{ contractStats.newThisMonth || 0 }}</strong></div>
            <div class="list-row"><span>待审核</span><strong class="text-warn">{{ contractStats.pending || 0 }}</strong></div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="panel">
          <h4>随访完成率</h4>
          <div class="panel-scroll-body" style="max-height:220px">
            <div class="list-row"><span>进行中计划</span><strong>{{ followUpStats.total || 0 }}</strong></div>
            <div class="list-row"><span>已完成</span><strong class="text-good">{{ followUpStats.completed || 0 }}</strong></div>
            <div class="list-row"><span>逾期未访</span><strong class="text-danger">{{ followUpStats.overdue || 0 }}</strong></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 增值信息行 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :sm="12">
        <div class="panel">
          <h4 class="panel-title-row">
            <span>接诊概况</span>
            <span class="panel-date-hint">{{ summaryDateLabel }}</span>
          </h4>
          <div class="panel-scroll-body" style="max-height:220px">
            <div class="list-row"><span>预约挂号总量</span><strong>{{ todaySummary.totalAppt }}</strong></div>
            <div class="list-row"><span>已完成就诊</span><strong class="text-good">{{ todaySummary.completed }}</strong></div>
            <div class="list-row"><span>候诊中</span><strong class="text-warn">{{ todaySummary.waiting }}</strong></div>
            <div class="list-row"><span>爽约/取消</span><strong class="text-muted">{{ todaySummary.cancelled }}</strong></div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12">
        <div class="panel">
          <h4 class="panel-title-row">
            <span>医生工作量排行</span>
            <span class="panel-date-hint">{{ dateRangeLabel }}</span>
            <el-tag v-if="isDemoRank" size="small" type="info" style="margin-left:6px">演示</el-tag>
          </h4>
          <div class="panel-scroll-body" style="max-height:220px">
            <el-empty v-if="doctorRank.length === 0" description="暂无数据" :image-size="40" />
            <div v-for="(doc, idx) in doctorRank" :key="doc.name" class="rank-row">
              <span class="rank-badge" :class="{ top: idx < 3 }">{{ idx + 1 }}</span>
              <span class="rank-name">{{ doc.name }}</span>
              <span class="rank-count">{{ doc.count }} 人次</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { PackageSearch } from 'lucide-vue-next'

use([CanvasRenderer, BarChart, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])

// ────────────────────────────────────────────────
// 日期范围（默认: 今天-6 ~ 今天）
// ────────────────────────────────────────────────
const today = new Date()
const fmt = (d) => d.toISOString().slice(0, 10)
const defaultStart = fmt(new Date(today.getTime() - 6 * 86400000))
const defaultEnd   = fmt(today)
const dateRange = ref([defaultStart, defaultEnd])

const dateRangeLabel = computed(() => {
  if (!dateRange.value?.length) return ''
  const [s, e] = dateRange.value
  if (s === e) return s.slice(5)
  return `${s.slice(5)} ~ ${e.slice(5)}`
})

const trendTitle = computed(() => {
  if (!dateRange.value?.length) return '就诊趋势'
  const [s, e] = dateRange.value
  const days = Math.round((new Date(e) - new Date(s)) / 86400000) + 1
  return `近 ${days} 天就诊趋势`
})

// 摘要日期：用结束日（或今日）
const summaryDate = computed(() => dateRange.value?.[1] || defaultEnd)
const summaryDateLabel = computed(() => summaryDate.value?.slice(5))

// ────────────────────────────────────────────────
// 响应式数据
// ────────────────────────────────────────────────
const statCards = ref([
  { label: '签约人数', value: '-', cls: 'mc-success', hint: '当前生效签约' },
  { label: '随访计划', value: '-', cls: 'mc-primary', hint: '进行中计划'  },
  { label: '员工总数', value: '-', cls: 'mc-warn',    hint: ''            },
  { label: '系统公告', value: '-', cls: 'mc-danger',  hint: ''            },
  { label: '今日预约', value: '-', cls: 'mc-info',    hint: '今日预约总量'  }
])

const todaySummary  = ref({ totalAppt: 0, completed: 0, waiting: 0, cancelled: 0 })
const doctorRank    = ref([])
const isDemoRank    = ref(false)
const contractStats = ref({ active: 0, newThisMonth: 0, pending: 0 })
const followUpStats = ref({ total: 0,  completed: 0,    overdue: 0 })
const notices       = ref([])
const exporting     = ref(false)
const trendOption   = ref({})
const pieOption     = ref({})

// ────────────────────────────────────────────────
// 核心图表调用（支持日期范围）
// ────────────────────────────────────────────────
async function loadCharts(startDate, endDate) {
  const params = { startDate, endDate }

  // 近 N 天就诊趋势
  try {
    const trendRes = await request.get('/admin/report/visit-trend', { params })
    const trend = trendRes.data || {}
    trendOption.value = {
      tooltip: { trigger: 'axis' },
      grid:    { left: 40, right: 20, top: 30, bottom: 30 },
      xAxis:   { type: 'category', data: (trend.dates || []).map(d => d.slice(5)) },
      yAxis:   { type: 'value', minInterval: 1 },
      series: [{
        data: trend.counts || [],
        type: 'line', smooth: true,
        areaStyle: { color: 'rgba(47, 107, 87, 0.12)' },
        lineStyle: { color: 'var(--primary)', width: 3 },
        itemStyle: { color: 'var(--primary)' },
        // ① 节点上方显示数字标注
        label: {
          show: true,
          position: 'top',
          fontSize: 11,
          color: 'var(--primary)',
          formatter: ({ value }) => value > 0 ? value : ''
        }
      }]
    }
  } catch { /* ok */ }

  // ② 科室就诊分布（联动日期）
  try {
    const deptRes = await request.get('/admin/report/dept-load', { params })
    const deptData = deptRes.data?.deptData || []
    pieOption.value = {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend:  { bottom: 0, textStyle: { fontSize: 11 } },
      series: [{
        type: 'pie', radius: ['38%', '65%'],
        center: ['50%', '45%'],
        data: deptData,
        emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.4)' } },
        label: { formatter: '{b}\n{d}%', fontSize: 11 }
      }]
    }
  } catch { /* ok */ }

  // ③ 接诊概况（用结束日）
  try {
    const sumRes = await request.get('/admin/report/daily-summary', { params: { date: endDate } })
    const s = sumRes.data || {}
    todaySummary.value = {
      totalAppt: s.totalAppt ?? 0,
      completed: s.completed ?? 0,
      waiting:   s.waiting   ?? 0,
      cancelled: s.cancelled ?? 0
    }
    statCards.value[4].value = s.totalAppt ?? '-'
  } catch { /* ok */ }
}

// ────────────────────────────────────────────────
// 日期切换回调
// ────────────────────────────────────────────────
async function onDateRangeChange(val) {
  if (!val || val.length < 2) return
  await loadCharts(val[0], val[1])
}

// ────────────────────────────────────────────────
// 初始化（固定数据 + 图表）
// ────────────────────────────────────────────────
onMounted(async () => {
  // 固定卡片数据（累计，不跟日期走）
  try {
    const staffRes = await request.get('/admin/staff', { params: { page: 1, size: 1 } })
    statCards.value[2].value = staffRes.data?.total ?? '-'
  } catch { /* ok */ }

  try {
    const noticeRes = await request.get('/admin/notice', { params: { page: 1, size: 5 } })
    notices.value = noticeRes.data?.records || []
    statCards.value[3].value = noticeRes.data?.total ?? '-'
  } catch { /* ok */ }

  try {
    const cRes = await request.get('/admin/report/contract')
    const cd = cRes.data || {}
    contractStats.value = {
      active: cd.activeCount ?? 0,
      newThisMonth: cd.monthNew  ?? 0,
      pending: cd.pendingCount ?? 0
    }
    statCards.value[0].value = cd.activeCount ?? '-'
  } catch { /* ok */ }

  try {
    const fRes = await request.get('/admin/report/follow-up')
    const fd = fRes.data || {}
    followUpStats.value = {
      total:     fd.activePlans     ?? 0,
      completed: fd.completedPlans  ?? 0,
      overdue:   fd.overduePlans    ?? 0
    }
    statCards.value[1].value = fd.activePlans ?? '-'
  } catch { /* ok */ }

  // 医生排行（本周，与日期范围无关）
  try {
    const rankRes = await request.get('/admin/report/doctor-workload')
    doctorRank.value = rankRes.data || []
    isDemoRank.value = false
  } catch {
    isDemoRank.value = true
    doctorRank.value = [
      { name: '李医生', count: 28 },
      { name: '赵医生', count: 15 },
      { name: '王医生', count: 8  }
    ]
  }

  // 默认近7天图表
  await loadCharts(defaultStart, defaultEnd)
})

function formatDate(dt) {
  return dt ? new Date(dt).toLocaleDateString('zh-CN') : ''
}

async function exportReport() {
  exporting.value = true
  try {
    const res = await request.post('/admin/report/export', {}, { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'report_overview.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
  } catch { /* ok */ }
  finally { exporting.value = false }
}
</script>

<style scoped>
.dashboard { padding: 20px; }

.page-subtitle { margin: 0; font-size: 13px; color: var(--muted); }

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 图表行间距 */
.chart-row { margin-bottom: 16px; }

/* 面板标题行（标题 + 日期提示） */
.panel-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
}
.panel-date-hint {
  font-size: 11px;
  color: var(--muted);
  font-weight: 400;
}

/* 公告项 */
.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
}
.notice-title { color: var(--text); }
.notice-time  { font-size: 12px; color: var(--muted); white-space: nowrap; margin-left: 8px; }

/* 列表行 */
.list-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 9px 0;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
}

.text-good   { color: var(--good);   }
.text-warn   { color: var(--warn);   }
.text-muted  { color: var(--muted);  }
.text-danger { color: var(--danger); }

/* 医生排行行 */
.rank-row {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 0; border-bottom: 1px solid var(--border); font-size: 13px;
}
.rank-badge {
  width: 22px; height: 22px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700;
  background: var(--surface-soft); color: var(--muted);
}
.rank-badge.top { background: var(--primary); color: #fff; }
.rank-name  { flex: 1; }
.rank-count { font-weight: 600; font-variant-numeric: tabular-nums; color: var(--text); }

/* 移动端适配 */
@media (max-width: 768px) {
  .dashboard { padding: 12px; }
  .header-actions { flex-wrap: wrap; }
}
</style>
