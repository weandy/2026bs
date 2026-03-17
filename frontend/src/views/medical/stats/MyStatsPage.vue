<template>
  <div class="stats-page"><div class="page-header"><h2>工作量统计</h2></div>
    <div class="stats-grid">
      <div class="stat-card"><span class="stat-label">本月接诊</span><strong>{{ stats.visitCount }}</strong></div>
      <div class="stat-card"><span class="stat-label">本月处方</span><strong>{{ stats.prescCount }}</strong></div>
    </div>
    <div class="chart-card"><strong>近7天接诊趋势</strong><div ref="chartRef" class="chart-area"></div></div>
  </div>
</template>
<script setup>
import { ref, onMounted, nextTick } from 'vue'
import request from '@/utils/request'
import * as echarts from 'echarts'
const stats = ref({ visitCount: 0, prescCount: 0, trend: [] })
const chartRef = ref(null)
onMounted(async () => {
  try { const res = await request.get('/medical/stats/my'); stats.value = res.data || {} } catch {}
  await nextTick()
  if (chartRef.value && stats.value.trend?.length) {
    const chart = echarts.init(chartRef.value)
    chart.setOption({
      grid: { top: 20, right: 16, bottom: 30, left: 40 },
      xAxis: { type: 'category', data: stats.value.trend.map(t => String(t.date).substring(5)) },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: stats.value.trend.map(t => t.count), itemStyle: { color: '#2f6b57', borderRadius: [4,4,0,0] } }],
      tooltip: { trigger: 'axis' }
    })
  }
})
</script>
<style scoped>
.stats-page { padding: 20px; }
.stats-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 16px; }
.stat-card { background: var(--surface); border: 1px solid var(--border); border-radius: 14px; padding: 20px; text-align: center; }
.stat-label { font-size: 13px; color: var(--muted); display: block; margin-bottom: 4px; }
.stat-card strong { font-size: 32px; font-weight: 700; color: var(--primary); font-variant-numeric: tabular-nums; }
.chart-card { background: var(--surface); border: 1px solid var(--border); border-radius: 14px; padding: 16px; }
.chart-card strong { font-size: 14px; display: block; margin-bottom: 10px; }
.chart-area { height: 240px; }
</style>
