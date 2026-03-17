<template>
  <div class="dispense-page">

    <div class="page-header">
      <div class="page-header-main">
        <h2>药房发药与核单</h2>
        <p>待审处方实时叫号、发药余量核对</p>
      </div>
      <div class="page-header-actions">
        <!-- 状态切换标签 -->
        <div class="segmented-tabs">
          <button :class="{ active: activeTab === 'pending' }" @click="switchTab('pending')">待发药</button>
          <button :class="{ active: activeTab === 'dispensed' }" @click="switchTab('dispensed')">已发药</button>
        </div>
      </div>
    </div>

    <div class="panel">
      <!-- 处方表格区 -->
      <span class="panel-title" style="margin-bottom: 12px; display:inline-block;">
        {{ activeTab === 'pending' ? '待处置发药队列' : '历史发药记录' }}
      </span>
      <el-table :data="prescriptions" stripe v-loading="loading" class="custom-table" :empty-text="activeTab === 'pending' ? '🎉 当前暂无待发药的处方单' : '暂无发药记录'">
        <el-table-column prop="prescNo" label="处方流水号" width="180">
          <template #default="{ row }"><span class="font-mono">{{ row.prescNo }}</span></template>
        </el-table-column>
        <el-table-column prop="staffName" label="开方医生" width="120" />
        <el-table-column prop="createdAt" label="开立时间" width="180">
          <template #default="{ row }"><span class="time-text">{{ formatTime(row.createdAt) }}</span></template>
        </el-table-column>
        <el-table-column v-if="activeTab === 'dispensed'" prop="pharmacistName" label="发药药师" width="120" />
        <el-table-column v-if="activeTab === 'dispensed'" prop="dispensedAt" label="完成发药时间" width="180">
          <template #default="{ row }"><span class="time-text">{{ formatTime(row.dispensedAt) }}</span></template>
        </el-table-column>
        <el-table-column prop="notes" label="医嘱备注" show-overflow-tooltip />
        <el-table-column label="操作指令" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewItems(row.id)">看明细</el-button>
            <template v-if="activeTab === 'pending'">
              <span class="action-divider">|</span>
              <el-button link type="success" @click="confirmDispense(row.id)">确收/发药</el-button>
              <span class="action-divider">|</span>
              <el-button link type="danger" @click="showReject(row.id)">拒单退回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :page-size="20" :total="total"
        layout="prev, pager, next" @current-change="loadData" style="margin-top:16px;justify-content:center;" />
    </div>

    <!-- 处方明细抽屉（发药端采用更宽大的抽屉方便核对） -->
    <el-drawer v-model="showItems" title="电子处方核对清单" size="65%">
      <div class="drawer-content">
        <div class="alert-bar alert-info" style="margin-bottom:16px;">
          <span>发药前请仔细核对患者信息、药品批号及包装无误。</span>
        </div>

        <el-table :data="items" stripe border class="drawer-table">
          <el-table-column prop="drugName" label="药品名称" width="180">
            <template #default="{ row }"><strong>{{ row.drugName }}</strong></template>
          </el-table-column>
          <el-table-column prop="drugSpec" label="规格" width="120">
            <template #default="{ row }"><span class="muted-text">{{ row.drugSpec }}</span></template>
          </el-table-column>
          <el-table-column prop="usageMethod" label="用药途径 / 频率" width="160">
            <template #default="{ row }">{{ row.usageMethod }} · {{ row.frequency }}</template>
          </el-table-column>
          <el-table-column prop="dosage" label="单次用量" width="90" />
          <el-table-column prop="days" label="天数" width="70" align="center" />
          
          <el-table-column prop="quantity" label="开出数量" width="90" align="center">
            <template #default="{ row }"><strong class="highlight-qty">{{ row.quantity }} {{ row.drugUnit }}</strong></template>
          </el-table-column>

          <!-- 库房余量（重点核对字段） -->
          <el-table-column label="当前库房可用量" width="140" align="center">
            <template #default="{ row }">
              <div v-if="row.stockQty === null"><span class="muted-text">加载中...</span></div>
              <div v-else class="stock-cell">
                <span :class="['stock-qty', getStockLevelClass(row)]">{{ row.stockQty }}</span> {{ row.drugUnit }}
                <el-tag v-if="row.stockQty < row.quantity" type="danger" size="small" effect="dark" class="stock-tag">库存短缺</el-tag>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top:24px;text-align:right;">
          <el-button @click="showItems = false">核对毕. 关闭面板</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 退回处方弹窗 -->
    <el-dialog v-model="showRejectDlg" title="拒单/退回处方" width="450px" class="custom-dialog">
      <div class="dialog-content">
        <div class="alert-bar alert-warn" style="margin-bottom:16px;">
          <span>此操作将直接驳回该处方回到门诊大夫。如有疑问建议先致电医生。</span>
        </div>
        <el-form label-position="top">
          <el-form-item label="退回说明与驳回原因（必填）">
            <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请详述退回原因，例如“某药断货/配伍禁忌等”" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRejectDlg = false">取 消</el-button>
          <el-button type="danger" @click="doReject">确认驳回</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('pending')
const prescriptions = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const showItems = ref(false)
const items = ref([])
const showRejectDlg = ref(false)
const rejectReason = ref('')
const rejectId = ref(null)

function switchTab(tab) {
  activeTab.value = tab
  page.value = 1
  loadData()
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

function getStockLevelClass(row) {
  if (row.stockQty < row.quantity) return 'stock-danger'
  if (row.stockQty < 30) return 'stock-warning'
  return 'stock-safe'
}

async function loadData() {
  loading.value = true
  try {
    const endpoint = activeTab.value === 'pending' ? '/medical/dispense/pending' : '/medical/dispense/dispensed'
    const res = await request.get(endpoint, { params: { page: page.value, size: 20 } })
    // 处理统一回参或者直接解构
    const data = res.data || {}
    prescriptions.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    console.warn(e)
  } finally {
    loading.value = false
  }
}

async function viewItems(prescriptionId) {
  try {
    const { data } = await request.get(`/medical/dispense/${prescriptionId}/items`)
    const rawItems = data || []
    
    // 异步拉取库存
    const itemsWithStock = await Promise.all(rawItems.map(async item => {
      try {
        const { data: stocks } = await request.get(`/admin/drug/${item.drugId}/stock`)
        const totalQty = (stocks || []).reduce((s, st) => s + (st.quantity || 0), 0)
        return { ...item, stockQty: totalQty }
      } catch { return { ...item, stockQty: null } }
    }))
    
    items.value = itemsWithStock
    showItems.value = true
  } catch (e) {
    ElMessage.error('无法读取明细结构')
  }
}

async function confirmDispense(prescriptionId) {
  try {
    await ElMessageBox.confirm('您将为该处方进行发药扫尾，一经确认系统将直接扣减对于药品库存，是否继续？', '请再次核对发药动作', { 
      confirmButtonText: '绝无差错，确认发药',
      cancelButtonText: '暂不处理',
      type: 'warning' 
    })
    await request.post(`/medical/dispense/${prescriptionId}/confirm`)
    ElMessage.success('发药成功，库存已经重置核销')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '发药处理遇到了问题')
    }
  }
}

function showReject(id) {
  rejectId.value = id
  rejectReason.value = ''
  showRejectDlg.value = true
}

async function doReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  try {
    await request.post(`/medical/dispense/${rejectId.value}/reject`, { reason: rejectReason.value })
    ElMessage.success('处方已驳回')
    showRejectDlg.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败，请稍后重试')
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.dispense-page { padding: 20px; max-width: 1200px; margin: 0 auto; }

/* segmented-tabs 已提升至 components.css */
/* 公共类（custom-table, font-mono, stock 系列, drawer-table 等）已在 components.css 中定义 */

/* 页面独有 */
.drawer-content { padding: 0 20px; }
.stock-tag { margin-left: 4px; }
.highlight-qty { font-size: 15px; color: var(--text-strong); font-weight: 700; }

/* 响应式 */
@media (max-width: 768px) {
  .dispense-page { padding: 12px; }
  .drawer-content { padding: 0 12px; }
}
</style>
