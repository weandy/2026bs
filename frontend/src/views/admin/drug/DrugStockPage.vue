<template>
  <div class="drug-stock-page">
    <div class="page-header">
      <div class="page-header-main">
        <h2>药品大库库存</h2>
        <p>中央药品入库、预警及明细管理</p>
      </div>
      <div class="page-header-actions">
        <el-button type="primary" @click="showAdd = true">
          <Plus class="btn-icon" /> 录入新药品
        </el-button>
      </div>
    </div>

    <!-- 顶部预警栏 -->
    <div class="alert-bar" v-if="warningCount > 0" style="margin-bottom: 20px;">
      <AlertTriangle :size="16" class="alert-icon" />
      <span>当前有 <strong>{{ warningCount }}</strong> 种药品的总库存低于系统预警阈值，请及时向供应商提出采购申请。</span>
    </div>

    <div class="panel">
      <!-- 紧凑型搜索栏 -->
      <div class="filter-section">
        <el-form :inline="true" @submit.prevent="loadDrugs" class="compact-form">
          <el-form-item style="margin-bottom:0">
            <el-input v-model="keyword" placeholder="搜通用名/商品名" clearable style="width: 240px">
              <template #prefix><Search class="input-icon" /></template>
            </el-input>
          </el-form-item>
          <el-form-item style="margin-bottom:0">
            <el-button type="primary" @click="loadDrugs">检索库存</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 药品底表 -->
      <el-table :data="drugs" stripe v-loading="loading" class="custom-table" :empty-text="'暂无药品记录'">
        <el-table-column prop="drugCode" label="药品编码" width="130">
          <template #default="{ row }"><span class="font-mono">{{ row.drugCode }}</span></template>
        </el-table-column>
        <el-table-column prop="genericName" label="通用名(化学名)" width="160">
          <template #default="{ row }"><strong class="drug-name">{{ row.genericName }}</strong></template>
        </el-table-column>
        <el-table-column prop="tradeName" label="商品名" width="140">
          <template #default="{ row }"><span class="muted-text">{{ row.tradeName }}</span></template>
        </el-table-column>
        <el-table-column label="属性(规格/剂型)" width="160">
          <template #default="{ row }">{{ row.spec }} · {{ row.dosageForm }}</template>
        </el-table-column>
        <el-table-column prop="manufacturer" label="生产厂家" show-overflow-tooltip />

        <!-- 当前总量与预警处理（前端实时汇总或展示） -->
        <el-table-column label="当期总库存" width="140" align="center">
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
    </div>

    <!-- 具体入库批次弹窗 -->
    <el-drawer v-model="showStock" :title="`【${activeDrug?.genericName}】 库房明细`" size="55%">
      <div style="padding: 0 20px;">
        <div class="drawer-meta">
          <span>当前系统设定的缺货预警阈值：<strong class="text-danger">{{ activeDrug?.alertQty }}</strong> {{ activeDrug?.unit }}</span>
          <el-button type="success" size="small" plain><Plus :size="14" style="margin-right:4px"/> 新批次入库</el-button>
        </div>
        <el-table :data="stocks" border stripe class="drawer-table">
          <el-table-column prop="batchNo" label="生产批号" width="160">
            <template #default="{ row }"><span class="font-mono">{{ row.batchNo }}</span></template>
          </el-table-column>
          <el-table-column prop="quantity" label="批次单量" width="100" align="center">
            <template #default="{ row }"><strong style="font-size:15px">{{ row.quantity }}</strong></template>
          </el-table-column>
          <el-table-column prop="supplier" label="供应商名称" />
          <el-table-column prop="purchasePrice" label="入库单价" width="100" align="right">
            <template #default="{ row }">¥{{ parseFloat(row.purchasePrice).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="expireDate" label="有效期至" width="130" align="center">
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

    <!-- 新增药品表单 -->
    <el-dialog v-model="showAdd" title="药品基准档案登记" width="600px" class="custom-dialog">
      <div class="dialog-content">
        <el-form :model="drugForm" label-position="top">
          <div class="form-row">
            <el-form-item label="国家药品编码" style="flex:1"><el-input v-model="drugForm.drugCode" placeholder="如: XY-1002" /></el-form-item>
            <el-form-item label="生产厂家" style="flex:1"><el-input v-model="drugForm.manufacturer" placeholder="药厂全称" /></el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="通用名(化学成分)" style="flex:1"><el-input v-model="drugForm.genericName" placeholder="如: 对乙酰氨基酚" /></el-form-item>
            <el-form-item label="商品名(品牌)" style="flex:1"><el-input v-model="drugForm.tradeName" placeholder="如: 泰诺林" /></el-form-item>
          </div>
          <div class="form-row-3">
            <el-form-item label="规格"><el-input v-model="drugForm.spec" placeholder="10mg*10片" /></el-form-item>
            <el-form-item label="剂型"><el-input v-model="drugForm.dosageForm" placeholder="口服片剂" /></el-form-item>
            <el-form-item label="包装单位"><el-input v-model="drugForm.unit" placeholder="盒" /></el-form-item>
          </div>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAdd = false">取 消</el-button>
          <el-button type="primary" @click="addDrug">归档并保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Search, Plus, AlertTriangle } from 'lucide-vue-next'

const drugs = ref([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const total = ref(0)
const showStock = ref(false)
const stocks = ref([])
const activeDrug = ref(null)
const showAdd = ref(false)
const drugForm = ref({ drugCode: '', genericName: '', tradeName: '', spec: '', dosageForm: '', unit: '', manufacturer: '' })

// 预警数统计
const warningCount = computed(() => {
  return drugs.value.filter(d => d._computedTotal !== undefined && d._computedTotal <= d.alertQty).length
})

function getStockLevelClass(row) {
  if (row._computedTotal <= row.alertQty) return 'stock-danger'
  if (row._computedTotal <= row.alertQty * 1.5) return 'stock-warning'
  return 'stock-safe'
}

function isExpired(dateStr) {
  if (!dateStr) return false
  return new Date(dateStr) < new Date()
}

// 并发拉取当前页所有药品的最新库存量进行前台汇总展示
const loadDrugs = async () => {
  loading.value = true
  try {
    const { data } = await request.get('/admin/drug', { params: { page: page.value, size: 10, keyword: keyword.value } })
    const list = data.records || []
    
    // 异步加载所有条目的库存总和
    await Promise.all(list.map(async (drug) => {
      try {
        const { data: stData } = await request.get(`/admin/drug/${drug.id}/stock`)
        drug._computedTotal = (stData || []).reduce((sum, item) => sum + (item.quantity || 0), 0)
      } catch (e) {
        drug._computedTotal = 0
      }
    }))
    
    drugs.value = list
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const viewStock = async (row) => {
  activeDrug.value = row
  try {
    const { data } = await request.get(`/admin/drug/${row.id}/stock`)
    stocks.value = data || []
    showStock.value = true
  } catch(e) { /* handled */ }
}

const addDrug = async () => {
  try {
    await request.post('/admin/drug', drugForm.value)
    ElMessage.success('基准档案添加成功')
    showAdd.value = false
    loadDrugs()
    drugForm.value = { drugCode: '', genericName: '', tradeName: '', spec: '', dosageForm: '', unit: '', manufacturer: '' }
  } catch(e) { ElMessage.error('添加失败')}
}

onMounted(loadDrugs)
</script>

<style scoped>
.drug-stock-page { padding: 20px; max-width: 1200px; margin: 0 auto; }

.btn-icon, .input-icon { width: 16px; height: 16px; margin-right: 6px; }
.input-icon { margin-right: 0; color: var(--muted); }

/* 预警图标 */
.alert-icon { margin-right: 10px; flex-shrink: 0; }

/* 公共类已提升至 components.css */

/* 库存预警提示（页面独有） */
.alert-tip { font-size: 11px; color: var(--text-warning); background: var(--surface-warm, #fdf6ec); border-radius: 10px; padding: 1px 6px; display: inline-block; margin-top:2px;}

/* 响应式 */
@media (max-width: 768px) {
  .drug-stock-page { padding: 12px; }
  .alert-tip { font-size: 10px; padding: 1px 4px; }
}
</style>

