<template>
  <div class="record-manage-page">
    <div class="page-header">
      <div>
        <h2>健康档案管理</h2>
        <p class="page-subtitle">查询并维护居民健康档案信息</p>
      </div>
    </div>

    <!-- 搜索区 -->
    <div class="filter-section">
      <div class="compact-form">
        <el-input v-model="residentId" placeholder="输入居民ID" style="width:200px" clearable
          @keyup.enter="search">
          <template #prefix><Search :size="14" /></template>
        </el-input>
        <el-button type="primary" @click="search" :loading="loading">
          <Search :size="14" style="margin-right:4px" /> 查询档案
        </el-button>
      </div>
    </div>

    <!-- 查询结果 -->
    <div v-if="record" class="record-detail-panel">
      <div class="panel-header">
        <h3>居民档案 #{{ record.residentId }}</h3>
        <el-button v-if="!editing" type="primary" plain size="small" @click="startEdit">
          <Edit :size="14" style="margin-right:4px" /> 编辑档案
        </el-button>
        <div v-else style="display:flex;gap:8px;">
          <el-button type="primary" size="small" @click="saveEdit" :loading="saving">保存</el-button>
          <el-button size="small" @click="cancelEdit">取消</el-button>
        </div>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="过敏史">
          <el-input v-if="editing" v-model="editForm.allergyHistory" type="textarea" :rows="2" />
          <span v-else>{{ record.allergyHistory || '无' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="既往病史">
          <el-input v-if="editing" v-model="editForm.pastMedicalHistory" type="textarea" :rows="2" />
          <span v-else>{{ record.pastMedicalHistory || '无' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="家族史">
          <el-input v-if="editing" v-model="editForm.familyHistory" type="textarea" :rows="2" />
          <span v-else>{{ record.familyHistory || '无' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="慢病标签">
          <el-input v-if="editing" v-model="editForm.chronicTags" placeholder="如: 高血压,糖尿病" />
          <span v-else>
            <span v-if="record.chronicTags" v-for="tag in record.chronicTags.split(',')" :key="tag"
              class="status-tag pending" style="margin-right:4px;">{{ tag.trim() }}</span>
            <span v-else class="muted-text">无</span>
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="紧急联系人">
          <template v-if="editing">
            <div style="display:flex;gap:8px;">
              <el-input v-model="editForm.emergencyContact" placeholder="姓名" style="flex:1" />
              <el-input v-model="editForm.emergencyPhone" placeholder="电话" style="flex:1" />
            </div>
          </template>
          <span v-else>{{ record.emergencyContact || '--' }} {{ record.emergencyPhone || '' }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <el-empty v-else-if="searched" description="未找到该居民的健康档案" :image-size="80" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Search, Edit } from 'lucide-vue-next'

const residentId = ref('')
const record = ref(null)
const searched = ref(false)
const loading = ref(false)
const editing = ref(false)
const saving = ref(false)
const editForm = ref({})

async function search() {
  if (!residentId.value) return
  searched.value = true
  loading.value = true
  try {
    const { data } = await request.get(`/medical/health-record/${residentId.value}`)
    record.value = data
  } catch {
    record.value = null
  } finally {
    loading.value = false
  }
}

function startEdit() {
  editForm.value = {
    allergyHistory: record.value.allergyHistory || '',
    pastMedicalHistory: record.value.pastMedicalHistory || '',
    familyHistory: record.value.familyHistory || '',
    chronicTags: record.value.chronicTags || '',
    emergencyContact: record.value.emergencyContact || '',
    emergencyPhone: record.value.emergencyPhone || ''
  }
  editing.value = true
}

function cancelEdit() { editing.value = false }

async function saveEdit() {
  saving.value = true
  try {
    await request.put(`/medical/health-record/${residentId.value}`, editForm.value)
    ElMessage.success('档案更新成功')
    editing.value = false
    await search()
  } catch {
    ElMessage.error('更新失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.record-manage-page { padding: 24px; max-width: 960px; margin: 0 auto; }
.record-detail-panel {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 20px;
}
.panel-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--border);
}
.panel-header h3 { font-size: 16px; font-weight: 600; color: var(--text-strong); margin: 0; }
</style>
