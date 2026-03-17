<template>
  <div class="prescription-page">
    <div class="page-header">
      <div class="page-header-main">
        <h2>处方管理</h2>
        <p>就诊开方及历史处方查阅</p>
      </div>
      <div class="page-header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <Plus class="btn-icon"/> 新建处方
        </el-button>
      </div>
    </div>

    <div class="panel">
      <!-- 搜索过滤器 -->
      <div class="filter-section">
        <el-form :inline="true" @submit.prevent="loadPrescriptions" class="compact-form">
          <el-form-item label="就诊ID" style="margin-bottom:0">
            <el-input v-model="visitId" placeholder="请输入发号/就诊ID查询" clearable style="width: 240px">
              <template #prefix><Search class="input-icon" /></template>
            </el-input>
          </el-form-item>
          <el-form-item style="margin-bottom:0">
            <el-button type="primary" @click="loadPrescriptions">查询开方记录</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 处方表格 -->
      <el-table :data="prescriptions" stripe v-loading="loading" class="custom-table" :empty-text="'暂无记录，请输入有效的就诊ID查询'">
        <el-table-column prop="prescNo" label="处方单号" width="180">
          <template #default="{ row }"><span class="font-mono">{{ row.prescNo }}</span></template>
        </el-table-column>
        <el-table-column prop="staffName" label="开方医生" width="120" />
        <el-table-column prop="status" label="发药状态" width="120">
          <template #default="{ row }">
            <span :class="['status-tag', statusTheme(row.status)]">
              {{ statusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="notes" label="临床备注" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="开方时间" width="180">
          <template #default="{ row }"><span class="time-text">{{ formatDate(row.createdAt) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewItems(row.id)">看明细</el-button>
            <el-button link type="primary" @click="printPdf(row.id)">打印处方</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 处方明细抽屉 -->
    <el-drawer v-model="showItems" title="处方单明细" size="50%">
      <div style="padding: 0 20px;">
        <el-table :data="items" border class="drawer-table">
          <el-table-column prop="drugName" label="药品名称" width="160">
            <template #default="{ row }"><strong>{{ row.drugName }}</strong></template>
          </el-table-column>
          <el-table-column prop="drugSpec" label="规格" width="120">
            <template #default="{ row }"><span class="muted-text">{{ row.drugSpec }}</span></template>
          </el-table-column>
          <el-table-column prop="quantity" label="开药量" width="80" align="center" />
          <el-table-column prop="usageMethod" label="用药途径" width="100" />
          <el-table-column prop="dosage" label="单次剂量" width="100" />
          <el-table-column prop="frequency" label="频次(每天)" width="100" />
          <el-table-column prop="days" label="用药天数" width="80" align="center" />
        </el-table>
        <div style="margin-top:24px;text-align:right;">
          <el-button @click="showItems = false">关闭明细</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 开方对话框 -->
    <el-dialog v-model="showCreateDialog" title="新建电子处方" width="840px" class="custom-dialog" destroy-on-close>
      <div class="dialog-content">
        <el-form :model="createForm" label-position="top" class="presc-form">
          <div class="form-row">
            <el-form-item label="就诊记录ID" style="flex:1">
              <el-input-number v-model="createForm.visitId" :min="1" :controls="false" style="width:100%" placeholder="必填" />
            </el-form-item>
            <el-form-item label="医嘱备注" style="flex:3">
              <el-input v-model="createForm.notes" placeholder="选填，如：注意复查肝肾功能" />
            </el-form-item>
          </div>
        </el-form>

        <div class="section-divider"><span>用药明细</span></div>

        <el-table :data="createForm.items" class="edit-table" border size="small">
          <el-table-column label="选择药品" width="220">
            <template #default="{ row }">
              <el-select v-model="row.drugId" placeholder="搜索并选取药品" filterable @change="onDrugSelect(row)" style="width:100%">
                <el-option v-for="d in drugList" :key="d.id" :label="`${d.name} (${d.spec})`" :value="d.id" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="发药量" width="100">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" size="small" :controls="false" style="width:100%" />
            </template>
          </el-table-column>
          <el-table-column label="用法" width="100">
            <template #default="{ row }"><el-input v-model="row.usageMethod" placeholder="如: 口服" /></template>
          </el-table-column>
          <el-table-column label="单次剂量" width="100">
            <template #default="{ row }"><el-input v-model="row.dosage" placeholder="如: 1片" /></template>
          </el-table-column>
          <el-table-column label="用药频次" width="100">
            <template #default="{ row }"><el-input v-model="row.frequency" placeholder="如: tid" /></template>
          </el-table-column>
          <el-table-column label="天数" width="80">
            <template #default="{ row }">
              <el-input-number v-model="row.days" :min="1" size="small" :controls="false" style="width:100%" />
            </template>
          </el-table-column>
          <el-table-column width="60" align="center">
            <template #default="{ $index }">
              <el-button link type="danger" @click="createForm.items.splice($index, 1)">
                <Trash2 :size="16" />
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-button plain type="primary" @click="addDrugRow" style="margin-top: 16px; width: 100%; border-style: dashed;">
          <Plus :size="16" style="margin-right:4px;" /> 继续添加药品明细
        </el-button>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取 消</el-button>
          <el-button type="primary" :loading="creating" @click="submitPrescription">确认签发处方</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { Search, Plus, Trash2 } from 'lucide-vue-next'

const visitId = ref('')
const prescriptions = ref([])
const loading = ref(false)
const showItems = ref(false)
const items = ref([])
const showCreateDialog = ref(false)
const creating = ref(false)
const drugList = ref([])

const createForm = ref({
  visitId: null,
  notes: '',
  items: [{ drugId: null, drugName: '', drugSpec: '', quantity: 1, usageMethod: '口服', dosage: '', frequency: 'tid', days: 3 }]
})

function statusTheme(s) {
  return { 1: 'pending', 2: 'done', 3: 'cancelled' }[s] || 'default'
}

function statusText(s) {
  return { 1: '待发放', 2: '已发药', 3: '已追回' }[s] || '未知'
}

function formatDate(dt) {
  return dt ? dt.replace('T', ' ').substring(0, 16) : '--'
}

function addDrugRow() {
  createForm.value.items.push({ drugId: null, drugName: '', drugSpec: '', quantity: 1, usageMethod: '口服', dosage: '', frequency: 'tid', days: 3 })
}

function onDrugSelect(row) {
  const drug = drugList.value.find(d => d.id === row.drugId)
  if (drug) {
    row.drugName = drug.name
    row.drugSpec = drug.spec
  }
}

const loadPrescriptions = async () => {
  if (!visitId.value) return
  loading.value = true
  try {
    const { data } = await request.get(`/medical/prescription/visit/${visitId.value}`)
    prescriptions.value = data || []
  } finally {
    loading.value = false
  }
}

const viewItems = async (prescriptionId) => {
  const { data } = await request.get(`/medical/prescription/${prescriptionId}/items`)
  items.value = data || []
  showItems.value = true
}

const printPdf = async (prescriptionId) => {
  try {
    const res = await request.post(`/medical/prescription/${prescriptionId}/pdf`, {}, { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `prescription_${prescriptionId}.pdf`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    ElMessage.success('PDF 下载成功')
  } catch (e) {
    ElMessage.error('PDF 生成失败')
  }
}

const loadDrugs = async () => {
  try {
    const { data } = await request.get('/admin/drug', { params: { page: 1, size: 500 } })
    drugList.value = (data.records || []).map(d => ({ id: d.id, name: d.name, spec: d.spec }))
  } catch (e) { /* handled */ }
}

const submitPrescription = async () => {
  if (!createForm.value.visitId) {
    ElMessage.warning('请填写关联的就诊记录ID')
    return
  }
  const validItems = createForm.value.items.filter(i => i.drugId)
  if (validItems.length === 0) {
    ElMessage.warning('到处方单内至少需添加一种有效的药品')
    return
  }
  
  creating.value = true
  try {
    await request.post('/medical/prescription', {
      ...createForm.value,
      items: validItems
    })
    ElMessage.success('电子处方开立成功')
    showCreateDialog.value = false
    visitId.value = String(createForm.value.visitId)
    loadPrescriptions()
    createForm.value = {
      visitId: null, notes: '',
      items: [{ drugId: null, drugName: '', drugSpec: '', quantity: 1, usageMethod: '口服', dosage: '', frequency: 'tid', days: 3 }]
    }
  } catch (e) { /* handled */ }
  finally { creating.value = false }
}

onMounted(() => loadDrugs())
</script>

<style scoped>
.prescription-page { padding: 20px; max-width: 1200px; margin: 0 auto; }

.btn-icon { width: 16px; height: 16px; margin-right: 6px; }
.input-icon { width: 16px; height: 16px; color: var(--muted); }

/* 公共类已在 components.css 中定义 */

/* 编辑表格（去除内部多余留白） */
.edit-table :deep(.el-input__wrapper) { box-shadow: none; padding: 0 8px; background: transparent; }
.edit-table :deep(.el-input__inner) { text-align: center; font-size: 13px; }
.edit-table :deep(.el-input-number.is-without-controls .el-input__wrapper) { padding: 0; }

/* 响应式 */
@media (max-width: 768px) {
  .prescription-page { padding: 12px; }
}
</style>
