<template>
  <div class="dept-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>科室管理</span>
          <el-button type="primary" size="small" @click="openCreate">新增科室</el-button>
        </div>
      </template>
      <el-table :data="depts" stripe v-loading="loading">
        <el-table-column prop="deptCode" label="科室编码" width="120" />
        <el-table-column prop="deptName" label="科室名称" width="150" />
        <el-table-column prop="isApptOpen" label="开放预约" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isApptOpen ? 'success' : 'info'" size="small">
              {{ row.isApptOpen ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除?" @confirm="deleteDept(row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="editingId ? '编辑科室' : '新增科室'" width="450px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="科室编码" prop="deptCode">
          <el-input v-model="form.deptCode" :disabled="!!editingId" placeholder="如 QKMZ" />
        </el-form-item>
        <el-form-item label="科室名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="如 全科门诊" />
        </el-form-item>
        <el-form-item label="开放预约">
          <el-switch v-model="form.isApptOpen" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveDept">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const depts = ref([])
const loading = ref(false)
const showDialog = ref(false)
const saving = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const form = ref({ deptCode: '', deptName: '', isApptOpen: true, sortOrder: 0, status: 1 })
const formRules = {
  deptCode: [{ required: true, message: '请输入科室编码', trigger: 'blur' }],
  deptName: [{ required: true, message: '请输入科室名称', trigger: 'blur' }]
}

async function loadDepts() {
  loading.value = true
  try {
    const { data } = await request.get('/admin/dept/list')
    depts.value = data || []
  } finally { loading.value = false }
}

function openCreate() {
  editingId.value = null
  form.value = { deptCode: '', deptName: '', isApptOpen: true, sortOrder: 0, status: 1 }
  showDialog.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.value = { ...row }
  showDialog.value = true
}

async function saveDept() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (editingId.value) {
      await request.put(`/admin/dept/${editingId.value}`, form.value)
    } else {
      await request.post('/admin/dept', form.value)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    loadDepts()
  } finally { saving.value = false }
}

async function deleteDept(id) {
  await request.delete(`/admin/dept/${id}`)
  ElMessage.success('删除成功')
  loadDepts()
}

onMounted(() => loadDepts())
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }

/* 响应式 */
@media (max-width: 768px) {
  .dept-manage { padding: 12px; }
  .card-header { flex-direction: column; align-items: flex-start; gap: 8px; }
}
</style>
