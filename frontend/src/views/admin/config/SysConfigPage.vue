<template>
  <div class="sys-config-page">
    <div class="page-header">
      <div>
        <h2>系统配置</h2>
        <p class="page-subtitle">管理平台运行参数与全局配置项</p>
      </div>
    </div>

    <!-- 配置表格 -->
    <el-table :data="configs" stripe v-loading="loading" class="custom-table"
      :empty-text="'暂无配置项'">
      <el-table-column prop="configKey" label="配置键" width="220">
        <template #default="{ row }"><span class="font-mono">{{ row.configKey }}</span></template>
      </el-table-column>
      <el-table-column prop="configName" label="配置名称" width="180" />
      <el-table-column prop="configGroup" label="分组" width="120">
        <template #default="{ row }">
          <span class="status-tag pending">{{ row.configGroup || '默认' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="configValue" label="配置值" min-width="200">
        <template #default="{ row }">
          <el-input v-if="editingId === row.id" v-model="row.configValue" size="small"
            @keyup.enter="saveConfig(row)" />
          <span v-else>{{ row.configValue }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center">
        <template #default="{ row }">
          <template v-if="editingId === row.id">
            <el-button link type="primary" @click="saveConfig(row)">保存</el-button>
            <span class="action-divider">|</span>
            <el-button link @click="cancelEdit(row)">取消</el-button>
          </template>
          <el-button v-else-if="row.isEditable" link type="primary" @click="startEdit(row)">编辑</el-button>
          <span v-else class="status-tag done" style="font-size:12px;">只读</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const configs = ref([])
const loading = ref(false)
const editingId = ref(null)
let originalValue = ''

async function loadConfigs() {
  loading.value = true
  try {
    const { data } = await request.get('/admin/config')
    configs.value = data || []
  } finally {
    loading.value = false
  }
}

function startEdit(row) {
  originalValue = row.configValue
  editingId.value = row.id
}

function cancelEdit(row) {
  row.configValue = originalValue
  editingId.value = null
}

async function saveConfig(row) {
  try {
    await request.put(`/admin/config/${row.id}`, { configValue: row.configValue })
    ElMessage.success('保存成功')
    editingId.value = null
  } catch {
    ElMessage.error('保存失败')
  }
}

onMounted(loadConfigs)
</script>

<style scoped>
.sys-config-page { padding: 24px; max-width: 1000px; margin: 0 auto; }
</style>
