<template>
  <div class="schedule-transfer-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>调班审批</span>
          <el-select v-model="statusFilter" clearable placeholder="审批状态" style="width:140px" @change="loadTransfers">
            <el-option label="待审批" :value="0" />
            <el-option label="已批准" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </div>
      </template>

      <el-table :data="list" stripe border v-loading="loading">
        <el-table-column prop="staffName" label="申请人" width="100" />
        <el-table-column prop="originalDate" label="原日期" width="120" />
        <el-table-column prop="targetDate" label="目标日期" width="120" />
        <el-table-column prop="reason" label="申请原因" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="{ 0:'warning', 1:'success', 2:'danger' }[row.status]" size="small">
              {{ { 0:'待审批', 1:'已批准', 2:'已拒绝' }[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="160">
          <template #default="{ row }">{{ row.createdAt?.replace('T',' ')?.substring(0,16) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" v-if="!statusFilter || statusFilter === 0">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button link type="success" @click="approve(row.id)">批准</el-button>
              <el-button link type="danger" @click="reject(row.id)">拒绝</el-button>
            </template>
            <span v-else style="color:var(--muted); font-size:12px">已处理</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="list.length === 0" description="暂无调班申请" :image-size="48" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const list = ref([])
const loading = ref(false)
const statusFilter = ref(0)

async function loadTransfers() {
  loading.value = true
  try {
    const params = {}
    if (statusFilter.value !== '' && statusFilter.value !== null) params.status = statusFilter.value
    const { data } = await request.get('/admin/schedule/transfer', { params })
    list.value = data?.records || data || []
  } finally { loading.value = false }
}

async function approve(id) {
  await ElMessageBox.confirm('确认批准该调班申请？', '批准调班')
  await request.put(`/admin/schedule/transfer/${id}`, { status: 1, comment: '' })
  ElMessage.success('已批准')
  loadTransfers()
}

async function reject(id) {
  const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝调班', { inputPlaceholder: '原因（可选）' }).catch(() => ({ value: '' }))
  await request.put(`/admin/schedule/transfer/${id}`, { status: 2, comment: value || '' })
  ElMessage.warning('已拒绝')
  loadTransfers()
}

onMounted(loadTransfers)
</script>

<style scoped>
.schedule-transfer-page { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

/* 响应式 */
@media (max-width: 768px) {
  .schedule-transfer-page { padding: 12px; }
  .card-header { flex-direction: column; align-items: flex-start; gap: 8px; }
}
</style>
