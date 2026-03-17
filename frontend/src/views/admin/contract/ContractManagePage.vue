<template>
  <div class="contract-manage"><div class="page-header"><h2>签约管理</h2><el-button type="primary" @click="showForm = true">新建签约</el-button></div>
    <el-table :data="records" stripe border v-loading="loading">
      <el-table-column prop="residentId" label="居民ID" width="80" />
      <el-table-column prop="teamName" label="签约团队" />
      <el-table-column prop="doctorName" label="责任医生" width="100" />
      <el-table-column prop="nurseName" label="责任护士" width="100" />
      <el-table-column prop="servicePackage" label="服务包" width="80">
        <template #default="{row}">{{ {basic:'基础',chronic:'慢病',elder:'老年'}[row.servicePackage] || row.servicePackage }}</template>
      </el-table-column>
      <el-table-column prop="contractStart" label="签约起始" width="110" />
      <el-table-column prop="contractEnd" label="签约到期" width="110" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{row}"><el-tag size="small" :type="{1:'success',2:'info',3:'danger'}[row.status]">{{ {1:'生效',2:'到期',3:'解约'}[row.status] }}</el-tag></template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="total > 0" v-model:current-page="page" :page-size="10" :total="total" layout="total, prev, pager, next" style="margin-top:16px" @current-change="load" />
    <el-dialog v-model="showForm" title="新建签约" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="居民ID"><el-input v-model.number="form.residentId" /></el-form-item>
        <el-form-item label="团队名称"><el-input v-model="form.teamName" /></el-form-item>
        <el-form-item label="医生ID"><el-input v-model.number="form.doctorId" /></el-form-item>
        <el-form-item label="医生姓名"><el-input v-model="form.doctorName" /></el-form-item>
        <el-form-item label="服务包">
          <el-select v-model="form.servicePackage" style="width:100%">
            <el-option label="基础包" value="basic" /><el-option label="慢病包" value="chronic" /><el-option label="老年包" value="elder" />
          </el-select>
        </el-form-item>
        <el-form-item label="起始日期"><el-date-picker v-model="form.contractStart" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="到期日期"><el-date-picker v-model="form.contractEnd" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showForm=false">取消</el-button><el-button type="primary" @click="submitContract">提交</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
const records = ref([]), total = ref(0), page = ref(1), loading = ref(false), showForm = ref(false)
const form = reactive({ residentId: null, teamName: '', doctorId: null, doctorName: '', servicePackage: 'basic', contractStart: '', contractEnd: '' })
async function load() {
  loading.value = true
  try { const r = await request.get('/admin/contract', { params: { page: page.value, size: 10 } }); records.value = r.data?.records || []; total.value = r.data?.total || 0 } catch {}
  finally { loading.value = false }
}
async function submitContract() {
  try { await request.post('/admin/contract', form); ElMessage.success('已创建'); showForm.value = false; load() } catch { ElMessage.error('失败') }
}
onMounted(() => load())
</script>
<style scoped>.contract-manage { padding: 20px; } .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }</style>
