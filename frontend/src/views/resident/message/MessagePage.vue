<template>
  <div class="message-page">
    <div class="page-header">
      <div>
        <h2>消息通知
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" style="margin-left: 8px;" />
        </h2>
      </div>
      <el-button type="primary" text @click="markAllRead" :disabled="unreadCount === 0">全部标为已读</el-button>
    </div>

    <!-- 分类Tab -->
    <el-tabs v-model="activeCategory" @tab-change="loadMessages">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="预约提醒" name="appointment" />
      <el-tab-pane label="随访提醒" name="followup" />
      <el-tab-pane label="接种提醒" name="vaccine" />
      <el-tab-pane label="系统通知" name="system" />
    </el-tabs>

    <el-card v-for="msg in messages" :key="msg.id" shadow="hover" class="msg-card"
             :class="{ unread: msg.isRead === 0 }" @click="readMessage(msg)">
      <div class="msg-header">
        <div class="msg-title-row">
          <el-tag v-if="msg.category" size="small" :type="categoryType(msg.category)" style="margin-right:6px">
            {{ categoryText(msg.category) }}
          </el-tag>
          <span class="msg-title">{{ msg.title }}</span>
          <span v-if="msg.isRead === 0" class="unread-dot" />
        </div>
        <span class="msg-time">{{ formatDate(msg.createdAt) }}</span>
      </div>
      <div class="msg-content">{{ msg.content }}</div>
    </el-card>

    <el-empty v-if="messages.length === 0" description="暂无消息" />

    <el-pagination
      v-if="total > 0"
      v-model:current-page="currentPage"
      :page-size="10"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 16px; justify-content: center;"
      @current-change="loadMessages"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const messages = ref([])
const currentPage = ref(1)
const total = ref(0)
const unreadCount = ref(0)
const activeCategory = ref('all')

function categoryType(c) {
  return { appointment: '', followup: 'warning', vaccine: 'success', system: 'info' }[c] || 'info'
}
function categoryText(c) {
  return { appointment: '预约', followup: '随访', vaccine: '接种', system: '系统' }[c] || c
}

function formatDate(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleString('zh-CN')
}

async function loadMessages() {
  try {
    const params = { page: currentPage.value, size: 10 }
    if (activeCategory.value !== 'all') params.category = activeCategory.value
    const res = await request.get('/resident/message', { params })
    messages.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { console.warn(e) }
}

async function loadUnread() {
  try {
    const res = await request.get('/resident/message/unread-count')
    unreadCount.value = res.data?.count || 0
  } catch (e) { console.warn(e) }
}

async function readMessage(msg) {
  if (msg.isRead === 0) {
    await request.put(`/resident/message/${msg.id}/read`)
    msg.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

async function markAllRead() {
  try {
    await request.put('/resident/message/read-all')
    messages.value.forEach(m => m.isRead = 1)
    unreadCount.value = 0
    ElMessage.success('已全部标为已读')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => { loadMessages(); loadUnread() })
</script>

<style scoped>
.msg-card { margin-bottom: 12px; cursor: pointer; transition: border-color 0.2s; }
.msg-card.unread { border-left: 3px solid var(--primary); background: var(--surface-soft); }
.msg-card:hover { border-color: var(--primary); }
.msg-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 4px; }
.msg-title-row { display: flex; align-items: center; }
.msg-title { font-weight: 600; }
.msg-time { font-size: 12px; color: var(--muted); white-space: nowrap; }
.msg-content { font-size: 13px; color: var(--text-secondary); }
.unread-dot {
  width: 6px; height: 6px; border-radius: 50%; background: var(--danger);
  display: inline-block; margin-left: 6px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .message-page { padding: 12px; }
  .msg-header { flex-direction: column; gap: 4px; }
}
</style>
