<template>
  <div class="contract-manage">
    <!-- 顶部 -->
    <div class="page-header">
      <h2>签约管理</h2>
      <el-button type="primary" @click="showForm = true">手动创建签约</el-button>
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" @tab-change="load">
      <el-tab-pane label="待审核" name="PENDING" />
      <el-tab-pane label="全部签约" name="ALL" />
      <el-tab-pane label="医生统计" name="STATS" />
    </el-tabs>

    <!-- 待审核 / 全部 列表 -->
    <template v-if="activeTab !== 'STATS'">
      <el-form :inline="true" v-if="activeTab === 'ALL'" @submit.prevent="load">
        <el-form-item label="状态">
          <el-select v-model="statusFilter" clearable placeholder="全部状态" @change="load">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已签约" value="ACTIVE" />
            <el-option label="已驳回" value="REJECTED" />
            <el-option label="已到期" value="EXPIRED" />
            <el-option label="已解约" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="records" stripe v-loading="loading">
        <el-table-column prop="residentId" label="居民ID" width="80" />
        <el-table-column prop="doctorName" label="申请医生" width="100" />
        <el-table-column prop="teamName" label="团队" />
        <el-table-column prop="applyReason" label="签约原因" />
        <el-table-column prop="applyTime" label="申请时间" width="160">
          <template #default="{ row }">{{ row.applyTime?.replace('T', ' ').slice(0, 16) || '--' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签约期" width="200" v-if="activeTab === 'ALL'">
          <template #default="{ row }">
            <span v-if="row.startDate">{{ row.startDate }} ~ {{ row.endDate }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" link type="success" @click="handleApprove(row.id)">批准</el-button>
            <el-button v-if="row.status === 'PENDING'" link type="danger" @click="openReject(row)">驳回</el-button>
            <el-button v-if="row.status === 'ACTIVE'" link type="warning" @click="handleCancel(row.id)">解约</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="total > 0" v-model:current-page="page" :page-size="10" :total="total"
        layout="total, prev, pager, next" style="margin-top:16px;justify-content:flex-end" @current-change="load" />
    </template>

    <!-- 医生统计 -->
    <template v-if="activeTab === 'STATS'">
      <el-table :data="stats" stripe>
        <el-table-column prop="doctorName" label="医生" width="120" />
        <el-table-column prop="contractCount" label="签约人数" width="100" />
        <el-table-column label="签约负载">
          <template #default="{ row }">
            <el-progress :percentage="Math.min(row.contractCount * 5, 100)"
              :color="row.contractCount > 15 ? '#e74c3c' : row.contractCount > 10 ? '#e67e22' : '#27ae60'" />
          </template>
        </el-table-column>
      </el-table>
    </template>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="showReject" title="驳回签约申请" width="400px">
      <el-form>
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请填写驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReject = false">取消</el-button>
        <el-button type="danger" @click="submitReject" :disabled="!rejectReason.trim()">确认驳回</el-button>
      </template>
    </el-dialog>

    <!-- 新建签约弹窗 -->
    <el-dialog v-model="showForm" title="手动创建签约" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="居民ID"><el-input-number v-model="form.residentId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="医生ID"><el-input-number v-model="form.doctorId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="医生姓名"><el-input v-model="form.doctorName" /></el-form-item>
        <el-form-item label="团队名称"><el-input v-model="form.teamName" /></el-form-item>
        <el-form-item label="服务包">
          <el-select v-model="form.servicePackage" style="width:100%">
            <el-option label="基础包" value="basic" />
            <el-option label="慢病包" value="chronic" />
            <el-option label="老年包" value="elder" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForm = false">取消</el-button>
        <el-button type="primary" @click="submitContract">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('PENDING')
const records = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
const statusFilter = ref('')
const stats = ref([])

// 驳回
const showReject = ref(false)
const rejectReason = ref('')
const rejectId = ref(null)

// 新建
const showForm = ref(false)
const form = ref({ residentId: null, doctorId: null, doctorName: '', teamName: '', servicePackage: 'basic' })

function statusLabel(s) {
  return { PENDING: '待审核', ACTIVE: '已签约', REJECTED: '已驳回', EXPIRED: '已到期', CANCELLED: '已解约' }[s] || s
}
function statusTagType(s) {
  return { PENDING: 'warning', ACTIVE: 'success', REJECTED: 'danger', EXPIRED: 'info', CANCELLED: 'info' }[s] || ''
}

async function load() {
  if (activeTab.value === 'STATS') {
    try {
      const { data } = await request.get('/admin/contract/stats')
      stats.value = data || []
    } catch {}
    return
  }

  loading.value = true
  try {
    const params = { page: page.value, size: 10 }
    if (activeTab.value === 'PENDING') params.status = 'PENDING'
    else if (statusFilter.value) params.status = statusFilter.value
    const { data } = await request.get('/admin/contract', { params })
    records.value = data?.records || []
    total.value = data?.total || 0
  } finally { loading.value = false }
}

async function handleApprove(id) {
  await ElMessageBox.confirm('确定批准该签约申请？', '批准签约')
  await request.put(`/admin/contract/${id}/approve`)
  ElMessage.success('已批准')
  load()
}

function openReject(row) {
  rejectId.value = row.id
  rejectReason.value = ''
  showReject.value = true
}

async function submitReject() {
  await request.put(`/admin/contract/${rejectId.value}/reject`, { reason: rejectReason.value })
  ElMessage.success('已驳回')
  showReject.value = false
  load()
}

async function handleCancel(id) {
  const { value: reason } = await ElMessageBox.prompt('请填写解约说明', '管理员解约', { inputType: 'textarea' })
  await request.put(`/admin/contract/${id}/cancel`, { reason })
  ElMessage.success('已解约')
  load()
}

async function submitContract() {
  try {
    await request.post('/admin/contract', form.value)
    ElMessage.success('已创建')
    showForm.value = false
    load()
  } catch { ElMessage.error('创建失败') }
}

onMounted(() => load())
</script>

<style scoped>
.contract-manage { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.text-muted { color: var(--el-text-color-secondary); }
</style>
