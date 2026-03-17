<template>
  <div class="home-page">

    <!-- ①居民 Hero -->
    <div class="resident-hero">
      <div class="hero-top-row">
        <span class="hero-badge">{{ greeting }}，{{ userStore.userInfo?.name || '居民' }}</span>
        <button class="elder-toggle-btn" @click="uiStore.toggleElder()">
          当前：{{ uiStore.elderMode ? '敬老版' : '标准版' }} · 切换
        </button>
      </div>
      <h4>{{ heroTitle }}</h4>
      <p>{{ heroSub }}</p>
    </div>

    <!-- ② 今日提醒 -->
    <div class="home-card" v-if="reminders.length > 0">
      <div class="card-title-row">
        <strong><Bell :size="14" class="title-icon" /> 今日提醒</strong>
      </div>
      <div v-for="(r, i) in reminders" :key="i" class="reminder-item">
        <component :is="r.icon" :size="14" class="reminder-icon" />
        <span>{{ r.text }}</span>
      </div>
    </div>

    <!-- ③ 快捷服务 -->
    <div class="home-card">
      <div class="card-title-row">
        <strong>快捷服务</strong>
        <span class="muted">全部服务</span>
      </div>
      <div class="shortcut-grid">
        <div
          v-for="item in shortcuts"
          :key="item.path"
          class="shortcut-item"
          @click="router.push(item.path)"
        >
          <span class="shortcut-icon" :style="{ background: item.bgColor, color: item.color }">
            <component :is="item.icon" :size="20" />
          </span>
          <span>{{ item.label }}</span>
        </div>
      </div>
    </div>

    <!-- ④ 健康摘要 -->
    <div class="home-card" v-if="healthSummary">
      <div class="card-title-row">
        <strong><Activity :size="14" class="title-icon" /> 健康摘要</strong>
        <span class="muted link" @click="router.push('/resident/health-record')">查看详情</span>
      </div>
      <div class="health-grid">
        <div v-for="v in healthVitals" :key="v.label" class="health-item">
          <span class="health-label">{{ v.label }}</span>
          <span class="health-value">{{ v.value || '--' }}</span>
        </div>
      </div>
      <div v-if="healthSummary.chronicles?.length" class="chronic-tags-row">
        <span v-for="c in healthSummary.chronicles" :key="c"
          :class="['chronic-tag', c]">{{ chronicLabel(c) }}</span>
      </div>
    </div>

    <!-- ⑤ 我的预约 -->
    <div class="home-card">
      <div class="card-title-row">
        <strong>我的预约</strong>
        <span class="muted link" @click="router.push('/resident/appointment')">查看全部</span>
      </div>
      <div v-if="recentAppts.length === 0" class="empty-tip">暂无预约记录</div>
      <div v-for="appt in recentAppts" :key="appt.id" class="list-item">
        <div>
          <strong class="item-title">{{ appt.deptName }} · {{ appt.staffName }}</strong>
          <span class="item-meta">
            {{ appt.apptDate }}
            {{ { 1: '上午', 2: '下午', 3: '晚上' }[appt.timePeriod] || '' }}
          </span>
        </div>
        <span :class="['status-tag', statusClass(appt.status)]">{{ statusLabel(appt.status) }}</span>
      </div>
    </div>

    <!-- ⑥ 消息通知 -->
    <div class="home-card">
      <div class="card-title-row">
        <strong>消息通知 <span v-if="unreadCount > 0" class="badge-count">{{ unreadCount }}</span></strong>
        <span class="muted link" @click="router.push('/resident/message')">查看全部</span>
      </div>
      <div v-if="recentMsgs.length === 0" class="empty-tip">暂无消息</div>
      <div v-for="msg in recentMsgs" :key="msg.id" class="msg-line" :class="{ unread: msg.isRead === 0 }">
        {{ msg.content || msg.title }}
      </div>
    </div>

    <!-- A5 社区公告 -->
    <div class="home-card" v-if="notices.length > 0">
      <div class="card-title-row">
        <strong>社区公告</strong>
      </div>
      <div v-for="notice in notices" :key="notice.id" class="notice-item">
        <div class="notice-title-row">
          <span v-if="notice.isTop" class="top-badge">置顶</span>
          <strong>{{ notice.title }}</strong>
        </div>
        <p class="notice-content">{{ notice.content }}</p>
        <span class="notice-date">{{ notice.publishedAt?.split('T')[0] || '' }}</span>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useUiStore } from '@/stores/uiStore'
import {
  CalendarDays, FileText, Clipboard, Syringe, Bell, UserRound,
  Activity, AlertCircle, Users, Newspaper, ClipboardList
} from 'lucide-vue-next'

const router    = useRouter()
const userStore = useUserStore()
const uiStore   = useUiStore()

const recentAppts = ref([])
const recentMsgs  = ref([])
const unreadCount = ref(0)
const healthSummary = ref(null)
const notices = ref([])

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6)  return '凌晨好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const heroTitle = computed(() => {
  const n = unreadCount.value
  if (n > 0) return `您有 ${n} 条健康消息`
  return '社区卫生服务中心'
})

const heroSub = computed(() => {
  const n = unreadCount.value
  return n > 0 ? `请及时查看` : '预约挂号 · 健康档案 · 慢病随访 · 公共卫生'
})

const shortcuts = [
  { path: '/resident/appointment',   icon: CalendarDays, label: '预约挂号', color: 'var(--primary)', bgColor: 'rgba(47,107,87,0.1)' },
  { path: '/resident/health-record', icon: FileText,     label: '健康档案', color: 'var(--medical)', bgColor: 'rgba(26,107,181,0.1)' },
  { path: '/resident/visit-records', icon: Clipboard,    label: '就诊记录', color: 'var(--accent-purple)', bgColor: 'rgba(125,60,152,0.1)' },
  { path: '/resident/vaccine',       icon: Syringe,      label: '疫苗接种', color: 'var(--warn)', bgColor: 'rgba(230,126,34,0.1)' },
  { path: '/resident/family',        icon: Users,        label: '家庭成员', color: '#2980b9', bgColor: 'rgba(41,128,185,0.1)' },
  { path: '/resident/article',       icon: Newspaper,    label: '健康资讯', color: '#27ae60', bgColor: 'rgba(39,174,96,0.1)' },
  { path: '/resident/follow-up',     icon: ClipboardList,label: '我的随访', color: '#8e44ad', bgColor: 'rgba(142,68,173,0.1)' },
  { path: '/resident/message',       icon: Bell,         label: '消息通知', color: 'var(--danger)', bgColor: 'rgba(192,57,43,0.1)' },
  { path: '/resident/profile',       icon: UserRound,    label: '个人中心', color: 'var(--neutral-500)', bgColor: 'rgba(93,109,126,0.1)' },
]

function statusLabel(s) {
  return { 1: '待就诊', 2: '候诊中', 3: '就诊中', 4: '已完成', 5: '已取消' }[s] || '未知'
}
function statusClass(s) {
  return { 1: 'pending', 2: 'in-progress', 3: 'in-progress', 4: 'done', 5: 'cancelled' }[s] || 'pending'
}
function chronicLabel(c) {
  return { hypertension: '高血压', diabetes: '糖尿病', chd: '冠心病', copd: '慢阻肺', stroke: '脑卒中' }[c] || c
}

/* ── 今日提醒（从预约和消息中提炼） ── */
const reminders = computed(() => {
  const list = []
  const upcoming = recentAppts.value.filter(a => a.status === 1 || a.status === 2)
  if (upcoming.length > 0) {
    list.push({ icon: CalendarDays, text: `您有 ${upcoming.length} 个待就诊预约` })
  }
  if (unreadCount.value > 0) {
    list.push({ icon: Bell, text: `${unreadCount.value} 条未读消息` })
  }
  return list
})

/* ── 健康摘要 ── */
const healthVitals = computed(() => {
  if (!healthSummary.value) return []
  const h = healthSummary.value
  return [
    { label: '血压', value: h.bloodPressure },
    { label: '血糖', value: h.bloodGlucose },
    { label: '体重', value: h.weight ? `${h.weight} kg` : '' },
    { label: '最近就诊', value: h.lastVisitDate },
  ]
})

onMounted(async () => {
  try {
    const [apptRes, msgRes, unreadRes] = await Promise.all([
      request.get('/resident/appointment', { params: { page: 1, size: 3 } }).catch(() => ({ data: {} })),
      request.get('/resident/message',     { params: { page: 1, size: 3 } }).catch(() => ({ data: {} })),
      request.get('/resident/message/unread-count').catch(() => ({ data: {} })),
    ])
    recentAppts.value = apptRes.data?.records  || apptRes.data?.data?.records || []
    recentMsgs.value  = msgRes.data?.records   || msgRes.data?.data?.records  || []
    unreadCount.value = unreadRes.data?.count  || unreadRes.data?.data?.count  || 0

    // 健康摘要（优雅降级）
    try {
      const healthRes = await request.get('/resident/health-record/summary')
      healthSummary.value = healthRes.data || null
    } catch { /* 接口不存在时静默 */ }

    // A5: 加载社区公告
    try {
      const noticeRes = await request.get('/admin/sys/notice', { params: { page: 1, size: 3 } })
      notices.value = (noticeRes.data?.records || []).filter(n => n.status === 1)
    } catch { /* 静默 */ }
  } catch (e) { console.warn(e) }
})
</script>

<style scoped>
.home-page {
  padding: 16px 16px 8px;
  max-width: 500px;
  margin: 0 auto;
}

/* ── Hero 顶行 ── */
.hero-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.elder-toggle-btn {
  padding: 4px 12px;
  border-radius: 999px;
  border: 1px solid rgba(255,255,255,0.35);
  background: rgba(255,255,255,0.14);
  color: rgba(255,255,255,0.88);
  font-size: 11px;
  cursor: pointer;
  font-family: var(--font-sans);
  transition: all 0.2s ease;
}
.elder-toggle-btn:hover { background: rgba(255,255,255,0.22); }

/* ── 居民 Hero ── */
.resident-hero {
  background: linear-gradient(140deg, var(--primary), #43826d);
  border-radius: 20px;
  padding: 20px;
  color: #fff;
  margin-bottom: 14px;
  position: relative;
  overflow: hidden;
}
.resident-hero::before {
  content: '';
  position: absolute;
  width: 160px; height: 160px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  top: -40px; right: -30px;
}
.resident-hero::after {
  content: '';
  position: absolute;
  width: 100px; height: 100px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.04);
  bottom: -20px; left: -20px;
}

.hero-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 12px;
  position: relative; z-index: 1;
}
.resident-hero h4 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.3;
  position: relative; z-index: 1;
}
.resident-hero p {
  margin: 0;
  font-size: 13px;
  opacity: 0.88;
  position: relative; z-index: 1;
}

/* ── 今日提醒 ── */
.reminder-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 10px;
  background: var(--bg-warning);
  color: var(--text-warning);
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 6px;
}
.reminder-item:last-child { margin-bottom: 0; }
.reminder-icon { flex-shrink: 0; }
.title-icon { vertical-align: -2px; margin-right: 2px; }

/* ── 通用 home 卡片 ── */
.home-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 16px;
  margin-bottom: 14px;
}
.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.card-title-row strong { font-size: 15px; color: var(--text); }

/* ── 快捷服务 ── */
.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
@media (max-width: 400px) {
  .shortcut-grid { grid-template-columns: repeat(2, 1fr); }
}
.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 8px;
  border-radius: 14px;
  background: var(--surface-soft);
  font-size: 12px;
  font-weight: 600;
  color: var(--primary-strong);
  cursor: pointer;
  transition: all 0.2s ease;
}
.shortcut-item:hover {
  background: rgba(47, 107, 87, 0.1);
  transform: translateY(-2px);
}
.shortcut-icon {
  width: 44px; height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease;
}

/* ── 健康摘要 ── */
.health-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 8px;
}
.health-item {
  background: var(--surface-soft);
  border-radius: 10px;
  padding: 10px 12px;
}
.health-label { font-size: 11px; color: var(--muted); display: block; }
.health-value { font-size: 16px; font-weight: 700; color: var(--text); font-variant-numeric: tabular-nums; }
.chronic-tags-row { display: flex; gap: 6px; flex-wrap: wrap; }

/* ── 预约列表项 ── */
.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  background: var(--surface-soft);
  margin-bottom: 8px;
}
.list-item:last-child { margin-bottom: 0; }
.item-title { display: block; }
.item-meta { font-size: 12px; color: var(--muted); }

/* ── 消息行 ── */
.msg-line {
  padding: 12px 14px;
  border-radius: 12px;
  background: var(--surface-soft);
  font-size: 13px;
  line-height: 1.7;
  color: var(--text);
  margin-bottom: 8px;
  border-left: 2px solid transparent;
}
.msg-line.unread {
  border-left-color: var(--primary);
  background: rgba(47, 107, 87, 0.04);
}
.msg-line:last-child { margin-bottom: 0; }

/* ── 公共 ── */
.muted { color: var(--muted); font-size: 13px; }
.link  { cursor: pointer; }
.link:hover { color: var(--primary); }
.empty-tip { color: var(--muted); font-size: 13px; text-align: center; padding: 16px 0; }

/* A5 社区公告 */
.notice-item {
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--surface-soft);
  margin-bottom: 8px;
}
.notice-item:last-child { margin-bottom: 0; }
.notice-title-row { display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.notice-title-row strong { font-size: 14px; color: var(--text-strong); }
.top-badge {
  font-size: 10px; padding: 1px 6px; border-radius: 4px;
  background: var(--danger); color: #fff; font-weight: 600;
}
.notice-content { font-size: 13px; color: var(--text); margin: 0 0 4px; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.notice-date { font-size: 11px; color: var(--muted); }
.badge-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px; height: 18px;
  border-radius: 50%;
  background: var(--danger);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  vertical-align: middle;
  margin-left: 4px;
}
</style>
