<template>
  <div class="visit-record-page">
    <div class="page-header">
      <h2>就诊记录</h2>
    </div>

    <!-- 筛选条件 -->
    <el-form :inline="true" class="filter-section">
      <el-form-item label="状态">
        <el-select v-model="filterStatus" clearable placeholder="全部" @change="loadRecords">
          <el-option label="就诊中" :value="1" />
          <el-option label="已完成" :value="2" />
          <el-option label="已取消" :value="3" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-card v-for="record in records" :key="record.id" class="record-card" shadow="hover"
             @click="showDetail(record)">
      <div class="record-header">
        <span class="visit-no">{{ record.visitNo || ('VR' + record.id) }}</span>
        <el-tag :type="statusType(record.status)" size="small">{{ statusText(record.status) }}</el-tag>
      </div>
      <div class="record-body">
        <div>科室：{{ record.deptName }} | 医生：{{ record.staffName }}</div>
        <div>主诉：{{ record.chiefComplaint || '—' }}</div>
        <div v-if="record.diagnosisNames" style="color:var(--info)">诊断：{{ record.diagnosisNames }}</div>
        <div class="record-time">{{ formatDate(record.visitDate || record.createdAt) }}</div>
      </div>
    </el-card>

    <el-empty v-if="records.length === 0" description="暂无就诊记录" />

    <el-pagination
      v-if="total > 0"
      v-model:current-page="currentPage"
      :page-size="10"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 16px; justify-content: center;"
      @current-change="loadRecords"
    />

    <!-- 就诊详情弹窗 -->
    <el-dialog v-model="showDetailDlg" title="就诊详情" width="700px">
      <template v-if="detailRecord">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="就诊编号" :span="2">{{ detailRecord.visitNo || ('VR' + detailRecord.id) }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ detailRecord.deptName }}</el-descriptions-item>
          <el-descriptions-item label="医生">{{ detailRecord.staffName }}</el-descriptions-item>
          <el-descriptions-item label="就诊日期">{{ detailRecord.visitDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detailRecord.status)" size="small">{{ statusText(detailRecord.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="主诉" :span="2">{{ detailRecord.chiefComplaint || '—' }}</el-descriptions-item>
          <el-descriptions-item label="现病史" :span="2">{{ detailRecord.presentIllness || '—' }}</el-descriptions-item>
          <el-descriptions-item label="体格检查" :span="2">{{ detailRecord.physicalExam || '—' }}</el-descriptions-item>
          <el-descriptions-item label="诊断" :span="2">
            <span style="color:var(--info);font-weight:500">{{ detailRecord.diagnosisNames || '—' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="医嘱" :span="2">{{ detailRecord.medicalAdvice || '—' }}</el-descriptions-item>
          <el-descriptions-item label="复诊日期">{{ detailRecord.revisitDate || '—' }}</el-descriptions-item>
          <el-descriptions-item label="复诊备注">{{ detailRecord.revisitNote || '—' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 关联处方 -->
        <h4 style="margin-top:16px">关联处方</h4>
        <el-table :data="relatedPrescriptions" stripe v-loading="loadingPres" border
          @expand-change="handlePresExpand" row-key="id">
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="presc-items-box" v-loading="row._loadingItems">
                <div v-if="row._items && row._items.length" class="presc-items-grid">
                  <div v-for="item in row._items" :key="item.id" class="presc-drug-item">
                    <strong>{{ item.drugName }}</strong>
                    <span class="drug-spec">{{ item.spec || '' }}</span>
                    <div class="drug-usage">
                      {{ item.usageMethod || '' }} | {{ item.frequency || '' }} | {{ item.days || '' }}天
                    </div>
                    <div class="drug-qty">×{{ item.quantity }}</div>
                  </div>
                </div>
                <el-empty v-else-if="!row._loadingItems" description="暂无药品明细" :image-size="36" />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="prescNo" label="处方号" width="140" />
          <el-table-column prop="prescType" label="类型" width="80">
            <template #default="{ row }">{{ { 1: '西药', 2: '中药', 3: '检查' }[row.prescType] || '—' }}</template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="金额" width="80" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 2 ? 'success' : ''">{{ { 1: '待审核', 2: '已审核', 3: '已退回' }[row.status] || '—' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="开方时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="relatedPrescriptions.length === 0 && !loadingPres" description="暂无关联处方" :image-size="48" />
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const records = ref([])
const currentPage = ref(1)
const total = ref(0)
const filterStatus = ref(null)
const showDetailDlg = ref(false)
const detailRecord = ref(null)
const relatedPrescriptions = ref([])
const loadingPres = ref(false)

function statusType(s) { return { 1: '', 2: 'success', 3: 'info' }[s] || 'info' }
function statusText(s) { return { 1: '就诊中', 2: '已完成', 3: '已取消' }[s] || '未知' }
function formatDate(dt) { if (!dt) return ''; return String(dt).substring(0, 10) }
function formatDateTime(dt) { if (!dt) return ''; return dt.replace('T', ' ').substring(0, 16) }

async function loadRecords() {
  try {
    const params = { page: currentPage.value, size: 10 }
    if (filterStatus.value) params.status = filterStatus.value
    const res = await request.get('/resident/visit-record', { params })
    records.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) { console.warn(e) }
}

async function showDetail(record) {
  detailRecord.value = record
  showDetailDlg.value = true
  relatedPrescriptions.value = []
  loadingPres.value = true
  try {
    const res = await request.get('/resident/visit-record/prescriptions', { params: { visitRecordId: record.id } })
    relatedPrescriptions.value = res.data || []
  } catch (e) { console.warn(e) }
  finally { loadingPres.value = false }
}

async function handlePresExpand(row, expandedRows) {
  if (!row._items && expandedRows.includes(row)) {
    row._loadingItems = true
    try {
      const res = await request.get(`/resident/visit-record/prescription/${row.id}/items`)
      row._items = res.data || []
    } catch { row._items = [] }
    finally { row._loadingItems = false }
  }
}

onMounted(() => loadRecords())
</script>

<style scoped>
.record-card { margin-bottom: 12px; cursor: pointer; }
.record-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.visit-no { font-weight: 600; color: var(--primary-color); }
.record-body { font-size: 13px; color: var(--text-secondary); line-height: 1.8; }
.record-time { font-size: 12px; color: var(--muted); margin-top: 4px; }

/* R2 处方药品明细 */
.presc-items-box { padding: 8px 12px; }
.presc-items-grid { display: flex; flex-direction: column; gap: 8px; }
.presc-drug-item {
  display: grid; grid-template-columns: 1fr auto; gap: 4px 12px;
  padding: 8px 12px; background: var(--el-fill-color-lighter); border-radius: 6px;
}
.presc-drug-item strong { font-size: 13px; }
.drug-spec { font-size: 12px; color: var(--muted); text-align: right; }
.drug-usage { font-size: 12px; color: var(--text-secondary); grid-column: 1 / -1; }
.drug-qty { font-size: 13px; font-weight: 600; color: var(--primary-color); text-align: right; }

/* 移动端适配 */
@media (max-width: 768px) {
  .visit-record-page { padding: 12px; }
  .visit-record-page :deep(.el-dialog) {
    width: calc(100vw - 24px) !important;
  }
}
</style>
