<template>
  <div class="dashboard">

    <!-- 页面头部 —— 标题由 AdminLayout topbar 提供，此处仅保留副标题与操作按钮 -->
    <div class="page-header">
      <p class="page-subtitle">{{ todayStr }} · 数据概览</p>
      <el-button type="success" size="small" :loading="exporting" @click="exportReport">
        <PackageSearch :size="14" style="margin-right:4px" /> 导出报表
      </el-button>
    </div>


    <!-- 指标卡 -->
    <div class="summary-grid">
      <div v-for="card in statCards" :key="card.label"
        :class="['metric-card-v2', card.cls]">
        <div class="mc-label">{{ card.label }}</div>
        <div class="mc-value">{{ card.value }}</div>
        <div class="mc-trend" v-if="card.hint">{{ card.hint }}</div>
      </div>
    </div>

    <!-- 图表行 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :sm="14">
        <div class="panel">
          <h4>近7天就诊趋势</h4>
          <v-chart :option="trendOption" style="height: 280px;" autoresize />
        </div>
      </el-col>
      <el-col :xs="24" :sm="10">
        <div class="panel">
          <h4>科室就诊分布</h4>
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
            <div class="list-row"><span>本月随访计划</span><strong>{{ followUpStats.total || 0 }}</strong></div>
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
          <h4>今日接诊概况 <el-tag v-if="todaySummary.estimated" size="small" type="warning" style="margin-left:6px">估算</el-tag></h4>
          <div class="panel-scroll-body" style="max-height:220px">
            <div class="list-row"><span>今日挂号总量</span><strong>{{ todaySummary.totalAppt }}</strong></div>
            <div class="list-row"><span>已完成就诊</span><strong class="text-good">{{ todaySummary.completed }}</strong></div>
            <div class="list-row"><span>候诊中</span><strong class="text-warn">{{ todaySummary.waiting }}</strong></div>
            <div class="list-row"><span>爽约/取消</span><strong class="text-muted">{{ todaySummary.cancelled }}</strong></div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12">
        <div class="panel">
          <h4>医生工作量排行 (本周) <el-tag v-if="isDemoRank" size="small" type="info" style="margin-left:6px">演示</el-tag></h4>
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
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { PackageSearch, TriangleAlert } from 'lucide-vue-next'

use([CanvasRenderer, BarChart, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])

const todayStr = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })

const statCards = ref([
  { label: '签约人数', value: '-', cls: 'mc-success', hint: '当前生效签约' },
  { label: '随访计划', value: '-', cls: 'mc-primary', hint: '进行中计划' },
  { label: '员工总数', value: '-', cls: 'mc-warn',    hint: '' },
  { label: '系统公告', value: '-', cls: 'mc-danger',  hint: '' }
])

const todaySummary = ref({ totalAppt: 0, completed: 0, waiting: 0, cancelled: 0, estimated: false })
const doctorRank = ref([])
const isDemoRank = ref(false)

const contractStats = ref({ active: 0, newThisMonth: 0, pending: 0 })
const followUpStats = ref({ total: 0, completed: 0, overdue: 0 })
const notices = ref([])
const exporting = ref(false)
const trendOption = ref({})
const pieOption = ref({})
const barOption = ref({})

function formatDate(dt) {
  return dt ? new Date(dt).toLocaleDateString('zh-CN') : ''
}

onMounted(async () => {
  try {
    const overview = await request.get('/admin/report/overview')
    const d = overview.data || {}

    const staffRes = await request.get('/admin/staff', { params: { page: 1, size: 1 } })
    statCards.value[2].value = staffRes.data?.total ?? '-'

    const noticeRes = await request.get('/admin/notice', { params: { page: 1, size: 5 } })
    notices.value = noticeRes.data?.records || []
    statCards.value[3].value = noticeRes.data?.total ?? '-'

    // 签约统计
    try {
      const cRes = await request.get('/admin/report/contract')
      const cd = cRes.data || {}
      contractStats.value = { active: cd.activeCount || 0, newThisMonth: cd.monthNew || 0, pending: cd.pendingCount || 0 }
      statCards.value[0].value = cd.activeCount ?? '-'
    } catch { /* ok */ }

    // 随访统计
    try {
      const fRes = await request.get('/admin/report/follow-up')
      const fd = fRes.data || {}
      followUpStats.value = { total: fd.totalPlans || 0, completed: fd.completedPlans || 0, overdue: fd.overduePlans || 0 }
      statCards.value[1].value = fd.totalPlans ?? '-'
    } catch { /* ok */ }

    // 今日接诊概况（前端用已有接口简单聚合）
    try {
      const apptRes = await request.get('/admin/report/visit-trend')
      const counts = apptRes.data?.counts || []
      const todayCount = counts.length > 0 ? counts[counts.length - 1] : 0
      todaySummary.value = {
        totalAppt: todayCount,
        completed: Math.round(todayCount * 0.6),
        waiting: Math.round(todayCount * 0.3),
        cancelled: Math.round(todayCount * 0.1),
        estimated: true  // 前端估算，非真实统计
      }
    } catch(e) { /* ok */ }

    // 医生工作量排行
    try {
      const rankRes = await request.get('/admin/report/doctor-workload')
      doctorRank.value = rankRes.data || []
    } catch(e) {
      // 接口不可用时使用演示数据
      isDemoRank.value = true
      doctorRank.value = [
        { name: '李医生', count: 28 },
        { name: '赵医生', count: 15 },
        { name: '王医生', count: 8 },
      ]
    }

    // 近7天趋势
    const trendRes = await request.get('/admin/report/visit-trend')
    const trend = trendRes.data || {}
    trendOption.value = {
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: (trend.dates || []).map(d => d.slice(5)) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        data: trend.counts || [],
        type: 'line', smooth: true,
        areaStyle: { color: 'rgba(47, 107, 87, 0.12)' },
        lineStyle: { color: 'var(--primary)', width: 3 },
        itemStyle: { color: 'var(--primary)' }
      }]
    }

    // 科室分布
    const deptRes = await request.get('/admin/report/dept-load')
    pieOption.value = {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        data: deptRes.data?.deptData || [],
        emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } },
        label: { formatter: '{b}\n{d}%' }
      }]
    }

    // 去除药品消耗 TOP5 图表（药品模块已删除）
  } catch (e) { /* handled */ }
})

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
  } catch (e) { /* handled */ }
  finally { exporting.value = false }
}
</script>

<style scoped>
.dashboard { padding: 20px; }

/* page-header 已迁入全局 components.css，这里只保留本页私有的 */
.page-subtitle { margin: 0; font-size: 13px; color: var(--muted); }

/* 预警条操作链接 */
.alert-action {
  margin-left: auto;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-warning);
  text-decoration: none;
  white-space: nowrap;
}
.alert-action:hover { text-decoration: underline; }

/* summary-grid 已迁入全局，此处仅保留图表行间距 */
.chart-row { margin-bottom: 16px; }

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
.notice-time { font-size: 12px; color: var(--muted); white-space: nowrap; margin-left: 8px; }

/* 预警行 */
.alert-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
}
.alert-qty { color: var(--danger); font-weight: 600; font-variant-numeric: tabular-nums; }
.more-link { display: block; text-align: center; font-size: 12px; color: var(--primary); margin-top: 8px; }

/* 今日接诊概况 KV 行 */
.kv-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
}
.kv-row strong { font-variant-numeric: tabular-nums; }
.text-good { color: var(--good); }
.text-warn { color: var(--warn); }
.text-muted { color: var(--muted); }

/* 医生排行行 */
.rank-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
}
.rank-badge {
  width: 22px; height: 22px; border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700;
  background: var(--surface-soft); color: var(--muted);
}
.rank-badge.top { background: var(--primary); color: #fff; }
.rank-name { flex: 1; }
.rank-count { font-weight: 600; font-variant-numeric: tabular-nums; color: var(--text); }

/* 移动端适配 */
@media (max-width: 768px) {
  .dashboard { padding: 12px; }
  .metric-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
