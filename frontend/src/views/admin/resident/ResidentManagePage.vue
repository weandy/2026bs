<template>
  <div class="resident-manage"><div class="page-header"><h2>居民管理</h2></div>
    <el-form :inline="true" class="filter-bar">
      <el-form-item><el-input v-model="keyword" placeholder="姓名/手机号/身份证" clearable @keyup.enter="load" /></el-form-item>
      <el-form-item><el-button type="primary" @click="load">搜索</el-button></el-form-item>
    </el-form>
    <el-table :data="records" stripe border v-loading="loading">
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="gender" label="性别" width="60"><template #default="{row}">{{ row.gender === 1 ? '男' : '女' }}</template></el-table-column>
      <el-table-column prop="birthDate" label="出生日期" width="120" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="bloodType" label="血型" width="60" />
      <el-table-column prop="address" label="地址" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{row}"><el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{row}">
          <el-button size="small" :type="row.status === 1 ? 'danger' : 'success'" text @click="toggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="total > 0" v-model:current-page="page" :page-size="10" :total="total" layout="total, prev, pager, next" style="margin-top:16px" @current-change="load" />
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
const records = ref([]), total = ref(0), page = ref(1), keyword = ref(''), loading = ref(false)
async function load() {
  loading.value = true
  try { const r = await request.get('/admin/resident', { params: { page: page.value, size: 10, keyword: keyword.value || undefined } }); records.value = r.data?.records || []; total.value = r.data?.total || 0 } catch {}
  finally { loading.value = false }
}
async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(`确定${newStatus === 1 ? '启用' : '停用'}该居民？`, '提示')
  try { await request.put(`/admin/resident/${row.id}/status`, null, { params: { status: newStatus } }); ElMessage.success('操作成功'); load() } catch {}
}
onMounted(() => load())
</script>
<style scoped>.resident-manage { padding: 20px; } .filter-bar { margin-bottom: 12px; }</style>
