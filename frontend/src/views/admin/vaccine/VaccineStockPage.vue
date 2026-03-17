<template>
  <div class="vaccine-stock-page">
    <div class="page-header">
      <div class="page-header-main">
        <h2>中央疫苗管理与库房</h2>
        <p>疫苗统一入库、效期管理与库存调拨</p>
      </div>
      <div class="page-header-actions">
        <el-button type="warning" plain @click="loadAlerts">
          <AlertCircle class="btn-icon" /> 预警检测看板
        </el-button>
        <el-button type="primary" @click="showAdd = true">
          <Plus class="btn-icon" /> 登记新疫苗
        </el-button>
      </div>
    </div>

    <!-- 顶部预警栏自动提示 -->
    <div class="alert-bar" v-if="warningCount > 0" style="margin-bottom: 20px;">
      <AlertTriangle :size="16" class="alert-icon" />
      <span>当前有 <strong>{{ warningCount }}</strong> 项疫苗库存低于健康基线或即将临期失效，请立即<a @click="loadAlerts" class="alert-link">前往预警看板处理</a>！</span>
    </div>

    <div class="panel">
      <!-- 搜索过滤器 -->
      <div class="filter-section">
        <el-form :inline="true" @submit.prevent="loadList" class="compact-form">
          <el-form-item >
            <el-input v-model="keyword" placeholder="搜索疫苗通用名称/批号" clearable style="width: 250px">
              <template #prefix><Search class="input-icon" /></template>
            </el-input>
          </el-form-item>
          <el-form-item >
            <el-button type="primary" @click="loadList">查询库存</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 疫苗库存总表 -->
      <el-table :data="vaccines" stripe v-loading="loading" class="custom-table" :empty-text="'暂无匹配的疫苗记录'">
        <el-table-column prop="vaccineCode" label="疫苗编码" width="130">
           <template #default="{ row }"><span class="font-mono">{{ row.vaccineCode }}</span></template>
        </el-table-column>
        <el-table-column prop="vaccineName" label="疫苗名称 (中文通用名)" width="180">
          <template #default="{ row }"><strong class="drug-name">{{ row.vaccineName }}</strong></template>
        </el-table-column>
        <el-table-column prop="manufacturer" label="生产厂家" width="160" show-overflow-tooltip />
        <el-table-column prop="batchNo" label="现主用批号" width="130">
          <template #default="{ row }"><span class="muted-text font-mono">{{ row.batchNo || '--' }}</span></template>
        </el-table-column>
        <el-table-column label="当期可用总库存" width="140" align="center">
          <template #default="{ row }">
            <div class="stock-cell">
              <span :class="['stock-qty', getStockLevelClass(row)]">{{ row.quantity }}</span>
              <span class="stock-unit">支/剂</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="近期失效阈值" width="140" align="center">
          <template #default="{ row }">
            <span :class="{ 'text-danger': isExpiringSoon(row.expiryDate) }">{{ row.expiryDate || '未关联批次' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="基准状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', row.status === 1 ? 'done' : 'cancelled']">{{ row.status === 1 ? '启用配置' : '冻结停用' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="管理选项" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAddStock(row)">入库登记</el-button>
            <span class="action-divider">|</span>
            <el-button link type="primary" @click="viewLogs(row.id)">异动日志</el-button>
            <span class="action-divider">|</span>
            <el-button link type="warning" @click="openEdit(row)">属性维护</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="page" :page-size="20" :total="total"
        layout="prev, pager, next" @current-change="loadList" style="margin-top:16px;justify-content:center" />
    </div>

    <!-- 预警弹窗：分组合的库存报警与失效报警 -->
    <el-dialog v-model="showAlerts" title="疫苗预警联合看板" width="750px" class="custom-dialog">
      <div class="dialog-content">
        <el-tabs v-model="alertTab" class="alert-tabs">
          <el-tab-pane label="库存短缺预警" name="stock">
            <div class="tab-padding">
              <el-table :data="stockAlerts" stripe border size="small">
                <el-table-column prop="vaccineName" label="涉险疫苗名单" />
                <el-table-column prop="quantity" label="盘点库存" width="120" align="center">
                  <template #default="{ row }"><strong class="stock-danger">{{ row.quantity }}</strong></template>
                </el-table-column>
                <el-table-column prop="alertQty" label="系统健康阈值" width="120" align="center">
                  <template #default="{ row }"><span class="muted-text">{{ row.alertQty }}</span></template>
                </el-table-column>
              </el-table>
              <el-empty v-if="!stockAlerts.length" description="当前无库存预警" :image-size="60" />
            </div>
          </el-tab-pane>
          <el-tab-pane label="三十日临期/过期预警" name="expiry">
            <div class="tab-padding">
              <el-table :data="expiryAlerts" stripe border size="small">
                <el-table-column prop="vaccineName" label="涉险疫苗名单" />
                <el-table-column prop="batchNo" label="临近批次" width="120">
                  <template #default="{ row }"><span class="font-mono">{{ row.batchNo }}</span></template>
                </el-table-column>
                <el-table-column prop="expiryDate" label="最后有效限期" width="120" align="center">
                   <template #default="{ row }"><strong class="stock-danger">{{ row.expiryDate }}</strong></template>
                </el-table-column>
                <el-table-column prop="quantity" label="冻结涉及量" width="100" align="center" />
              </el-table>
              <el-empty v-if="!expiryAlerts.length" description="当前无效期预警" :image-size="60" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 新增 / 编辑疫苗基准档案表单 (复用相似模板与类) -->
    <el-dialog v-model="showAdd" title="建档：新疫苗基准配置" width="600px" class="custom-dialog">
      <div class="dialog-content">
        <el-form :model="addForm" :rules="addVaccineRules" ref="addFormRef" label-position="top">
          <div class="form-row">
            <el-form-item label="国家疫苗编排代码" prop="vaccineCode"><el-input v-model="addForm.vaccineCode" placeholder="如: VCX-09" /></el-form-item>
            <el-form-item label="生产机构全称" prop="manufacturer" class="flex-2"><el-input v-model="addForm.manufacturer" placeholder="写明生产企业" /></el-form-item>
          </div>
          <el-form-item label="通用名称" prop="vaccineName"><el-input v-model="addForm.vaccineName" placeholder="如: 重组新型冠状病毒疫苗(CHO细胞)" /></el-form-item>
          
          <div class="section-divider"><span>期初批次连带录入</span></div>
          <div class="form-row">
            <el-form-item label="到货批号"><el-input v-model="addForm.batchNo" placeholder="批次钢印" /></el-form-item>
            <el-form-item label="有限期限"><el-date-picker v-model="addForm.expiryDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="到货数量 (剂)" prop="quantity"><el-input-number v-model="addForm.quantity" :min="0" :controls="false" /></el-form-item>
            <el-form-item label="库存健康预警线"><el-input-number v-model="addForm.alertQty" :min="0" :controls="false" /></el-form-item>
          </div>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAdd = false">丢弃草稿</el-button>
          <el-button type="primary" @click="doAdd">校验并建档</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 疫苗入库表单 -->
    <el-dialog v-model="showAddStock" title="新批次疫苗入库" width="450px" class="custom-dialog">
      <div class="dialog-content">
        <el-form :rules="addStockRules" ref="addStockFormRef" label-position="top">
          <el-form-item label="对应入库疫苗">
            <div class="value-display">{{ addStockVaccine?.vaccineName }}</div>
          </el-form-item>
          <div class="form-row">
            <el-form-item label="供应批号" style="flex:1"><el-input v-model="addStockBatchNo" placeholder="仔细核对包装批号"/></el-form-item>
            <el-form-item label="入库剂型数量" style="flex:1"><el-input-number v-model="addStockQty" :min="1" :controls="false" style="width:100%" /></el-form-item>
          </div>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
           <el-button @click="showAddStock = false">取 消</el-button>
           <el-button type="primary" @click="doAddStock">确认登记入库</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 操作日志弹窗 -->
    <el-drawer v-model="showLogs" title="疫苗全生命周期异动台账" size="55%">
      <div style="padding: 0 20px;">
        <el-table :data="logs" stripe border class="drawer-table" size="small">
          <el-table-column prop="opTime" label="时间发生锚点" width="160">
            <template #default="{ row }"><span class="time-text">{{ formatTime(row.opTime) }}</span></template>
          </el-table-column>
          <el-table-column prop="opType" label="业务主向" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.opType === 1 ? 'success' : row.opType === 2 ? 'danger' : 'warning'" size="small">
                {{ { 1: '入库', 2: '流转发出', 3: '过保损控' }[row.opType] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="流转量" width="80" align="center">
             <template #default="{ row }">
               <strong :class="row.opType === 1 ? 'success-text' : 'danger-text'">
                 {{ row.opType === 1 ? '+' : '-' }}{{ row.quantity }}
               </strong>
             </template>
          </el-table-column>
          <el-table-column prop="balance" label="结余核查" width="80" align="center">
             <template #default="{ row }"><strong>{{ row.balance }}</strong></template>
          </el-table-column>
          <el-table-column prop="batchNo" label="批次穿透" width="120">
             <template #default="{ row }"><span class="font-mono">{{ row.batchNo }}</span></template>
          </el-table-column>
          <el-table-column prop="operatorName" label="经办操作人" width="100" />
          <el-table-column prop="remark" label="异动原因说明" show-overflow-tooltip />
        </el-table>
        <div style="margin-top:24px;text-align:right;">
          <el-button @click="showLogs = false">合上台账</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 编辑疫苗表单 -->
    <el-dialog v-model="showEdit" title="修改存量疫苗配置" width="600px" class="custom-dialog">
      <div class="dialog-content">
        <el-form :model="editForm" label-position="top">
          <div class="form-row">
            <el-form-item label="国家疫苗编排代码" style="flex:1"><el-input v-model="editForm.vaccineCode" /></el-form-item>
            <el-form-item label="生产机构全称" style="flex:2"><el-input v-model="editForm.manufacturer" /></el-form-item>
          </div>
          <el-form-item label="通用名称"><el-input v-model="editForm.vaccineName" /></el-form-item>
          <div class="form-row">
            <el-form-item label="系统预警干预数量" style="flex:1"><el-input-number v-model="editForm.alertQty" :min="0" :controls="false" style="width:100%" /></el-form-item>
            <el-form-item label="前台可见性与使用状态" style="flex:1">
               <el-radio-group v-model="editForm.status" style="width:100%;">
                 <el-radio-button :value="1">正常配置允许使用</el-radio-button>
                 <el-radio-button :value="0">库中强行冻结停用</el-radio-button>
               </el-radio-group>
            </el-form-item>
          </div>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEdit = false">原样返回</el-button>
          <el-button type="warning" @click="doEdit">提交强控修改</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { Plus, Search, AlertTriangle, AlertCircle } from 'lucide-vue-next'

const vaccines = ref([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const total = ref(0)

const showAlerts = ref(false)
const alertTab = ref('stock')
const stockAlerts = ref([])
const expiryAlerts = ref([])
const showAdd = ref(false)
const addForm = ref({ vaccineName: '', vaccineCode: '', manufacturer: '', batchNo: '', quantity: 0, alertQty: 10, expiryDate: '' })
const showAddStock = ref(false)
const addStockVaccine = ref(null)
const addStockQty = ref(1)
const addStockBatchNo = ref('')
const addFormRef = ref()
const addStockFormRef = ref()
const addVaccineRules = {
  vaccineCode: [{ required: true, message: '请输入疫苗编码', trigger: 'blur' }],
  vaccineName: [{ required: true, message: '请输入疫苗名称', trigger: 'blur' }],
  manufacturer: [{ required: true, message: '请输入生产厂家', trigger: 'blur' }],
  quantity: [{ required: true, type: 'number', min: 0, message: '数量必须≥ 0', trigger: 'blur' }]
}
const addStockRules = {
  batchNo: [{ required: true, message: '请输入批号', trigger: 'blur' }],
  quantity: [{ required: true, type: 'number', min: 1, message: '入库数量至少为 1', trigger: 'blur' }]
}
const showLogs = ref(false)
const logs = ref([])
const showEdit = ref(false)
const editForm = ref({})

const warningCount = computed(() => {
  return vaccines.value.filter(v => v.quantity <= v.alertQty || isExpiringSoon(v.expiryDate)).length
})

function formatTime(t) { return t ? t.replace('T', ' ').substring(0, 16) : '' }
function isExpiringSoon(d) {
  if (!d) return false
  const diff = (new Date(d) - new Date()) / 86400000
  return diff <= 30 && diff > 0
}

function getStockLevelClass(row) {
  if (row.quantity <= row.alertQty) return 'stock-danger'
  if (row.quantity <= row.alertQty * 1.5) return 'stock-warning'
  return 'stock-safe'
}

async function loadList() {
  loading.value = true
  try {
    const { data } = await request.get('/admin/vaccine-stock', { params: { page: page.value, size: 20, keyword: keyword.value } })
    vaccines.value = data.records || []
    total.value = data.total || 0
  } finally { loading.value = false }
}

async function loadAlerts() {
  const [stockRes, expiryRes] = await Promise.all([
    request.get('/admin/vaccine-stock/stock-alert'),
    request.get('/admin/vaccine-stock/expiry-alert')
  ])
  stockAlerts.value = stockRes.data || []
  expiryAlerts.value = expiryRes.data || []
  alertTab.value = 'stock'
  showAlerts.value = true
}

async function doAdd() {
  const valid = await addFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await request.post('/admin/vaccine-stock', addForm.value)
  ElMessage.success('疫苗档案创建成功')
  showAdd.value = false
  loadList()
}

function openAddStock(row) {
  addStockVaccine.value = row
  addStockQty.value = 1
  addStockBatchNo.value = row.batchNo || ''
  showAddStock.value = true
}

async function doAddStock() {
  const valid = await addStockFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await request.post(`/admin/vaccine-stock/${addStockVaccine.value.id}/add-stock`, { quantity: addStockQty.value, batchNo: addStockBatchNo.value })
  ElMessage.success('入库登记成功')
  showAddStock.value = false
  loadList()
}

async function viewLogs(vaccineId) {
  const { data } = await request.get('/admin/vaccine-stock/logs', { params: { vaccineId, page: 1, size: 50 } })
  logs.value = data.records || []
  showLogs.value = true
}

function openEdit(row) {
  editForm.value = { ...row }
  showEdit.value = true
}

async function doEdit() {
  await request.put(`/admin/vaccine-stock/${editForm.value.id}`, editForm.value)
  ElMessage.success('疫苗配置已更新')
  showEdit.value = false
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.vaccine-stock-page { padding: 20px; max-width: 1200px; margin: 0 auto; }

.btn-icon, .input-icon { width: 16px; height: 16px; margin-right: 6px; }
.input-icon { margin-right: 0; color: var(--muted); }

/* 预警图标 */
.alert-icon { margin-right: 10px; flex-shrink: 0; }
.alert-link { color: var(--text-warning); text-decoration: underline; cursor: pointer; margin-left:8px; font-weight:600;}

/* 公共类已提升至 components.css */

/* 页面独有 */
.alert-tabs :deep(.el-tabs__nav-wrap) { padding: 0 20px; }
.tab-padding { padding: 10px 20px 20px; }

/* 表单布局工具类 */
.form-row .el-form-item { flex: 1; }
.form-row .el-form-item.flex-2 { flex: 2; }
.form-row :deep(.el-input-number),
.form-row :deep(.el-date-editor) { width: 100%; }
.value-display { font-size: 14px; font-weight: 600; color: var(--text-strong); padding: 8px 0; }
.compact-form .el-form-item { margin-bottom: 0; }
.stock-unit { margin-left: 4px; font-size: 12px; color: var(--muted); }

/* 响应式 */
@media (max-width: 768px) {
  .vaccine-stock-page { padding: 12px; }
  .alert-tabs :deep(.el-tabs__nav-wrap) { padding: 0 10px; }
  .tab-padding { padding: 8px 10px 12px; }
}
</style>

