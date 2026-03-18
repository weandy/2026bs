<template>
  <div class="work-stats-page">
    <!-- Metric 卡片 -->
    <div class="metric-row">
      <div class="metric-card mc-primary" v-loading="loading">
        <div class="mc-label">本月接诊</div>
        <div class="mc-value">{{ stats.visitCount ?? '—' }}</div>
        <div class="mc-trend">次就诊记录</div>
      </div>
      <div class="metric-card mc-success" v-loading="loading">
        <div class="mc-label">本月处方</div>
        <div class="mc-value">{{ stats.prescCount ?? '—' }}</div>
        <div class="mc-trend">张处方</div>
      </div>
      <div class="metric-card mc-warn">
        <div class="mc-label">日均接诊</div>
        <div class="mc-value">{{ avgDaily }}</div>
        <div class="mc-trend">近7天均值</div>
      </div>
      <div class="metric-card mc-danger">
        <div class="mc-label">7日峰值</div>
        <div class="mc-value">{{ maxDay }}</div>
        <div class="mc-trend">单日最高</div>
      </div>
    </div>

    <!-- 近7天趋势图 -->
    <div class="chart-panel">
      <div class="chart-header">
        <span class="chart-title">近7天接诊趋势</span>
        <el-tag size="small" type="info">{{ today }}</el-tag>
      </div>
      <div class="chart-body">
        <!-- 简单 SVG 折线图（无需引入额外库）-->
        <svg :viewBox="`0 0 ${svgW} ${svgH}`" class="trend-svg" v-if="trendPoints.length">
          <!-- 网格线 -->
          <line v-for="i in 5" :key="i"
            :x1="padL" :y1="padT + (svgH-padT-padB)/5*(i-1)"
            :x2="svgW-padR" :y2="padT + (svgH-padT-padB)/5*(i-1)"
            stroke="#e5e7eb" stroke-width="1" />
          <!-- 折线 -->
          <polyline :points="trendPoints" fill="none" stroke="var(--primary,#409eff)" stroke-width="2.5"
            stroke-linecap="round" stroke-linejoin="round" />
          <!-- 数据点 -->
          <circle v-for="pt in trendDots" :key="pt.x" :cx="pt.x" :cy="pt.y" r="5"
            fill="white" stroke="var(--primary,#409eff)" stroke-width="2.5" />
          <!-- X 轴日期标签 -->
          <text v-for="pt in trendDots" :key="'l'+pt.x" :x="pt.x" :y="svgH-4"
            text-anchor="middle" font-size="11" fill="#9ca3af">{{ pt.label }}</text>
          <!-- 数值标签 -->
          <text v-for="pt in trendDots" :key="'v'+pt.x" :x="pt.x" :y="pt.y - 9"
            text-anchor="middle" font-size="12" fill="var(--primary,#409eff)" font-weight="600">
            {{ pt.count }}
          </text>
        </svg>
        <el-empty v-else description="近7天暂无接诊记录" :image-size="80" />
      </div>
    </div>

    <!-- 说明 -->
    <div class="tip-text">统计数据以当前登录医护账号为准，每次进入页面自动刷新</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'

const stats   = ref({ visitCount: 0, prescCount: 0, trend: [] })
const loading = ref(false)

// SVG 尺寸
const svgW = 700, svgH = 200, padL = 40, padR = 20, padT = 30, padB = 28

const today = new Date().toLocaleDateString('zh-CN', { month:'2-digit', day:'2-digit' })

const avgDaily = computed(() => {
  const t = stats.value.trend || []
  if (!t.length) return '—'
  return (t.reduce((s, d) => s + (Number(d.count) || 0), 0) / t.length).toFixed(1)
})

const maxDay = computed(() => {
  const t = stats.value.trend || []
  if (!t.length) return '—'
  return Math.max(...t.map(d => Number(d.count) || 0))
})

const trendDots = computed(() => {
  const t = stats.value.trend || []
  if (!t.length) return []
  const innerW = svgW - padL - padR
  const innerH = svgH - padT - padB
  const maxCount = Math.max(...t.map(d => Number(d.count) || 0), 1)
  return t.map((d, i) => {
    const x = padL + (t.length === 1 ? innerW/2 : (innerW / (t.length-1)) * i)
    const y = padT + innerH - (innerH * (Number(d.count) || 0) / maxCount)
    const label = String(d.date || '').slice(5)  // MM-DD
    return { x: Math.round(x), y: Math.round(y), count: d.count, label }
  })
})

const trendPoints = computed(() =>
  trendDots.value.map(p => `${p.x},${p.y}`).join(' ')
)

async function load() {
  loading.value = true
  try {
    const res = await request.get('/medical/stats/my')
    stats.value = res.data || {}
  } finally { loading.value = false }
}

onMounted(load)
</script>

<style scoped>
.work-stats-page { padding: 0; }

.metric-row { display:flex; gap:16px; margin-bottom:20px; flex-wrap:wrap; }
.metric-card { flex:1; min-width:140px; background:var(--card-bg,#fff); border-radius:12px; padding:20px; box-shadow:0 2px 8px rgba(0,0,0,.06); }
.mc-label { font-size:13px; color:var(--muted,#9ca3af); margin-bottom:8px; }
.mc-value { font-size:34px; font-weight:700; line-height:1; margin-bottom:6px; }
.mc-trend { font-size:12px; color:var(--muted,#9ca3af); }
.mc-primary .mc-value { color: var(--primary,#409eff); }
.mc-success .mc-value { color: #10b981; }
.mc-warn    .mc-value { color: #f59e0b; }
.mc-danger  .mc-value { color: #ef4444; }

.chart-panel { background:var(--card-bg,#fff); border-radius:12px; padding:20px; box-shadow:0 2px 8px rgba(0,0,0,.06); margin-bottom:14px; }
.chart-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.chart-title  { font-size:15px; font-weight:600; }
.chart-body   { }
.trend-svg    { width:100%; height:200px; display:block; overflow:visible; }

.tip-text { font-size:12px; color:var(--muted,#9ca3af); text-align:center; }
</style>
