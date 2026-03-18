<template>
  <div class="family-page">

    <!-- PC双栏 / 移动单栏 -->
    <div class="family-layout">

      <!-- ══ 左侧：成员列表 ══ -->
      <div class="family-sidebar">
        <div class="sidebar-header">
          <span class="sidebar-title">家庭成员</span>
          <button class="btn-add" @click="openAddForm">
            <Plus :size="14" /> 添加
          </button>
        </div>

        <div v-if="members.length === 0" class="sidebar-empty">
          <Users :size="32" class="empty-icon" />
          <p>暂无家庭成员</p>
          <p class="empty-sub">添加后可代为预约挂号</p>
        </div>

        <div
          v-for="m in members" :key="m.id"
          :class="['member-card', { active: selected?.id === m.id }]"
          @click="selected = m"
        >
          <div class="mc-avatar" :style="{ background: relationColor(m.relation) }">
            {{ m.memberName?.[0] || '?' }}
          </div>
          <div class="mc-info">
            <strong>{{ m.memberName }}</strong>
            <span class="relation-tag">{{ relationLabel(m.relation) }}</span>
          </div>
          <ChevronRight :size="14" class="mc-arrow" />
        </div>
      </div>

      <!-- ══ 右侧：详情面板 ══ -->
      <div class="family-detail" v-if="selected">

        <!-- 头部 -->
        <div class="detail-header">
          <div class="detail-avatar" :style="{ background: relationColor(selected.relation) }">
            {{ selected.memberName?.[0] || '?' }}
          </div>
          <div class="detail-name-wrap">
            <h3>{{ selected.memberName }}</h3>
            <span class="relation-tag-lg">{{ relationLabel(selected.relation) }}</span>
            <span v-if="!selected.linkedResidentId" class="unlinked-tip">未关联平台账号</span>
          </div>
          <div class="detail-header-actions">
            <button class="btn-icon" @click="openEditForm(selected)" title="编辑">
              <Pencil :size="15" />
            </button>
            <button class="btn-icon danger" @click="doDelete(selected.id)" title="删除">
              <Trash2 :size="15" />
            </button>
          </div>
        </div>

        <!-- 基本信息 -->
        <div class="detail-section">
          <div class="section-title">基本信息</div>
          <div class="kv-grid">
            <div class="kv" v-if="selected.memberPhone">
              <span class="kv-label">手机号</span>
              <span>{{ selected.memberPhone }}</span>
            </div>
            <div class="kv" v-if="selected.idCard">
              <span class="kv-label">身份证</span>
              <span>{{ maskIdCard(selected.idCard) }}</span>
            </div>
            <div class="kv" v-if="selected.birthDate">
              <span class="kv-label">出生日期</span>
              <span>{{ selected.birthDate }}</span>
            </div>
            <div class="kv" v-if="selected.gender">
              <span class="kv-label">性别</span>
              <span>{{ selected.gender === 1 ? '男' : '女' }}</span>
            </div>
            <div class="kv" v-if="selected.remark">
              <span class="kv-label">备注</span>
              <span>{{ selected.remark }}</span>
            </div>
          </div>
        </div>

        <!-- 权限设置 -->
        <div class="detail-section">
          <div class="section-title">代管权限</div>
          <div class="scope-list">
            <div v-for="sp in allScopes" :key="sp.key" class="scope-item">
              <label class="scope-toggle">
                <input
                  type="checkbox"
                  :checked="hasScope(sp.key)"
                  @change="toggleScope(sp.key)"
                />
                <span class="scope-slider"></span>
              </label>
              <div class="scope-info">
                <span class="scope-label">{{ sp.label }}</span>
                <span class="scope-desc">{{ sp.desc }}</span>
              </div>
            </div>
          </div>
          <p class="scope-tip">💡 权限越大建议关系越亲密（如子女可开启全部）</p>
        </div>

        <!-- 快捷操作 -->
        <div class="detail-section">
          <div class="section-title">快捷操作</div>
          <div class="action-btns">
            <button class="action-btn primary" @click="goAppoint">
              <CalendarDays :size="15" /> 代为预约挂号
            </button>
            <button
              class="action-btn"
              :disabled="!hasScope('health_record') && !hasScope('vital')"
              @click="openProxyView"
            >
              <HeartPulse :size="15" /> 查看健康摘要
            </button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="family-detail empty-detail" v-else>
        <Users :size="40" class="empty-icon" />
        <p>点击左侧成员查看详情</p>
      </div>
    </div>

    <!-- ══ 添加/编辑弹窗 ══ -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-box">
        <h3>{{ editId ? '编辑成员' : '添加成员' }}</h3>

        <div class="form-grid">
          <div class="form-item full">
            <label>姓名 <em>*</em></label>
            <input v-model="form.memberName" placeholder="成员真实姓名" />
          </div>
          <div class="form-item">
            <label>与我的关系 <em>*</em></label>
            <select v-model="form.relation" @change="onRelationChange">
              <option value="spouse">配偶</option>
              <option value="child">子女</option>
              <option value="parent">父母</option>
              <option value="grandparent">祖父母/外祖父母</option>
              <option value="sibling">兄弟姐妹</option>
              <option value="other">其他</option>
            </select>
          </div>
          <div class="form-item">
            <label>性别</label>
            <div class="radio-group">
              <label><input type="radio" v-model="form.gender" :value="1" /> 男</label>
              <label><input type="radio" v-model="form.gender" :value="2" /> 女</label>
            </div>
          </div>
          <div class="form-item">
            <label>出生日期</label>
            <input type="date" v-model="form.birthDate" />
          </div>
          <div class="form-item">
            <label>手机号</label>
            <input v-model="form.memberPhone" placeholder="用于关联账号" maxlength="11" />
          </div>
          <div class="form-item">
            <label>身份证号</label>
            <input v-model="form.idCard" placeholder="可选" maxlength="18" />
          </div>
          <div class="form-item full">
            <label>备注</label>
            <textarea v-model="form.remark" rows="2" placeholder="如：高血压长期随访患者"></textarea>
          </div>

          <!-- 权限预设提示 -->
          <div class="form-item full">
            <label>代管权限（根据关系自动预填，可在详情中调整）</label>
            <div class="scope-preview">
              <span v-for="sp in previewScopes" :key="sp" class="scope-chip">{{ scopeLabel(sp) }}</span>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="showForm = false">取消</button>
          <button class="btn-primary" @click="submitForm" :disabled="!form.memberName || !form.relation">
            {{ editId ? '保存修改' : '添加成员' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ══ 代管健康摘要弹窗 ══ -->
    <div v-if="proxyData" class="modal-overlay" @click.self="proxyData = null">
      <div class="modal-box proxy-modal">
        <div class="proxy-header">
          <h3>{{ proxyData.memberName }} 的健康摘要</h3>
          <span class="proxy-note">数据来源：被管理人本人档案（只读）</span>
        </div>

        <!-- 健康档案 -->
        <div v-if="proxyData.healthRecord" class="proxy-section">
          <div class="proxy-section-title">慢病与过敏</div>
          <div class="chronic-tags" v-if="proxyData.healthRecord.chronicTags">
            <span
              v-for="c in proxyData.healthRecord.chronicTags.split(',')" :key="c"
              class="chronic-chip"
            >{{ chronicLabel(c) }}</span>
          </div>
          <div class="kv-row" v-if="proxyData.healthRecord.allergyHistory">
            <span class="kv-label">过敏史</span>
            <span>{{ proxyData.healthRecord.allergyHistory }}</span>
          </div>
        </div>

        <!-- 体征 -->
        <div v-if="proxyData.recentVitals?.length" class="proxy-section">
          <div class="proxy-section-title">最近体征（最新3条）</div>
          <div v-for="v in proxyData.recentVitals.slice(0,3)" :key="v.id" class="vital-row">
            <span class="vital-date">{{ String(v.measureTime || v.createdAt || '').slice(0,10) }}</span>
            <span v-if="v.systolicBp">血压 {{ v.systolicBp }}/{{ v.diastolicBp }} mmHg</span>
            <span v-if="v.fastingGlucose">空腹血糖 {{ v.fastingGlucose }} mmol/L</span>
          </div>
        </div>

        <!-- 就诊记录 -->
        <div v-if="proxyData.recentVisits?.length" class="proxy-section">
          <div class="proxy-section-title">最近就诊</div>
          <div v-for="r in proxyData.recentVisits.slice(0,3)" :key="r.id" class="visit-row">
            <span class="vital-date">{{ String(r.createdAt || '').slice(0,10) }}</span>
            <span>{{ r.diagnosis || '暂无诊断' }}</span>
          </div>
        </div>

        <div v-if="!proxyData.healthRecord && !proxyData.recentVitals?.length" class="empty-tip">
          暂无健康数据（需被管理人有平台账号且已填写档案）
        </div>

        <div class="modal-footer">
          <button class="btn-primary" @click="proxyData = null">关闭</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import {
  Users, Plus, ChevronRight, Pencil, Trash2,
  CalendarDays, HeartPulse
} from 'lucide-vue-next'

const router = useRouter()
const members = ref([])
const selected = ref(null)
const showForm = ref(false)
const editId = ref(null)
const proxyData = ref(null)

const form = reactive({
  memberName: '', relation: 'spouse', gender: null,
  birthDate: '', memberPhone: '', idCard: '',
  remark: '', permissionScope: 'basic,appointment,health_record,vital,visit_record'
})

// ── 权限定义 ──
const allScopes = [
  { key: 'appointment',    label: '代为预约挂号',  desc: '可以代该成员预约医生' },
  { key: 'health_record',  label: '查看健康档案',  desc: '慢病标签、过敏史、家族史' },
  { key: 'vital',          label: '查看体征指标',  desc: '血压、血糖等体征数据' },
  { key: 'visit_record',   label: '查看就诊记录',  desc: '历史门诊及诊断信息' },
  { key: 'follow_up',      label: '代答随访问卷',  desc: '代为回答医生随访问题' },
]

const defaultScopeMap = {
  spouse:      'basic,appointment,health_record,vital,visit_record',
  child:       'basic,appointment,health_record,vital,visit_record,follow_up',
  parent:      'basic,appointment,health_record,vital',
  grandparent: 'basic,appointment,health_record,vital',
  sibling:     'basic,appointment',
  other:       'basic,appointment',
}

// ── 工具函数 ──
function relationLabel(r) {
  return { spouse:'配偶', child:'子女', parent:'父母', grandparent:'祖父母', sibling:'兄弟姐妹', other:'其他' }[r] || r || ''
}
function relationColor(r) {
  return { spouse:'#e8f4f8', child:'#e8f8e8', parent:'#f8f4e8', grandparent:'#f4e8f8', sibling:'#e8f0f8', other:'#f0f0f0' }[r] || '#f0f0f0'
}
function maskIdCard(card) {
  if (!card || card.length < 8) return card
  return card.substring(0, 4) + '****' + card.substring(card.length - 4)
}
function chronicLabel(c) {
  return { hypertension:'高血压', diabetes:'糖尿病', chd:'冠心病', copd:'慢阻肺', stroke:'脑卒中' }[c?.trim()] || c
}
function scopeLabel(s) {
  return { basic:'基础信息', appointment:'代为预约', health_record:'健康档案', vital:'体征指标', visit_record:'就诊记录', follow_up:'随访代答' }[s] || s
}
function hasScope(key) {
  return (selected.value?.permissionScope || '').split(',').map(s=>s.trim()).includes(key)
}

// 权限预览（表单中显示）
const previewScopes = computed(() => {
  return (form.permissionScope || '').split(',').filter(s => s && s !== 'basic')
})

// ── 数据加载 ──
async function loadMembers() {
  try {
    const { data } = await request.get('/resident/family')
    members.value = data || []
    if (members.value.length && !selected.value) selected.value = members.value[0]
  } catch { members.value = [] }
}

// ── 权限切换（Bug13修复：乐观更新+失败回滚）──
async function toggleScope(key) {
  if (!selected.value) return
  const oldScope = selected.value.permissionScope  // 备份旧值

  const scopes = new Set((selected.value.permissionScope || 'basic').split(',').map(s => s.trim()))
  if (scopes.has(key)) scopes.delete(key)
  else scopes.add(key)
  scopes.add('basic') // basic 永远保留

  const newScope = [...scopes].join(',')
  selected.value.permissionScope = newScope  // 乐观更新UI

  try {
    await request.put(`/resident/family/${selected.value.id}`, { ...selected.value })
  } catch (e) {
    // Bug13修复：请求失败时回滚UI
    selected.value.permissionScope = oldScope
    alert('权限更新失败，请重试')
    console.warn('权限更新失败', e)
  }
}

// ── 关系变更：自动赋权 ──
function onRelationChange() {
  form.permissionScope = defaultScopeMap[form.relation] || 'basic,appointment'
}

// ── 表单操作 ──
function openAddForm() {
  editId.value = null
  Object.assign(form, { memberName:'', relation:'spouse', gender:null, birthDate:'', memberPhone:'', idCard:'', remark:'' })
  onRelationChange()
  showForm.value = true
}
function openEditForm(m) {
  editId.value = m.id
  Object.assign(form, {
    memberName: m.memberName, relation: m.relation,
    gender: m.gender, birthDate: m.birthDate || '',
    memberPhone: m.memberPhone || '', idCard: m.idCard || '',
    remark: m.remark || '', permissionScope: m.permissionScope || 'basic,appointment'
  })
  showForm.value = true
}

async function submitForm() {
  if (!form.memberName || !form.relation) return
  try {
    const payload = { ...form }
    if (editId.value) {
      await request.put(`/resident/family/${editId.value}`, payload)
    } else {
      await request.post('/resident/family', payload)
    }
    showForm.value = false
    await loadMembers()
    // 选中刚操作的那个
    if (!editId.value) selected.value = members.value[members.value.length - 1]
  } catch { alert('操作失败，请重试') }
}

async function doDelete(id) {
  if (!confirm('确定删除该成员吗？')) return
  try {
    await request.delete(`/resident/family/${id}`)
    selected.value = null
    await loadMembers()
  } catch { alert('删除失败') }
}

// ── 代为预约 ──
function goAppoint() {
  if (!selected.value) return
  router.push(`/resident/appointment?proxyId=${selected.value.id}&proxyName=${encodeURIComponent(selected.value.memberName)}`)
}

// ── 代管健康摘要 ──
async function openProxyView() {
  if (!selected.value) return
  try {
    const { data } = await request.get(`/resident/family/${selected.value.id}/proxy-view`)
    proxyData.value = data
  } catch (e) {
    if (e.response?.data?.code === 400) {
      alert('该成员尚未关联平台账号，无法查看健康数据')
    } else {
      proxyData.value = { memberName: selected.value.memberName }
    }
  }
}

onMounted(() => loadMembers())
</script>

<style scoped>
.family-page { padding: 16px 20px; }

/* ── 布局 ── */
.family-layout {
  display: flex;
  gap: 20px;
  min-height: 500px;
}

/* ── 左侧列表 ── */
.family-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border);
  background: var(--surface-soft);
}
.sidebar-title { font-size: 14px; font-weight: 700; color: var(--text); }
.btn-add {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  font-family: var(--font-sans);
  transition: .2s;
}
.btn-add:hover { filter: brightness(1.1); }

.sidebar-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--muted);
  text-align: center;
  flex: 1;
}
.empty-icon { color: var(--border); margin-bottom: 8px; }
.sidebar-empty p { font-size: 13px; margin: 2px 0; }
.empty-sub { font-size: 12px; }

.member-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  cursor: pointer;
  transition: background .15s;
  border-bottom: 1px solid var(--border);
}
.member-card:hover { background: var(--surface-soft); }
.member-card.active { background: rgba(47,107,87,0.07); }
.mc-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 15px;
  color: #555;
  flex-shrink: 0;
}
.mc-info { flex: 1; min-width: 0; }
.mc-info strong { font-size: 13px; display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.mc-arrow { color: var(--muted); flex-shrink: 0; }

/* ── 右侧详情 ── */
.family-detail {
  flex: 1;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.empty-detail {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--muted);
  font-size: 14px;
  gap: 10px;
}

/* 详情头部 */
.detail-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border);
  background: var(--surface-soft);
}
.detail-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: #555;
  flex-shrink: 0;
}
.detail-name-wrap { flex: 1; }
.detail-name-wrap h3 { margin: 0 0 4px; font-size: 18px; color: var(--text); }
.relation-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 6px;
  background: rgba(47,107,87,0.1);
  color: var(--primary);
  font-weight: 600;
  margin-right: 6px;
}
.relation-tag-lg {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 8px;
  background: rgba(47,107,87,0.1);
  color: var(--primary);
  font-weight: 600;
  margin-right: 8px;
}
.unlinked-tip {
  font-size: 11px;
  color: var(--muted);
  padding: 2px 8px;
  background: rgba(93,109,126,0.1);
  border-radius: 6px;
}
.detail-header-actions { display: flex; gap: 6px; }
.btn-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: .2s;
}
.btn-icon:hover { background: var(--surface-soft); }
.btn-icon.danger:hover { color: var(--danger); border-color: var(--danger); }

/* 详情section */
.detail-section {
  padding: 18px 24px;
  border-bottom: 1px solid var(--border);
}
.detail-section:last-child { border-bottom: none; }
.section-title {
  font-size: 12px;
  font-weight: 700;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: .5px;
  margin-bottom: 12px;
}

/* KV网格 */
.kv-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 8px 16px;
}
.kv { display: flex; gap: 8px; font-size: 13px; align-items: baseline; }
.kv-label { color: var(--muted); min-width: 56px; flex-shrink: 0; }

/* 权限开关 */
.scope-list { display: flex; flex-direction: column; gap: 10px; }
.scope-item { display: flex; align-items: center; gap: 12px; }
.scope-toggle { position: relative; flex-shrink: 0; }
.scope-toggle input { opacity: 0; width: 0; height: 0; position: absolute; }
.scope-slider {
  display: block;
  width: 40px;
  height: 22px;
  background: var(--border);
  border-radius: 11px;
  cursor: pointer;
  transition: background .2s;
  position: relative;
}
.scope-slider::after {
  content: '';
  position: absolute;
  left: 3px; top: 3px;
  width: 16px; height: 16px;
  border-radius: 50%;
  background: #fff;
  transition: transform .2s;
}
.scope-toggle input:checked + .scope-slider { background: var(--primary); }
.scope-toggle input:checked + .scope-slider::after { transform: translateX(18px); }
.scope-info { flex: 1; }
.scope-label { font-size: 13px; color: var(--text); font-weight: 500; margin-right: 8px; }
.scope-desc { font-size: 12px; color: var(--muted); }
.scope-tip { font-size: 12px; color: var(--muted); margin-top: 10px; }

/* 快捷操作 */
.action-btns { display: flex; gap: 10px; flex-wrap: wrap; }
.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--surface);
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
  font-family: var(--font-sans);
  transition: .2s;
}
.action-btn:hover { border-color: var(--primary); color: var(--primary); }
.action-btn.primary { background: var(--primary); color: #fff; border-color: var(--primary); }
.action-btn.primary:hover { filter: brightness(1.1); }
.action-btn:disabled { opacity: .4; cursor: not-allowed; }

/* ══ 弹窗 ══ */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  z-index: 1000; display: flex; align-items: center; justify-content: center;
}
.modal-box {
  background: var(--surface); border-radius: 18px; padding: 24px;
  width: 500px; max-height: 85vh; overflow-y: auto;
  box-shadow: 0 12px 40px rgba(0,0,0,0.15);
}
.modal-box h3 { margin: 0 0 18px; font-size: 18px; }

/* 表单 */
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px 16px; }
.form-item { display: flex; flex-direction: column; gap: 5px; }
.form-item.full { grid-column: 1 / -1; }
.form-item label { font-size: 12px; font-weight: 600; color: var(--muted); }
.form-item label em { color: var(--danger); font-style: normal; }
.form-item input, .form-item select, .form-item textarea {
  padding: 8px 12px; border: 1px solid var(--border); border-radius: 10px;
  font-size: 13px; background: var(--surface); color: var(--text);
  font-family: var(--font-sans); outline: none; transition: .2s;
}
.form-item input:focus, .form-item select:focus, .form-item textarea:focus {
  border-color: var(--primary); box-shadow: 0 0 0 3px rgba(47,107,87,.1);
}
.form-item textarea { resize: none; }
.radio-group { display: flex; gap: 16px; align-items: center; padding: 8px 0; }
.radio-group label { display: flex; align-items: center; gap: 4px; font-size: 13px; cursor: pointer; color: var(--text); }

/* 权限预览 */
.scope-preview { display: flex; flex-wrap: wrap; gap: 6px; padding: 4px 0; }
.scope-chip {
  padding: 3px 10px; border-radius: 8px;
  background: rgba(47,107,87,0.1); color: var(--primary);
  font-size: 12px; font-weight: 600;
}

.modal-footer { display: flex; justify-content: flex-end; gap: 8px; margin-top: 20px; }
.modal-footer button {
  padding: 9px 18px; border-radius: 10px; font-size: 13px;
  border: 1px solid var(--border); background: var(--surface);
  cursor: pointer; font-family: var(--font-sans);
}
.btn-primary { background: var(--primary) !important; color: #fff !important; border-color: var(--primary) !important; }
.btn-primary:hover { filter: brightness(1.1); }
.btn-primary:disabled { opacity: .5; cursor: not-allowed; }

/* 代管健康摘要弹窗 */
.proxy-modal { width: 520px; }
.proxy-header { margin-bottom: 16px; }
.proxy-header h3 { margin: 0 0 4px; }
.proxy-note { font-size: 12px; color: var(--muted); }
.proxy-section { margin-bottom: 16px; padding-bottom: 16px; border-bottom: 1px solid var(--border); }
.proxy-section:last-of-type { border-bottom: none; }
.proxy-section-title { font-size: 12px; font-weight: 700; color: var(--muted); margin-bottom: 10px; text-transform: uppercase; }
.chronic-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 8px; }
.chronic-chip {
  padding: 3px 10px; border-radius: 8px;
  background: rgba(231,76,60,0.1); color: #e74c3c;
  font-size: 12px; font-weight: 600;
}
.kv-row { display: flex; gap: 8px; font-size: 13px; margin-top: 6px; }
.vital-row, .visit-row { display: flex; gap: 12px; font-size: 13px; padding: 5px 0; align-items: baseline; }
.vital-date { font-size: 12px; color: var(--muted); min-width: 80px; }
.vital-row span, .visit-row span { background: var(--surface-soft); padding: 2px 8px; border-radius: 6px; }
.vital-date { background: none !important; padding: 0 !important; }

.empty-tip { text-align: center; color: var(--muted); font-size: 13px; padding: 24px 0; }

/* ── 响应式（移动端） ── */
@media (max-width: 768px) {
  .family-layout { flex-direction: column; gap: 12px; }
  .family-sidebar { width: 100%; border-radius: 14px; }
  .family-detail { border-radius: 14px; }
  .form-grid { grid-template-columns: 1fr; }
  .modal-box { width: 95vw; padding: 20px 16px; }
  .detail-header { padding: 14px 16px; }
  .detail-section { padding: 14px 16px; }
}
</style>
