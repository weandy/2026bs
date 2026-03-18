<template>
  <div class="audit-log-page">
    <!-- 筛选栏 -->
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索操作人/模块/IP" clearable
        style="width:200px" @keyup.enter="load" @clear="load">
        <template #prefix><Search :size="14" /></template>
      </el-input>
      <el-select v-model="filterType" placeholder="操作类型" clearable style="width:130px" @change="load">
        <el-option label="登录" value="LOGIN" />
        <el-option label="创建" value="CREATE" />
        <el-option label="修改" value="UPDATE" />
        <el-option label="删除" value="DELETE" />
        <el-option label="发布" value="PUBLISH" />
        <el-option label="导出" value="EXPORT" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
        start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD"
        style="width:240px" @change="load" />
      <el-button type="primary" @click="load" :loading="loading" style="margin-left:auto">查询</el-button>
    </div>

    <!-- 日志表格 -->
    <el-table :data="logs" v-loading="loading" stripe size="small">
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ fmtTime(row.createdAt || row.operateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作人" prop="operatorName" width="100" />
      <el-table-column label="角色" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="roleTag(row.operatorRole)">{{ row.operatorRole || '—' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="模块" width="100">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.moduleCode || '—' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作类型" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="typeTag(row.logType)">{{ row.logType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="描述/内容" show-overflow-tooltip min-width="200">
        <template #default="{ row }">{{ row.targetDesc || row.action }}</template>
      </el-table-column>
      <el-table-column label="IP 地址" width="130">
        <template #default="{ row }">
          <span class="mono">{{ row.requestIp || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结果" width="70" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.result === 0 ? 'danger' : 'success'">
            {{ row.result === 0 ? '失败' : '成功' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pager">
      <el-pagination v-model:current-page="page" v-model:page-size="size"
        :total="total" layout="total, prev, pager, next" small @change="load" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from 'lucide-vue-next'
import request from '@/utils/request'

const logs    = ref([])
const loading = ref(false)
const page    = ref(1)
const size    = ref(20)
const total   = ref(0)
const keyword    = ref('')
const filterType = ref('')
const dateRange  = ref([])

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (keyword.value)    params.keyword  = keyword.value
    if (filterType.value) params.type     = filterType.value
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate   = dateRange.value[1]
    }
    const res = await request.get('/api/admin/audit-log', { params })
    const data = res.data
    if (Array.isArray(data)) {
      logs.value  = data
      total.value = data.length
    } else {
      logs.value  = data?.records || data?.list || []
      total.value = data?.total   || logs.value.length
    }
  } finally { loading.value = false }
}

const roleTag = (r) => ({ admin:'warning', doctor:'success', nurse:'info' }[r] || '')
const typeTag = (t) => ({ DELETE:'danger', CREATE:'success', UPDATE:'warning', LOGIN:'info' }[t] || '')
function fmtTime(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('zh-CN', { month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit', second:'2-digit' })
}

onMounted(load)
</script>

<style scoped>
.audit-log-page { padding: 0 }
.toolbar { display:flex; align-items:center; flex-wrap:wrap; gap:8px; margin-bottom:14px }
.pager   { display:flex; justify-content:flex-end; margin-top:14px }
.mono { font-family: monospace; font-size: 12px }
</style>
