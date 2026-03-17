<template>
  <div class="follow-up-page">
    <h2>签约与随访</h2>

    <!-- 我的签约 -->
    <div class="section-card">
      <div class="section-title-row">
        <strong>我的签约</strong>
      </div>
      <div v-if="contracts.length === 0" class="empty-tip">暂无签约记录</div>
      <div v-for="c in contracts" :key="c.id" class="contract-item">
        <div class="ci-top">
          <span class="ci-team">{{ c.teamName || '家庭医生团队' }}</span>
          <span :class="['status-tag', c.status === 1 ? 'active' : 'expired']">
            {{ c.status === 1 ? '生效中' : '已过期' }}
          </span>
        </div>
        <div class="ci-meta">
          <span>签约医生：{{ c.doctorName || '--' }}</span>
          <span>有效期：{{ c.startDate?.split('T')[0] || '--' }} ~ {{ c.endDate?.split('T')[0] || '--' }}</span>
        </div>
      </div>
    </div>

    <!-- 我的随访计划 -->
    <div class="section-card">
      <div class="section-title-row">
        <strong>随访计划</strong>
      </div>
      <div v-if="plans.length === 0" class="empty-tip">暂无随访计划</div>
      <div v-for="p in plans" :key="p.id" class="plan-item">
        <div class="pi-top">
          <span :class="['chronic-tag', p.chronicType]">{{ chronicLabel(p.chronicType) }}</span>
          <span class="pi-freq">每 {{ p.frequency }} 天</span>
          <span :class="['status-tag', planStatusClass(p.status)]">{{ planStatusLabel(p.status) }}</span>
        </div>
        <div class="pi-meta">
          <span>责任医生：{{ p.doctorName || '--' }}</span>
          <span>下次随访：{{ p.nextFollowDate?.split('T')[0] || '--' }}</span>
        </div>
      </div>
    </div>

    <!-- 随访记录 -->
    <div class="section-card">
      <div class="section-title-row">
        <strong>随访记录</strong>
      </div>
      <div v-if="records.length === 0" class="empty-tip">暂无随访记录</div>
      <div v-for="r in records" :key="r.id" class="record-item">
        <div class="ri-top">
          <span class="ri-date">{{ r.followDate?.split('T')[0] || r.createdAt?.split('T')[0] || '--' }}</span>
          <span class="ri-method">{{ r.followUpMethod === 1 ? '电话随访' : '门诊随访' }}</span>
        </div>
        <div class="ri-content" v-if="r.content">{{ r.content }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const contracts = ref([])
const plans = ref([])
const records = ref([])

function chronicLabel(c) {
  return { hypertension: '高血压', diabetes: '糖尿病', chd: '冠心病', copd: '慢阻肺', stroke: '脑卒中' }[c] || c || '--'
}
function planStatusLabel(s) {
  return { 0: '待开始', 1: '进行中', 2: '已完成', 3: '已终止' }[s] || '未知'
}
function planStatusClass(s) {
  return { 0: 'pending', 1: 'active', 2: 'done', 3: 'cancelled' }[s] || 'pending'
}

onMounted(async () => {
  try {
    const res = await request.get('/resident/contract/my')
    contracts.value = res.data || []
  } catch (e) { console.warn('加载签约', e) }

  try {
    const res = await request.get('/resident/follow-up/my-plans')
    plans.value = res.data || []
  } catch (e) { console.warn('加载随访计划', e) }

  try {
    const res = await request.get('/resident/follow-up/my-records')
    records.value = res.data || []
  } catch (e) { console.warn('加载随访记录', e) }
})
</script>

<style scoped>
.follow-up-page { padding: 16px; }
.follow-up-page h2 { font-size: 20px; margin: 0 0 16px; color: var(--text); }

.section-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 14px;
}
.section-title-row { margin-bottom: 12px; }
.section-title-row strong { font-size: 15px; color: var(--text); }

.empty-tip { font-size: 13px; color: var(--muted); padding: 12px 0; text-align: center; }

/* 签约 */
.contract-item { padding: 10px 0; border-bottom: 1px solid var(--border-light); }
.contract-item:last-child { border-bottom: none; }
.ci-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.ci-team { font-weight: 600; font-size: 14px; color: var(--text); }
.ci-meta { display: flex; gap: 16px; font-size: 12px; color: var(--muted); }

/* 随访计划 */
.plan-item { padding: 10px 0; border-bottom: 1px solid var(--border-light); }
.plan-item:last-child { border-bottom: none; }
.pi-top { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.pi-freq { font-size: 12px; color: var(--muted); }
.pi-meta { display: flex; gap: 16px; font-size: 12px; color: var(--muted); }

/* 随访记录 */
.record-item { padding: 10px 0; border-bottom: 1px solid var(--border-light); }
.record-item:last-child { border-bottom: none; }
.ri-top { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
.ri-date { font-weight: 600; font-size: 13px; color: var(--text); }
.ri-method { font-size: 12px; color: var(--muted); background: var(--surface-soft); padding: 2px 8px; border-radius: 6px; }
.ri-content { font-size: 13px; color: var(--text-secondary); line-height: 1.5; }

/* Tags */
.status-tag {
  font-size: 11px; padding: 2px 8px; border-radius: 6px; font-weight: 600;
}
.status-tag.active { background: rgba(39,174,96,0.1); color: #27ae60; }
.status-tag.expired { background: rgba(230,126,34,0.1); color: var(--warn); }
.status-tag.done { background: rgba(39,174,96,0.1); color: #27ae60; }
.status-tag.pending { background: rgba(93,109,126,0.1); color: var(--neutral-500); }
.status-tag.cancelled { background: rgba(231,76,60,0.1); color: #e74c3c; }

.chronic-tag {
  font-size: 11px; padding: 2px 8px; border-radius: 6px; font-weight: 600;
  background: rgba(47,107,87,0.1); color: var(--primary);
}
</style>
