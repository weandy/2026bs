<template>
  <div class="visit-record-page" :class="{ 'desktop-mode': isDesktop }">
    <div class="page-header">
      <h2>就诊记录</h2>
    </div>

    <el-form :inline="true" class="filter-section">
      <el-form-item label="状态">
        <el-radio-group v-model="filterStatus" @change="loadRecords">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button :value="1">就诊中</el-radio-button>
          <el-radio-button :value="2">已完成</el-radio-button>
          <el-radio-button :value="3">已取消</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <div class="vr-layout">

      <!-- 左侧 记录列表 -->
      <div class="vr-list-col">
        <el-card v-for="record in records" :key="record.id" class="record-card" shadow="hover"
                 :class="{ selected: detailRecord?.id === record.id }"
                 @click="showDetail(record)">
          <div class="record-header">
            <span class="record-time">{{ formatDate(record.visitDate || record.createdAt) }}</span>
            <el-tag :type="statusType(record.status)" size="small">{{ statusText(record.status) }}</el-tag>
          </div>
          <div class="record-body">
            <div class="visit-no-row">{{ record.visitNo || ('VR' + record.id) }}</div>
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
      </div>

      <!-- 右侧 详情面板 (桌面端 inline) -->
      <div class="vr-detail-col" v-if="isDesktop">
        <div v-if="detailRecord" class="vr-detail-card">
          <h3 class="detail-heading">就诊详情</h3>
          <el-descriptions :column="2" border size="small">
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
        </div>
        <div v-else class="vr-detail-empty">
          <el-empty description="点击左侧记录查看详情" :image-size="100" />
        </div>
      </div>

    </div>

    <!-- 移动端弹窗 (保留) -->
    <el-dialog v-if="!isDesktop" v-model="showDetailDlg" title="就诊详情" width="700px">
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'

const records = ref([])
const currentPage = ref(1)
const total = ref(0)
const filterStatus = ref(null)
const showDetailDlg = ref(false)
const detailRecord = ref(null)
const relatedPrescriptions = ref([])
const loadingPres = ref(false)

// 响应式断点
const windowWidth = ref(window.innerWidth)
const isDesktop = computed(() => windowWidth.value >= 768)
function onResize() { windowWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))

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
  if (!isDesktop.value) {
    showDetailDlg.value = true
  }
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
/* ══ 移动端默认样式 ══ */
.vr-layout {
  display: block;
}

.record-card { margin-bottom: 12px; cursor: pointer; transition: border-color 0.2s; }
.record-card:hover { border-color: var(--primary); }
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

/* ══ PC 桌面端 列表+详情 布局 (>= 768px) ══ */
@media (min-width: 768px) {
  .visit-record-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 16px 24px;
  }

  .vr-layout {
    display: grid;
    grid-template-columns: 5fr 7fr;
    gap: 20px;
    align-items: start;
    min-height: 60vh;
  }

  .vr-list-col {
    max-height: calc(100vh - 200px);
    overflow-y: auto;
    padding-right: 4px;
  }

  .record-card.selected {
    border-color: var(--primary) !important;
    box-shadow: 0 0 0 2px rgba(47, 107, 87, 0.15);
  }

  /* 右侧详情面板 */
  .vr-detail-col {
    position: sticky;
    top: 80px;
  }
  .vr-detail-card {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 16px;
    padding: 24px;
  }
  .detail-heading {
    margin: 0 0 16px;
    font-size: 18px;
    font-weight: 600;
    color: var(--text);
  }
  .vr-detail-empty {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 16px;
    padding: 60px 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 400px;
  }
}
</style>
