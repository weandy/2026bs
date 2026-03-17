<template>
  <div class="drug-log-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>出入库日志 & 效期看板</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="出入库日志" name="logs" />
        <el-tab-pane label="效期看板" name="expiry" />
      </el-tabs>

      <!-- 出入库日志 -->
      <template v-if="activeTab === 'logs'">
        <el-form :inline="true" style="margin-bottom:12px">
          <el-form-item><el-input v-model="logKeyword" placeholder="药品名称" clearable /></el-form-item>
          <el-form-item>
            <el-select v-model="logType" clearable placeholder="操作类型">
              <el-option label="入库" :value="1" />
              <el-option label="出库（发药）" :value="2" />
              <el-option label="报损" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item><el-button type="primary" @click="loadLogs">查询</el-button></el-form-item>
        </el-form>
        <el-table :data="logs" stripe v-loading="loadingLogs" border>
          <el-table-column prop="drugName" label="药品" width="150" />
          <el-table-column prop="batchNo" label="批号" width="120" />
          <el-table-column prop="changeType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.changeType === 1 ? 'success' : row.changeType === 2 ? 'danger' : 'warning'" size="small">
                {{ { 1:'入库', 2:'出库', 3:'报损' }[row.changeType] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="changeQty" label="数量" width="80" />
          <el-table-column prop="afterQty" label="余量" width="80" />
          <el-table-column prop="operatorName" label="操作人" width="100" />
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="时间" width="170">
            <template #default="{ row }">{{ row.createdAt?.replace('T',' ')?.substring(0,16) }}</template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="logPage" :page-size="20" :total="logTotal"
          layout="total, prev, pager, next" @current-change="loadLogs" style="margin-top:12px;justify-content:flex-end" />
      </template>

      <!-- 效期看板 -->
      <template v-if="activeTab === 'expiry'">
        <el-row :gutter="12" style="margin-bottom:16px">
          <el-col :span="8">
            <el-statistic title="已过期" :value="expiryStats.expired" class="stat-expired" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="30天内过期" :value="expiryStats.soon" class="stat-soon" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="正常" :value="expiryStats.normal" class="stat-normal" />
          </el-col>
        </el-row>
        <el-table :data="expiryList" stripe border v-loading="loadingExpiry">
          <el-table-column prop="genericName" label="药品" width="150" />
          <el-table-column prop="batchNo" label="批号" width="120" />
          <el-table-column prop="quantity" label="库存" width="80" />
          <el-table-column prop="expireDate" label="过期日期" width="120">
            <template #default="{ row }">
              <span :class="getExpiryClass(row.expireDate)">{{ row.expireDate }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="supplier" label="供应商" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getExpiryTag(row.expireDate)" size="small">{{ getExpiryText(row.expireDate) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const activeTab = ref('logs')
const logs = ref([])
const loadingLogs = ref(false)
const logKeyword = ref('')
const logType = ref(null)
const logPage = ref(1)
const logTotal = ref(0)
const expiryList = ref([])
const loadingExpiry = ref(false)
const expiryStats = ref({ expired: 0, soon: 0, normal: 0 })

function onTabChange(tab) {
  if (tab === 'logs') loadLogs()
  else loadExpiry()
}

async function loadLogs() {
  loadingLogs.value = true
  try {
    const params = { page: logPage.value, size: 20 }
    if (logKeyword.value) params.keyword = logKeyword.value
    if (logType.value) params.changeType = logType.value
    const { data } = await request.get('/admin/drug/stock-logs', { params })
    logs.value = data?.records || data || []
    logTotal.value = data?.total || logs.value.length
  } finally { loadingLogs.value = false }
}

async function loadExpiry() {
  loadingExpiry.value = true
  try {
    const { data } = await request.get('/admin/drug/expiry-board')
    expiryList.value = data || []
    const now = new Date()
    let expired = 0, soon = 0, normal = 0
    expiryList.value.forEach(item => {
      const diff = (new Date(item.expireDate) - now) / 86400000
      if (diff <= 0) expired++
      else if (diff <= 30) soon++
      else normal++
    })
    expiryStats.value = { expired, soon, normal }
  } finally { loadingExpiry.value = false }
}

function getExpiryClass(d) {
  const diff = (new Date(d) - new Date()) / 86400000
  if (diff <= 0) return 'expiry-expired'
  if (diff <= 30) return 'expiry-soon'
  return 'expiry-normal'
}
function getExpiryTag(d) {
  const diff = (new Date(d) - new Date()) / 86400000
  if (diff <= 0) return 'danger'
  if (diff <= 30) return 'warning'
  return 'success'
}
function getExpiryText(d) {
  const diff = (new Date(d) - new Date()) / 86400000
  if (diff <= 0) return '已过期'
  if (diff <= 30) return '即将过期'
  return '正常'
}

onMounted(() => loadLogs())
</script>

<style scoped>
.drug-log-page { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

/* 效期颜色语义化 */
.expiry-expired { color: var(--danger); font-weight: bold; }
.expiry-soon { color: var(--warn); font-weight: bold; }
.expiry-normal { color: var(--good); font-weight: bold; }

/* el-statistic 语义色 */
.stat-expired :deep(.el-statistic__number) { color: var(--danger); }
.stat-soon :deep(.el-statistic__number) { color: var(--warn); }
.stat-normal :deep(.el-statistic__number) { color: var(--good); }

/* 响应式 */
@media (max-width: 768px) {
  .drug-log-page { padding: 12px; }
  .card-header { flex-direction: column; align-items: flex-start; gap: 8px; }
}
</style>
