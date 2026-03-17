<template>
  <div class="staff-mgmt" style="padding: 20px;">
    <!-- 搜索栏 -->
    <div class="toolbar">
      <div class="search-group">
        <el-select v-model="searchType" style="width: 120px;" size="default">
          <el-option label="姓名" value="name" />
          <el-option label="工号" value="username" />
          <el-option label="手机号" value="phone" />
        </el-select>
        <el-input v-model="keyword" :placeholder="`搜索${searchTypeLabel}...`" style="width: 260px;" @keyup.enter="loadStaff" clearable>
          <template #prefix><Search :size="14" /></template>
        </el-input>
        <el-button type="primary" @click="loadStaff">查询</el-button>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增员工</el-button>
    </div>

    <el-table :data="staffList" border stripe v-loading="loading">
      <el-table-column prop="username" label="工号" width="120" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="deptName" label="科室" min-width="120" />
      <el-table-column prop="jobTitle" label="职称" min-width="100" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="roleId" label="角色" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="{ 4:'danger', 2:'', 3:'success' }[row.roleId]" size="small">
            {{ { 4:'管理员', 2:'医生', 3:'护士' }[row.roleId] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <div class="op-btns">
            <el-button type="primary" text size="small" @click="editStaff(row)">编辑</el-button>
            <el-button type="warning" text size="small" @click="resetPwd(row.id)">重置密码</el-button>
            <el-button :type="row.status === 1 ? 'danger' : 'success'" text size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :page-size="10"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 16px;"
      @current-change="loadStaff"
    />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑员工' : '新增员工'" width="500px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="工号" prop="username"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="科室"><el-input v-model="form.deptName" /></el-form-item>
        <el-form-item label="职称"><el-input v-model="form.jobTitle" /></el-form-item>
        <el-form-item label="密码" v-if="!form.id" prop="password"><el-input v-model="form.password" type="password" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleId" placeholder="选择角色">
            <el-option label="管理员" :value="4" />
            <el-option label="医生" :value="2" />
            <el-option label="护士" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStaff">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from 'lucide-vue-next'
import request from '@/utils/request'

const staffList = ref([])
const page = ref(1)
const total = ref(0)
const keyword = ref('')
const searchType = ref('name')
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, username: '', name: '', phone: '', deptName: '', jobTitle: '', password: '', roleId: 2 })
const formRef = ref(null)
const formRules = computed(() => ({
  username: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  ...(!form.id ? { password: [{ required: true, message: '请设置密码', trigger: 'blur' }] } : {})
}))

const searchTypeLabel = computed(() => ({ name: '姓名', username: '工号', phone: '手机号' }[searchType.value]))

async function loadStaff() {
  loading.value = true
  try {
    const res = await request.get('/admin/staff', { params: { page: page.value, size: 10, [searchType.value]: keyword.value } })
    staffList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function openCreateDialog() {
  Object.assign(form, { id: null, username: '', name: '', phone: '', deptName: '', jobTitle: '', password: '', roleId: 2 })
  dialogVisible.value = true
}

function editStaff(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function saveStaff() {
  await formRef.value.validate()
  if (form.id) {
    await request.put(`/admin/staff/${form.id}`, form)
  } else {
    await request.post('/admin/staff', form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadStaff()
}

async function resetPwd(id) {
  const { value } = await ElMessageBox.prompt('请输入新密码', '重置密码', { inputType: 'password' })
  await request.put(`/admin/staff/${id}/reset-password`, { password: value })
  ElMessage.success('密码已重置')
}

async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确认${action}账号「${row.name}」？`, action)
  await request.put(`/admin/staff/${row.id}`, { ...row, status: newStatus })
  ElMessage.success(`已${action}`)
  loadStaff()
}

onMounted(() => loadStaff())
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
}
.search-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.op-btns {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-wrap: nowrap;
}

/* 响应式 */
@media (max-width: 768px) {
  .toolbar { flex-direction: column; align-items: stretch; }
  .search-group { flex-wrap: wrap; }
}
</style>
