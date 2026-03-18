<template>
  <div class="notice-manage">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-select v-model="filterCategory" placeholder="全部类型" clearable size="small" style="width:120px" @change="loadList">
        <el-option label="通知公告" value="notice" />
        <el-option label="健康活动" value="activity" />
        <el-option label="停诊公告" value="suspend" />
        <el-option label="疫情预防" value="prevention" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="全部状态" clearable size="small" style="width:120px; margin-left:8px" @change="loadList">
        <el-option label="已发布" :value="1" />
        <el-option label="草稿" :value="0" />
      </el-select>
      <el-button type="primary" size="small" style="margin-left:auto" @click="openDrawer()">
        <Plus :size="14" style="margin-right:4px" /> 新建公告
      </el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" stripe class="notice-table">
      <el-col :xs="24" />
      <el-table-column label="标题" prop="title" min-width="200" show-overflow-tooltip />
      <el-table-column label="类别" width="100">
        <template #default="{ row }">
          <el-tag :type="catTag(row.category)" size="small">{{ catLabel(row.category) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="目标受众" width="110">
        <template #default="{ row }">
          <span>{{ audienceLabel(row.audience) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isPublished ? 'success' : 'info'" size="small">
            {{ row.isPublished ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" width="150">
        <template #default="{ row }">{{ fmtTime(row.publishedAt) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" width="150">
        <template #default="{ row }">{{ fmtTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link @click="openDrawer(row)">编辑</el-button>
          <el-button size="small" link :type="row.isPublished ? 'warning' : 'success'"
            @click="togglePublish(row)">
            {{ row.isPublished ? '下架' : '发布' }}
          </el-button>
          <el-popconfirm title="确认删除此公告？" @confirm="remove(row.id)">
            <template #reference>
              <el-button size="small" link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pager">
      <el-pagination
        v-model:current-page="page" v-model:page-size="size"
        :total="total" layout="total, prev, pager, next"
        small @change="loadList" />
    </div>

    <!-- 编辑抽屉 -->
    <el-drawer v-model="drawerOpen" :title="form.id ? '编辑公告' : '新建公告'"
      size="560px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="form.category" style="width:100%">
            <el-option label="通知公告" value="notice" />
            <el-option label="健康活动" value="activity" />
            <el-option label="停诊公告" value="suspend" />
            <el-option label="疫情预防" value="prevention" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标受众">
          <el-select v-model="form.audience" style="width:100%">
            <el-option label="全体（员工+居民）" value="all" />
            <el-option label="仅居民" value="resident" />
            <el-option label="仅员工" value="staff" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8"
            placeholder="请输入公告正文内容..." />
        </el-form-item>
        <el-form-item label="定时发布">
          <el-date-picker v-model="form.scheduledAt" type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="不填则手动发布" style="width:100%" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" placeholder="可选，显示在列表中的摘要" maxlength="120" />
        </el-form-item>
      </el-form>

      <!-- 发布提示 -->
      <el-alert v-if="!form.id" type="info" show-icon :closable="false" style="margin-bottom:16px">
        保存为草稿后，点击「发布」可将公告推送至目标受众的消息中心。
      </el-alert>

      <template #footer>
        <el-button @click="drawerOpen = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存草稿</el-button>
        <el-button type="success" :loading="saving" @click="saveAndPublish" v-if="!form.isPublished">
          保存并发布
        </el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from 'lucide-vue-next'
import request from '@/utils/request'

const list    = ref([])
const loading = ref(false)
const saving  = ref(false)
const page    = ref(1)
const size    = ref(15)
const total   = ref(0)
const filterCategory = ref('')
const filterStatus   = ref(null)
const drawerOpen = ref(false)
const formRef    = ref()

const emptyForm = () => ({
  id: null, title: '', category: 'notice', content: '',
  audience: 'all', summary: '', scheduledAt: null, isPublished: 0
})
const form = ref(emptyForm())

const rules = {
  title:    [{ required: true, message: '请输入标题' }],
  category: [{ required: true, message: '请选择类别' }],
  content:  [{ required: true, message: '请输入内容' }]
}

async function loadList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterCategory.value) params.category = filterCategory.value
    if (filterStatus.value !== null && filterStatus.value !== '') params.published = filterStatus.value
    const res = await request.get('/admin/article', { params })
    list.value  = res.data?.records || []
    total.value = res.data?.total   || 0
  } finally { loading.value = false }
}

function openDrawer(row = null) {
  form.value = row ? { ...row } : emptyForm()
  drawerOpen.value = true
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.value.id) {
      await request.put(`/admin/article/${form.value.id}`, form.value)
    } else {
      await request.post('/admin/article', form.value)
    }
    ElMessage.success('保存成功')
    drawerOpen.value = false
    loadList()
  } finally { saving.value = false }
}

async function saveAndPublish() {
  await formRef.value.validate()
  saving.value = true
  try {
    let id = form.value.id
    if (!id) {
      const res = await request.post('/admin/article', form.value)
      id = res.data?.id
    } else {
      await request.put(`/admin/article/${id}`, form.value)
    }
    await request.put(`/admin/article/${id}/publish`, null, { params: { publish: 1 } })
    ElMessage.success('发布成功，目标受众将收到消息通知')
    drawerOpen.value = false
    loadList()
  } finally { saving.value = false }
}

async function togglePublish(row) {
  const next = row.isPublished ? 0 : 1
  await request.put(`/admin/article/${row.id}/publish`, null, { params: { publish: next } })
  ElMessage.success(next ? '已发布' : '已下架')
  loadList()
}

async function remove(id) {
  await request.delete(`/admin/article/${id}`)
  ElMessage.success('已删除')
  loadList()
}

// ── 工具函数 ──
const catMap = { notice: ['', '通知公告'], activity: ['success', '健康活动'], suspend: ['danger', '停诊公告'], prevention: ['warning', '疫情预防'] }
const catTag   = (c) => catMap[c]?.[0] || ''
const catLabel = (c) => catMap[c]?.[1] || c

const audienceMap = { all: '全体', resident: '居民', staff: '员工' }
const audienceLabel = (a) => audienceMap[a] || a

function fmtTime(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('zh-CN', { year:'numeric', month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit' })
}

onMounted(loadList)
</script>

<style scoped>
.notice-manage { padding: 0 }
.toolbar { display:flex; align-items:center; margin-bottom:14px; flex-wrap:wrap; gap:6px; }
.notice-table { width:100%; }
.pager { display:flex; justify-content:flex-end; margin-top:14px; }
</style>
