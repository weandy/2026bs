<template>
  <div class="public-screen">
    <div class="screen-header">
      <h1>候诊信息</h1>
      <p>{{ deptName }} · {{ today }}</p>
    </div>

    <div class="screen-body">
      <div class="current-calling" v-if="callingList.length > 0">
        <h2>正在就诊</h2>
        <div class="calling-card" v-for="a in callingList.filter(x => x.status === 3)" :key="a.id">
          <span class="calling-no">{{ a.apptNo }}</span>
          <span class="calling-name">{{ a.patientName }}</span>
          <span class="calling-doctor">{{ a.staffName }}</span>
        </div>
      </div>

      <div class="waiting-section">
        <h2>候诊列表</h2>
        <div class="waiting-list">
          <div class="waiting-item" v-for="a in waitingList" :key="a.id"
               :class="{ highlight: a.status === 2 }">
            <span class="seq">{{ a.apptNo?.slice(-5) }}</span>
            <span class="name">{{ a.patientName }}</span>
            <el-tag :type="a.status === 2 ? 'warning' : ''" size="small">
              {{ a.status === 2 ? '请就诊' : '等候中' }}
            </el-tag>
          </div>
        </div>
        <el-empty v-if="waitingList.length === 0" description="暂无候诊" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const deptCode = route.params.deptCode
const deptNames = { QKMZ: '全科门诊', ZYMZ: '中医门诊', KQMZ: '口腔门诊', FKMZ: '妇科门诊', EKMZ: '儿科门诊', YFJZ: '预防接种' }
const deptName = deptNames[deptCode] || deptCode
const today = new Date().toLocaleDateString('zh-CN')
const allList = ref([])
const wsConnected = ref(false)

const callingList = computed(() => allList.value.filter(a => a.status === 3))
const waitingList = computed(() => allList.value.filter(a => a.status === 1 || a.status === 2))

async function loadScreen() {
  try {
    const res = await request.get(`/public/screen/${deptCode}`)
    allList.value = res.data || []
  } catch (e) { console.warn(e) }
}

// WebSocket 连接
let ws = null
let reconnectTimer = null
let pollTimer = null

function connectWs() {
  const protocol = location.protocol === 'https:' ? 'wss' : 'ws'
  const wsUrl = `${protocol}://${location.hostname}:8080/api/ws/queue/${deptCode}`
  ws = new WebSocket(wsUrl)
  ws.onopen = () => {
    wsConnected.value = true
    // WS 建立后停止轮询
    if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
  }
  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      if (msg.type === 'refresh') {
        loadScreen()
      } else if (msg.type === 'call') {
        // 叫号提示
        loadScreen()
      }
    } catch (e) { loadScreen() }
  }
  ws.onclose = () => {
    wsConnected.value = false
    // 降级为轮询
    if (!pollTimer) pollTimer = setInterval(loadScreen, 10000)
    // 5秒后重连
    reconnectTimer = setTimeout(connectWs, 5000)
  }
  ws.onerror = () => ws.close()
}

onMounted(() => {
  loadScreen()
  connectWs()
  // 初始也启动轮询，WS 建立后会停止
  pollTimer = setInterval(loadScreen, 10000)
})

onUnmounted(() => {
  if (ws) ws.close()
  if (reconnectTimer) clearTimeout(reconnectTimer)
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.public-screen {
  min-height: 100vh;
  background: linear-gradient(180deg, #1f5644 0%, var(--primary-hover) 100%);
  color: #fff;
  padding: 32px;
  font-family: 'Microsoft YaHei', sans-serif;
}
.screen-header { text-align: center; margin-bottom: 32px; }
.screen-header h1 { font-size: 36px; margin-bottom: 8px; }
.screen-header p { font-size: 18px; opacity: 0.8; }
.screen-body { max-width: 900px; margin: 0 auto; }
.current-calling h2, .waiting-section h2 { font-size: 20px; margin-bottom: 16px; border-bottom: 2px solid var(--primary-soft); padding-bottom: 8px; }
.calling-card {
  background: rgba(255,255,255,0.15);
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 24px;
  margin-bottom: 12px;
}
.calling-no { font-weight: 700; color: #d4edda; }
.calling-name { font-size: 28px; }
.calling-doctor { opacity: 0.7; font-size: 16px; }
.waiting-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; }
.waiting-item {
  background: rgba(255,255,255,0.08);
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.waiting-item.highlight { background: rgba(77, 145, 121, 0.25); border: 1px solid var(--primary-soft); }
.seq { font-weight: 700; font-size: 18px; color: #96c5b3; }
.name { flex: 1; }
</style>
