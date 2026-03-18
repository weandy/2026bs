<template>
  <div class="drug-stock-page">
    <div class="page-header">
      <div class="page-header-main">
        <h2>药品管理</h2>
        <p>库存管理、出入库日志一体化查看</p>
      </div>
      <div class="page-header-actions">
        <el-button type="primary" @click="showAdd = true">
          <Plus class="btn-icon" /> 录入新药品
        </el-button>
      </div>
    </div>

    <!-- 顶部预警栏 -->
    <div class="alert-bar" v-if="warningCount > 0" style="margin-bottom: 14px;">
      <AlertTriangle :size="16" class="alert-icon" />
      <span>当前有 <strong>{{ warningCount }}</strong> 种药品的总库存低于预警阈值，请及时采购。</span>
    </div>

    <!-- Tab 主体 -->
    <el-tabs v-model="activeTab" class="drug-tabs">

      <!-- ── 库存 Tab ── -->
      <el-tab-pane label="药品库存" name="stock">
        <div class="filter-section">
          <el-input v-model="keyword" placeholder="搜通用名/商品名" clearable style="width:240px" @keyup.enter="loadDrugs">
            <template #prefix><Search class="input-icon" /></template>
          </el-input>
          <el-button type="primary" @click="loadDrugs">检索</el-button>
        </div>

        <el-table :data="drugs" stripe v-loading="loading" class="custom-table" empty-text="暂无药品记录">
          <el-table-column prop="drugCode"      label="药品编码"    width="130">
            <template #default="{ row }"><span class="font-mono">{{ row.drugCode }}</span></template>
          </el-table-column>
          <el-table-column prop="genericName"   label="通用名"      width="160">
            <template #default="{ row }"><strong class="drug-name">{{ row.genericName }}</strong></template>
          </el-table-column>
          <el-table-column prop="tradeName"     label="商品名"      width="140">
            <template #default="{ row }"><span class="muted-text">{{ row.tradeName }}</span></template>
          </el-table-column>
          <el-table-column label="规格/剂型" width="160">
            <template #default="{ row }">{{ row.spec }} · {{ row.dosageForm }}</template>
          </el-table-column>
          <el-table-column prop="manufacturer"  label="生产厂家"    show-overflow-tooltip />
          <el-table-column label="当期总库存"   width="140" align="center">
            <template #default="{ row }">
              <div class="stock-cell">
                <span :class="['stock-qty', getStockLevelClass(row)]">{{ row._computedTotal || 0 }}</span>
                <span style="margin-left:4px; font-size:12px; color:var(--muted)">{{ row.unit }}</span>
              </div>
              <div v-if="row._computedTotal > 0 && row._computedTotal <= row.alertQty" class="alert-tip">触发预警</div>
            </template>
          </el-table-column>
          <el-table-column label="管理选项" width="120" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="viewStock(row)">入库批次</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination v-model:current-page="page" :page-size="10" :total="total"
          layout="prev, pager, next" @current-change="loadDrugs" style="margin-top:16px;justify-content:center" />
      </el-tab-pane>

      <!-- ── 出入库日志 Tab ── -->
      <el-tab-pane label="出入库日志" name="logs">
        <div class="filter-section">
          <el-input v-model="logKeyword" placeholder="搜索药品名称" clearable style="width:180px" @keyup.enter="loadLogs" />
          <el-select v-model="logDirection" placeholder="出/入库" clearable style="width:110px" @change="loadLogs">
            <el-option label="入库" :value="1" />
            <el-option label="出库" :value="2" />
          </el-select>
          <el-button @click="loadLogs">查询</el-button>
        </div>
        <el-table :data="logs" stripe size="small" v-loading="logsLoading">
          <el-table-column prop="drugName"  label="药品名称" min-width="140" />
          <el-table-column prop="direction" label="类型"     width="80" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.direction === 1 ? 'success' : 'danger'">
                {{ row.direction === 1 ? '入库' : '出库' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="quantity"  label="数量"    width="80" align="center" />
          <el-table-column prop="remark"    label="备注/来源" min-width="220" show-overflow-tooltip />
          <el-table-column label="时间"     width="150">
            <template #default="{ row }">{{ fmtLog(row.opTime || row.changeTime) }}</template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="logPage" :page-size="20" :total="logTotal"
          layout="total, prev, pager, next" small @current-change="loadLogs"
          style="margin-top:14px;justify-content:flex-end" />
      </el-tab-pane>
    </el-tabs>

    <!-- 入库批次弹窗 -->
    <el-drawer v-model="showStock" :title="`【${activeDrug?.genericName}】 库房明细`" size="55%">
      <div style="padding: 0 20px;">
        <div class="drawer-meta">
          <span>缺货预警阈值：<strong class="text-danger">{{ activeDrug?.alertQty }}</strong> {{ activeDrug?.unit }}</span>
          <el-button type="success" size="small" plain><Plus :size="14" style="margin-right:4px"/> 新批次入库</el-button>
        </div>
        <el-table :data="stocks" border stripe class="drawer-table">
          <el-table-column prop="batchNo"       label="生产批号" width="160">
            <template #default="{ row }"><span class="font-mono">{{ row.batchNo }}</span></template>
          </el-table-column>
          <el-table-column prop="quantity"      label="批次单量" width="100" align="center">
            <template #default="{ row }"><strong style="font-size:15px">{{ row.quantity }}</strong></template>
          </el-table-column>
          <el-table-column prop="supplier"      label="供应商名称" />
          <el-table-column prop="purchasePrice" label="入库单价"   width="100" align="right">
            <template #default="{ row }">¥{{ parseFloat(row.purchasePrice).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="expireDate"    label="有效期至"   width="130" align="center">
            <template #default="{ row }">
              <span :class="{ 'text-danger': isExpired(row.expireDate) }">{{ row.expireDate }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '上架' : '失效' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top:24px;text-align:right;">
          <el-button @click="showStock = false">关闭明细</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 新增药品弹窗 -->
    <el-dialog v-model="showAdd" title="药品基准档案登记" width="600px">
      <el-form :model="drugForm" label-position="top">
        <div class="form-row">
          <el-form-item label="国家药品编码" style="flex:1"><el-input v-model="drugForm.drugCode" placeholder="如: XY-1002" /></el-form-item>
          <el-form-item label="生产厂家"   style="flex:1"><el-input v-model="drugForm.manufacturer" placeholder="药厂全称" /></el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="通用名" style="flex:1"><el-input v-model="drugForm.genericName" placeholder="如: 对乙酰氨基酚" /></el-form-item>
          <el-form-item label="商品名" style="flex:1"><el-input v-model="drugForm.tradeName"   placeholder="如: 泰诺林" /></el-form-item>
        </div>
        <div class="form-row-3">
          <el-form-item label="规格"><el-input v-model="drugForm.spec"       placeholder="10mg*10片" /></el-form-item>
          <el-form-item label="剂型"><el-input v-model="drugForm.dosageForm" placeholder="口服片剂" /></el-form-item>
          <el-form-item label="单位"><el-input v-model="drugForm.unit"       placeholder="盒" /></el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取 消</el-button>
        <el-button type="primary" @click="addDrug">归档并保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Search, Plus, AlertTriangle } from 'lucide-vue-next'

// ── 库存 Tab 状态 ──
const drugs      = ref([])
const loading    = ref(false)
const keyword    = ref('')
const page       = ref(1)
const total      = ref(0)
const showStock  = ref(false)
const stocks     = ref([])
const activeDrug = ref(null)
const showAdd    = ref(false)
const drugForm   = ref({ drugCode:'', genericName:'', tradeName:'', spec:'', dosageForm:'', unit:'', manufacturer:'' })
const activeTab  = ref('stock')

// ── 日志 Tab 状态 ──
const logs          = ref([])
const logsLoading   = ref(false)
const logPage       = ref(1)
const logTotal      = ref(0)
const logKeyword    = ref('')
const logDirection  = ref(null)

const warningCount = computed(() =>
  drugs.value.filter(d => d._computedTotal !== undefined && d._computedTotal <= d.alertQty).length
)

function getStockLevelClass(row) {
  if (row._computedTotal <= row.alertQty)       return 'stock-danger'
  if (row._computedTotal <= row.alertQty * 1.5) return 'stock-warning'
  return 'stock-safe'
}

function isExpired(dateStr) {
  return dateStr ? new Date(dateStr) < new Date() : false
}

function fmtLog(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('zh-CN', { month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit' })
}

async function loadDrugs() {
  loading.value = true
  try {
    const { data } = await request.get('/admin/drug', { params: { page: page.value, size: 10, keyword: keyword.value } })
    const list = data.records || []
    await Promise.all(list.map(async (drug) => {
      try {
        const { data: stData } = await request.get(`/admin/drug/${drug.id}/stock`)
        drug._computedTotal = (stData || []).reduce((s, i) => s + (i.quantity || 0), 0)
      } catch { drug._computedTotal = 0 }
    }))
    drugs.value = list
    total.value = data.total || 0
  } finally { loading.value = false }
}

async function viewStock(row) {
  activeDrug.value = row
  try {
    const { data } = await request.get(`/admin/drug/${row.id}/stock`)
    stocks.value = data || []
    showStock.value = true
  } catch { /* handled */ }
}

async function addDrug() {
  try {
    await request.post('/admin/drug', drugForm.value)
    ElMessage.success('基准档案添加成功')
    showAdd.value = false
    loadDrugs()
    drugForm.value = { drugCode:'', genericName:'', tradeName:'', spec:'', dosageForm:'', unit:'', manufacturer:'' }
  } catch { ElMessage.error('添加失败') }
}

async function loadLogs() {
  logsLoading.value = true
  try {
    const params = { page: logPage.value, size: 20 }
    if (logKeyword.value)  params.keyword     = logKeyword.value
    if (logDirection.value !== null && logDirection.value !== '') params.changeType = logDirection.value
    const { data } = await request.get('/admin/drug/stock-logs', { params })
    logs.value    = data?.records || []
    logTotal.value= data?.total   || 0
  } finally { logsLoading.value = false }
}

onMounted(() => { loadDrugs(); loadLogs() })
</script>

<style scoped>
.drug-stock-page { padding: 0; }
.page-header { display:flex; align-items:flex-start; justify-content:space-between; margin-bottom:14px; }
.page-header-main h2 { margin:0 0 4px; font-size:18px; }
.page-header-main p  { margin:0; color:var(--muted); font-size:13px; }
.btn-icon, .input-icon { width:16px; height:16px; margin-right:6px; }
.input-icon { margin-right:0; color:var(--muted); }
.alert-icon { margin-right:10px; flex-shrink:0; }
.alert-bar  { display:flex; align-items:center; padding:10px 16px; background:var(--bg-warn,#fffbeb); border-radius:8px; color:var(--text-warning,#b45309); }
.filter-section { display:flex; gap:8px; align-items:center; margin-bottom:12px; }
.drug-tabs { }
.stock-cell { display:flex; align-items:baseline; }
.alert-tip { font-size:11px; color:var(--text-warning); background:var(--surface-warm,#fdf6ec); border-radius:10px; padding:1px 6px; display:inline-block; margin-top:2px; }
.drawer-meta { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.form-row   { display:flex; gap:16px; }
.form-row-3 { display:flex; gap:12px; }
</style>
