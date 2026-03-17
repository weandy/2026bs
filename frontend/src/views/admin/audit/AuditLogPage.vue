<template>
  <div class="audit-log-page">
    <div class="page-header">
      <div>
        <h2>审计日志</h2>
        <p class="page-subtitle">系统操作记录与安全审计追溯</p>
      </div>
    </div>

    <!-- 过滤搜索区 -->
    <div class="filter-section">
      <div class="compact-form">
        <el-select v-model="moduleFilter" clearable placeholder="全部模块" style="width:150px">
          <el-option label="用户管理" value="USER" />
          <el-option label="排班管理" value="SCHEDULE" />
          <el-option label="药品管理" value="DRUG" />
          <el-option label="系统配置" value="SYSTEM" />
          <el-option label="疫苗管理" value="VACCINE" />
          <el-option label="处方管理" value="PRESCRIPTION" />
        </el-select>
        <el-select v-model="resultFilter" clearable placeholder="全部结果" style="width:120px">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
        <el-button type="primary" @click="page = 1; loadLogs()">
          <Search :size="14" style="margin-right:4px" /> 查询
        </el-button>
      </div>
    </div>

    <!-- 日志表格 -->
    <el-table :data="logs" stripe v-loading="loading" class="custom-table"
      :empty-text="'暂无审计日志'">
      <el-table-column prop="opTime" label="操作时间" width="170">
        <template #default="{ row }"><span class="time-text">{{ formatTime(row.opTime) }}</span></template>
      </el-table-column>
      <el-table-column prop="operatorName" label="操作人" width="100" />
      <el-table-column prop="operatorRole" label="角色" width="80">
        <template #default="{ row }">
          <span class="status-tag" :class="roleTheme(row.operatorRole)">{{ row.operatorRole }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="moduleCode" label="模块" width="100">
        <template #default="{ row }"><span class="font-mono">{{ row.moduleCode }}</span></template>
      </el-table-column>
      <el-table-column prop="action" label="操作" width="120" />
      <el-table-column prop="targetDesc" label="目标" show-overflow-tooltip />
      <el-table-column prop="result" label="结果" width="80" align="center">
        <template #default="{ row }">
          <span class="status-tag" :class="row.result === 1 ? 'done' : 'cancelled'">
            {{ row.result === 1 ? '成功' : '失败' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="requestIp" label="IP 地址" width="130">
        <template #default="{ row }"><span class="font-mono">{{ row.requestIp }}</span></template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="page" :page-size="20" :total="total"
      layout="total, prev, pager, next" @current-change="loadLogs"
      style="margin-top:16px;justify-content:flex-end" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Search } from 'lucide-vue-next'

const logs = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const moduleFilter = ref('')
const resultFilter = ref('')

function formatTime(t) { return t ? t.replace('T', ' ').substring(0, 19) : '' }
function roleTheme(r) {
  return { admin: 'in-progress', doctor: 'pending', nurse: 'done' }[r?.toLowerCase()] || 'default'
}

async function loadLogs() {
  loading.value = true
  try {
    const params = { page: page.value, size: 20 }
    if (moduleFilter.value) params.module = moduleFilter.value
    if (resultFilter.value !== '') params.result = resultFilter.value
    const { data } = await request.get('/admin/audit-log', { params })
    logs.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(loadLogs)
</script>

<style scoped>
.audit-log-page { padding: 24px; max-width: 1200px; margin: 0 auto; }
</style>
