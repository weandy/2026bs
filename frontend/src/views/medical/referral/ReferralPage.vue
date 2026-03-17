<template>
  <div class="referral-page"><div class="page-header"><h2>转诊管理</h2><el-button type="primary" @click="showForm = true">开具转诊单</el-button></div>
    <el-table :data="list" stripe border v-loading="loading">
      <el-table-column prop="diagnosis" label="诊断" />
      <el-table-column prop="toHospital" label="转往医院" />
      <el-table-column prop="toDept" label="转往科室" width="100" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{row}"><el-tag size="small" :type="{1:'',2:'success',3:'info'}[row.status]">{{ {1:'已开具',2:'已到诊',3:'已回转'}[row.status] }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createdAt" label="开具时间" width="170">
        <template #default="{row}">{{ row.createdAt?.replace('T',' ')?.substring(0,16) }}</template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="showForm" title="开具转诊单" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="转往医院"><el-input v-model="form.toHospital" /></el-form-item>
        <el-form-item label="转往科室"><el-input v-model="form.toDept" /></el-form-item>
        <el-form-item label="诊断"><el-input v-model="form.diagnosis" /></el-form-item>
        <el-form-item label="转诊原因"><el-input v-model="form.reason" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showForm=false">取消</el-button><el-button type="primary" @click="submitReferral">提交</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
const list = ref([]), loading = ref(false), showForm = ref(false)
const form = reactive({ toHospital: '', toDept: '', diagnosis: '', reason: '' })
async function load() { loading.value = true; try { const r = await request.get('/medical/referral'); list.value = r.data || [] } catch {} finally { loading.value = false } }
async function submitReferral() {
  try { await request.post('/medical/referral', form); ElMessage.success('已开具'); showForm.value = false; load() } catch { ElMessage.error('失败') }
}
onMounted(() => load())
</script>
<style scoped>.referral-page { padding: 20px; } .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }</style>
