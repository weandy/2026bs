<template>
  <div class="queue-progress-page">
    <div class="page-header">
      <button class="back-btn" @click="$router.back()">
        <ChevronLeft :size="18" /> 返回
      </button>
      <h3>候诊进度</h3>
    </div>

    <!-- 当前叫号 -->
    <div class="current-call-card" v-if="currentCall">
      <span class="call-label">当前叫号</span>
      <strong class="call-number">{{ currentCall.patientName }}</strong>
      <span class="call-no">{{ currentCall.apptNo }}</span>
    </div>

    <!-- 我的排队状态 -->
    <div class="my-status-card" v-if="myPosition !== null">
      <div class="status-row">
        <span class="status-label">您的排队状态</span>
        <span :class="['status-tag', myPosition === 0 ? 'in-progress' : 'pending']">
          {{ myPosition === 0 ? '轮到您了' : '等待中' }}
        </span>
      </div>
      <div class="position-display">
        <div class="position-number">
          <span class="big-num">{{ myPosition }}</span>
          <span class="unit">人</span>
        </div>
        <span class="position-hint">前方等待人数</span>
      </div>
      <div class="estimate-row" v-if="myPosition > 0">
        <Clock :size="14" />
        <span>预计等待约 {{ myPosition * 8 }} - {{ myPosition * 12 }} 分钟</span>
      </div>
    </div>

    <!-- 候诊列表 -->
    <div class="queue-card">
      <div class="card-title-row">
        <strong>候诊队列</strong>
        <span class="muted">共 {{ waitingList.length }} 人</span>
      </div>
      <div v-for="(item, idx) in waitingList" :key="item.id" class="queue-item"
        :class="{ highlight: item.id === myApptId }">
        <span class="queue-idx">{{ idx + 1 }}</span>
        <span class="queue-name">{{ maskName(item.patientName) }}</span>
        <span :class="['status-tag', queueStatusClass(item.status)]">{{ queueStatusLabel(item.status) }}</span>
      </div>
      <div v-if="waitingList.length === 0" class="empty-tip">暂无候诊人员</div>
    </div>

    <!-- 自动刷新提示 -->
    <div class="refresh-bar">
      <RefreshCcw :size="12" :class="{ spinning: refreshing }" />
      <span>{{ countdown }}s 后自动刷新</span>
      <button class="refresh-btn" @click="loadQueue">立即刷新</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ChevronLeft, Clock, RefreshCcw } from 'lucide-vue-next'

const route = useRoute()
const deptCode = computed(() => route.query.deptCode || 'QKMZ')
const myApptId = computed(() => Number(route.query.apptId) || null)

const queueData = ref([])
const refreshing = ref(false)
const countdown = ref(15)
let timer = null
let countdownTimer = null

const currentCall = computed(() => queueData.value.find(q => q.status === 2))

const waitingList = computed(() =>
  queueData.value.filter(q => q.status <= 2).sort((a, b) => a.status === 2 ? -1 : b.status === 2 ? 1 : 0)
)

const myPosition = computed(() => {
  if (!myApptId.value) return null
  const idx = waitingList.value.findIndex(q => q.id === myApptId.value)
  if (idx === -1) return null
  // count how many are before me (excluding currently being seen)
  return waitingList.value.slice(0, idx).filter(q => q.status === 1).length
})

function maskName(name) {
  if (!name || name.length <= 1) return name
  return name[0] + '*'.repeat(name.length - 1)
}

function queueStatusLabel(s) {
  return { 1: '待叫号', 2: '候诊中', 3: '就诊中' }[s] || '未知'
}
function queueStatusClass(s) {
  return { 1: 'pending', 2: 'in-progress', 3: 'done' }[s] || 'pending'
}

async function loadQueue() {
  refreshing.value = true
  try {
    const res = await request.get(`/medical/screen/${deptCode.value}`)
    queueData.value = res.data || []
  } catch (e) {
    console.warn('候诊进度加载失败', e)
  } finally {
    refreshing.value = false
    countdown.value = 15
  }
}

onMounted(() => {
  loadQueue()
  timer = setInterval(loadQueue, 15000)
  countdownTimer = setInterval(() => {
    if (countdown.value > 0) countdown.value--
  }, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
  clearInterval(countdownTimer)
})
</script>

<style scoped>
.queue-progress-page { padding: 16px; max-width: 500px; margin: 0 auto; }

.page-header {
  display: flex; align-items: center; gap: 8px; margin-bottom: 16px;
}
.page-header h3 { font-size: 18px; color: var(--text-strong); margin: 0; }
.back-btn {
  display: flex; align-items: center; gap: 2px;
  background: none; border: none; color: var(--primary); font-size: 14px;
  cursor: pointer; padding: 4px 8px; border-radius: 8px;
  font-family: var(--font-sans);
}
.back-btn:hover { background: var(--primary-light); }

/* 当前叫号 */
.current-call-card {
  background: linear-gradient(140deg, var(--primary), #43826d);
  border-radius: 16px; padding: 20px; color: #fff;
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  margin-bottom: 14px;
}
.call-label { font-size: 12px; opacity: 0.8; }
.call-number { font-size: 24px; font-weight: 700; }
.call-no { font-size: 13px; opacity: 0.7; }

/* 我的状态 */
.my-status-card {
  background: var(--surface); border: 1px solid var(--border);
  border-radius: 16px; padding: 20px; margin-bottom: 14px;
}
.status-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.status-label { font-size: 14px; font-weight: 600; color: var(--text-strong); }

.position-display { text-align: center; margin-bottom: 12px; }
.position-number { display: flex; align-items: baseline; justify-content: center; gap: 4px; }
.big-num { font-size: 48px; font-weight: 700; color: var(--primary); font-variant-numeric: tabular-nums; }
.unit { font-size: 16px; color: var(--muted); }
.position-hint { font-size: 13px; color: var(--muted); }

.estimate-row {
  display: flex; align-items: center; gap: 6px; justify-content: center;
  font-size: 13px; color: var(--muted);
  padding: 8px 12px; background: var(--surface-soft); border-radius: 8px;
}

/* 候诊队列 */
.queue-card {
  background: var(--surface); border: 1px solid var(--border);
  border-radius: 16px; padding: 16px; margin-bottom: 14px;
}
.card-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.card-title-row strong { font-size: 15px; color: var(--text); }
.muted { color: var(--muted); font-size: 13px; }

.queue-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 12px; border-radius: 10px; margin-bottom: 4px;
  background: var(--surface-soft);
}
.queue-item.highlight { background: rgba(47,107,87,0.08); border: 1px solid var(--primary); }
.queue-idx { width: 24px; height: 24px; border-radius: 50%; background: var(--border); display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; color: var(--muted); flex-shrink: 0; }
.queue-name { flex: 1; font-size: 14px; color: var(--text); }
.empty-tip { text-align: center; padding: 20px; color: var(--muted); font-size: 13px; }

/* 刷新条 */
.refresh-bar {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  font-size: 12px; color: var(--muted); padding: 8px 0;
}
.refresh-btn {
  background: none; border: none; color: var(--primary); cursor: pointer;
  font-size: 12px; font-family: var(--font-sans);
}
.refresh-btn:hover { text-decoration: underline; }

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.spinning { animation: spin 1s linear infinite; }
</style>
