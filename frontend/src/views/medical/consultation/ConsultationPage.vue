<template>
  <div class="consultation-page"><div class="page-header"><h2>会诊管理</h2><el-button type="primary" @click="showForm = true">发起会诊</el-button></div>
    <el-table :data="list" stripe border v-loading="loading">
      <el-table-column prop="targetDeptName" label="目标科室" width="120" />
      <el-table-column prop="reason" label="会诊原因" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{row}"><el-tag size="small" :type="{1:'warning',2:'',3:'success',4:'danger'}[row.status]">{{ {1:'待处理',2:'已接受',3:'已完成',4:'已拒绝'}[row.status] }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="responseNote" label="回复" />
      <el-table-column prop="createdAt" label="时间" width="170">
        <template #default="{row}">{{ row.createdAt?.replace('T',' ')?.substring(0,16) }}</template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="showForm" title="发起会诊" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="目标科室"><el-input v-model="form.targetDeptName" /></el-form-item>
        <el-form-item label="会诊原因"><el-input v-model="form.reason" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showForm=false">取消</el-button><el-button type="primary" @click="submitConsult">提交</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
const list = ref([]), loading = ref(false), showForm = ref(false)
const form = reactive({ targetDeptName: '', reason: '' })
async function load() { loading.value = true; try { const r = await request.get('/medical/consultation'); list.value = r.data || [] } catch {} finally { loading.value = false } }
async function submitConsult() {
  try { await request.post('/medical/consultation', form); ElMessage.success('已发起'); showForm.value = false; load() } catch { ElMessage.error('失败') }
}
onMounted(() => load())
</script>
<style scoped>.consultation-page { padding: 20px; } .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }</style>
