<template>
  <div class="vaccination-page">
    <el-tabs v-model="activeTab" @tab-change="loadData">
      <el-tab-pane label="待接种" name="pending" />
      <el-tab-pane label="接种记录" name="records" />
    </el-tabs>

    <el-card shadow="never">
      <!-- 待接种列表 -->
      <template v-if="activeTab === 'pending'">
        <!-- 常显筛选栏 -->
        <el-form :inline="true" style="margin-bottom:12px">
          <el-form-item label="疫苗">
            <el-input v-model="pendingKeyword" placeholder="疫苗名称" clearable style="width:160px"
              @input="loadPending" @clear="loadPending" />
          </el-form-item>
          <el-form-item label="预约日">
            <el-date-picker v-model="pendingDate" type="date" value-format="YYYY-MM-DD"
              placeholder="选择日期" clearable style="width:150px" @change="loadPending" />
          </el-form-item>
        </el-form>
        <el-table :data="pendingList" stripe v-loading="loading">
          <el-table-column prop="apptNo" label="预约号" width="140" />
          <el-table-column prop="patientName" label="姓名" width="100" />
          <el-table-column prop="vaccineName" label="疫苗" width="150" />
          <el-table-column prop="doseNum" label="剂次" width="70">
            <template #default="{ row }">第{{ row.doseNum }}剂</template>
          </el-table-column>
          <el-table-column prop="apptDate" label="预约日期" width="120" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openRegister(row)">登记接种</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <!-- 接种记录 -->
      <template v-if="activeTab === 'records'">
        <el-form :inline="true" style="margin-bottom:12px">
          <el-form-item>
            <el-input v-model="searchPatientName" placeholder="接种人姓名" clearable style="width:160px"
              @keyup.enter="loadRecords" @clear="loadRecords" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadRecords">查询</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="records" stripe v-loading="loading">
          <el-table-column prop="patientName" label="接种人" width="100" />
          <el-table-column prop="vaccineName" label="疫苗" width="150" />
          <el-table-column prop="batchNo" label="批号" width="120" />
          <el-table-column prop="doseNum" label="剂次" width="70">
            <template #default="{ row }">第{{ row.doseNum }}剂</template>
          </el-table-column>
          <el-table-column prop="injectionSite" label="接种部位" width="100" />
          <el-table-column prop="staffName" label="接种护士" width="100" />
          <el-table-column prop="vaccinatedAt" label="接种时间" width="170">
            <template #default="{ row }">{{ formatTime(row.vaccinatedAt) }}</template>
          </el-table-column>
          <el-table-column prop="adverseReaction" label="不良反应" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.adverseReaction" style="color:var(--danger)">{{ row.adverseReaction }}</span>
              <span v-else style="color:var(--good)">无</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="warning" @click="openAdverse(row)">记录反应</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <el-pagination v-model:current-page="page" :page-size="20" :total="total"
        layout="total, prev, pager, next" @current-change="loadData" style="margin-top:16px;justify-content:flex-end" />
    </el-card>

    <!-- 登记接种弹窗 -->
    <el-dialog v-model="showRegister" title="登记接种" width="500px">
      <el-form :model="regForm" :rules="regRules" ref="regFormRef" label-width="80px">
        <el-form-item label="疫苗"><el-input v-model="regForm.vaccineName" disabled /></el-form-item>
        <el-form-item label="剂次"><el-input-number v-model="regForm.doseNum" :min="1" :max="10" /></el-form-item>
        <el-form-item label="批号" prop="batchNo"><el-input v-model="regForm.batchNo" placeholder="输入疫苗批号" /></el-form-item>
        <el-form-item label="接种部位" prop="injectionSite">
          <el-select v-model="regForm.injectionSite" placeholder="选择部位">
            <el-option label="左上臂" value="左上臂" />
            <el-option label="右上臂" value="右上臂" />
            <el-option label="左臀部" value="左臀部" />
            <el-option label="右臀部" value="右臀部" />
            <el-option label="口服" value="口服" />
          </el-select>
        </el-form-item>
        <el-form-item label="剂量"><el-input v-model="regForm.dosage" placeholder="如 0.5ml" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" @click="doRegister" :loading="submitting">确认登记</el-button>
      </template>
    </el-dialog>

    <!-- 不良反应弹窗 -->
    <el-dialog v-model="showAdverse" title="记录不良反应" width="400px">
      <el-form>
        <el-form-item label="不良反应">
          <el-input v-model="adverseText" type="textarea" :rows="3" placeholder="描述不良反应症状" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdverse = false">取消</el-button>
        <el-button type="warning" @click="doAdverse">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('pending')
const pendingList = ref([])
const records = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const searchPatientName = ref('')
const pendingKeyword = ref('')
const pendingDate = ref('')
const showRegister = ref(false)
const submitting = ref(false)
const regForm = ref({ vaccineName: '', doseNum: 1, batchNo: '', injectionSite: '', dosage: '', residentId: null, apptId: null, vaccineId: null })
const regRules = {
  batchNo: [{ required: true, message: '请输入疫苗批号', trigger: 'blur' }],
  injectionSite: [{ required: true, message: '请选择接种部位', trigger: 'change' }]
}
const regFormRef = ref(null)
const showAdverse = ref(false)
const adverseText = ref('')
const adverseRecordId = ref(null)

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

async function loadData() {
  if (activeTab.value === 'pending') await loadPending()
  else await loadRecords()
}

async function loadPending() {
  loading.value = true
  try {
    const params = { page: page.value, size: 20 }
    if (pendingKeyword.value) params.keyword = pendingKeyword.value
    if (pendingDate.value) params.date = pendingDate.value
    const { data } = await request.get('/medical/vaccination/pending', { params })
    pendingList.value = data.records || []
    total.value = data.total || 0
  } finally { loading.value = false }
}

async function loadRecords() {
  loading.value = true
  try {
    const params = { page: page.value, size: 20 }
    if (searchPatientName.value) params.patientName = searchPatientName.value
    const { data } = await request.get('/medical/vaccination/records', { params })
    records.value = data.records || []
    total.value = data.total || 0
  } finally { loading.value = false }
}

function openRegister(appt) {
  regForm.value = {
    vaccineName: appt.vaccineName,
    vaccineId: appt.vaccineId,
    doseNum: appt.doseNum,
    batchNo: '',
    injectionSite: '',
    dosage: '',
    residentId: appt.residentId,
    apptId: appt.id
  }
  showRegister.value = true
}

async function doRegister() {
  await regFormRef.value.validate()
  submitting.value = true
  try {
    await request.post('/medical/vaccination/register', regForm.value)
    ElMessage.success('接种登记成功')
    showRegister.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '登记失败')
  } finally { submitting.value = false }
}

function openAdverse(row) {
  adverseRecordId.value = row.id
  adverseText.value = row.adverseReaction || ''
  showAdverse.value = true
}

async function doAdverse() {
  await request.put(`/medical/vaccination/${adverseRecordId.value}/adverse`, { reaction: adverseText.value })
  ElMessage.success('已保存')
  showAdverse.value = false
  loadRecords()
}

onMounted(loadData)
</script>

<style scoped>
.vaccination-page { padding: 16px; }

/* 响应式 */
@media (max-width: 768px) {
  .vaccination-page { padding: 12px; }
}
</style>
