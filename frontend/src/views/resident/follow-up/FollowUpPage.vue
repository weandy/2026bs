<template>
  <div class="followup-page">
    <div class="page-header"><h2>我的随访</h2></div>
    <div v-for="plan in plans" :key="plan.id" class="plan-card" @click="loadRecords(plan)">
      <div class="plan-header">
        <strong>{{ diseaseLabel(plan.diseaseType) }}</strong>
        <span class="freq-tag">{{ plan.frequency }}</span>
      </div>
      <div class="plan-meta">下次随访：{{ plan.nextFollowDate || '待安排' }}</div>
    </div>
    <div v-if="plans.length === 0" class="empty-tip">暂无随访计划</div>
    <!-- 随访记录弹窗 -->
    <el-dialog v-model="showRecords" title="随访记录" width="600px">
      <el-table :data="records" stripe border>
        <el-table-column prop="followDate" label="随访日期" width="120">
          <template #default="{ row }">{{ row.followDate?.substring(0,10) }}</template>
        </el-table-column>
        <el-table-column prop="bloodPressure" label="血压" width="100" />
        <el-table-column prop="bloodGlucose" label="血糖" width="80" />
        <el-table-column prop="weight" label="体重" width="80" />
        <el-table-column prop="note" label="备注" />
        <el-table-column prop="operatorName" label="随访人" width="80" />
      </el-table>
      <div style="margin-top:16px">
        <h4>自报指标</h4>
        <el-form :inline="true" :model="selfReport">
          <el-form-item label="血压"><el-input v-model="selfReport.bloodPressure" size="small" style="width:100px" /></el-form-item>
          <el-form-item label="血糖"><el-input v-model="selfReport.bloodGlucose" size="small" style="width:80px" /></el-form-item>
          <el-form-item><el-button type="primary" size="small" @click="submitSelfReport">提交</el-button></el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
const plans = ref([])
const records = ref([])
const showRecords = ref(false)
const currentPlanId = ref(null)
const selfReport = reactive({ bloodPressure: '', bloodGlucose: '' })
function diseaseLabel(t) { return { hypertension: '高血压随访', diabetes: '糖尿病随访', chd: '冠心病随访', copd: '慢阻肺随访' }[t] || t }
async function loadPlans() {
  try { const res = await request.get('/resident/follow-up/plan'); plans.value = res.data || [] } catch { plans.value = [] }
}
async function loadRecords(plan) {
  currentPlanId.value = plan.id
  showRecords.value = true
  try { const res = await request.get(`/resident/follow-up/record/${plan.id}`); records.value = res.data || [] } catch { records.value = [] }
}
async function submitSelfReport() {
  try {
    await request.post('/resident/follow-up/self-report', { planId: currentPlanId.value, ...selfReport })
    ElMessage.success('已提交')
    loadRecords({ id: currentPlanId.value })
    selfReport.bloodPressure = ''; selfReport.bloodGlucose = ''
  } catch { ElMessage.error('提交失败') }
}
onMounted(() => loadPlans())
</script>
<style scoped>
.followup-page { padding: 16px; max-width: 500px; margin: 0 auto; }
.plan-card { background: var(--surface); border: 1px solid var(--border); border-radius: 14px; padding: 14px 16px; margin-bottom: 10px; cursor: pointer; transition: transform 0.15s; }
.plan-card:hover { transform: translateY(-1px); }
.plan-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.freq-tag { font-size: 11px; padding: 2px 8px; border-radius: 4px; background: rgba(47,107,87,0.1); color: var(--primary); }
.plan-meta { font-size: 12px; color: var(--muted); }
.empty-tip { text-align: center; padding: 40px; color: var(--muted); }
</style>
