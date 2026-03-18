<template>
  <div class="inventory-page">
    <!-- 外层主 Tab：药品管理 / 疫苗管理 -->
    <el-tabs v-model="mainTab" class="main-tabs" type="border-card">

      <!-- ════════════════════════════════════════
           药品管理
           ════════════════════════════════════════ -->
      <el-tab-pane label="💊 药品管理" name="drug">
        <div class="tab-header">
          <div v-if="drugWarningCount > 0" class="alert-bar">
            <AlertTriangle :size="15" class="alert-icon" />
            <span>有 <strong>{{ drugWarningCount }}</strong> 种药品库存低于预警阈值，请及时采购</span>
          </div>
          <div class="tab-header-right">
            <el-button type="primary" @click="showAddDrug = true"><Plus class="btn-icon" />录入新药品</el-button>
          </div>
        </div>

        <!-- 内层 Tab：库存 / 日志 -->
        <el-tabs v-model="drugTab" class="inner-tabs">
          <el-tab-pane label="药品库存" name="stock">
            <div class="filter-section">
              <el-input v-model="drugKeyword" placeholder="搜通用名/商品名" clearable style="width:220px" @keyup.enter="loadDrugs">
                <template #prefix><Search class="input-icon" /></template>
              </el-input>
              <el-button type="primary" @click="loadDrugs">检索</el-button>
            </div>
            <el-table :data="drugs" stripe v-loading="drugLoading" empty-text="暂无药品记录">
              <el-table-column prop="drugCode"    label="药品编码"   width="120"><template #default="{row}"><span class="font-mono">{{row.drugCode}}</span></template></el-table-column>
              <el-table-column prop="genericName" label="通用名"     width="150"><template #default="{row}"><strong class="drug-name">{{row.genericName}}</strong></template></el-table-column>
              <el-table-column prop="tradeName"   label="商品名"     width="130"><template #default="{row}"><span class="muted-text">{{row.tradeName}}</span></template></el-table-column>
              <el-table-column label="规格/剂型" width="160"><template #default="{row}">{{row.spec}} · {{row.dosageForm}}</template></el-table-column>
              <el-table-column prop="manufacturer" label="生产厂家" show-overflow-tooltip />
              <el-table-column label="总库存" width="130" align="center">
                <template #default="{row}">
                  <span :class="['stock-qty', drugStockClass(row)]">{{ row._computedTotal ?? 0 }}</span>
                  <span class="stock-unit">{{ row.unit }}</span>
                  <div v-if="row._computedTotal > 0 && row._computedTotal <= row.alertQty" class="alert-tip">预警</div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right" align="center">
                <template #default="{row}"><el-button link type="primary" @click="viewDrugStock(row)">批次明细</el-button></template>
              </el-table-column>
            </el-table>
            <el-pagination v-model:current-page="drugPage" :page-size="10" :total="drugTotal"
              layout="prev, pager, next" @current-change="loadDrugs" style="margin-top:14px;justify-content:center" />
          </el-tab-pane>

          <el-tab-pane label="出入库日志" name="logs">
            <div class="filter-section">
              <el-input v-model="drugLogKeyword" placeholder="药品名称" clearable style="width:180px" @keyup.enter="loadDrugLogs" />
              <el-select v-model="drugLogDir" placeholder="出/入库" clearable style="width:110px" @change="loadDrugLogs">
                <el-option label="入库" :value="1" /><el-option label="出库" :value="2" />
              </el-select>
              <el-button @click="loadDrugLogs">查询</el-button>
            </div>
            <el-table :data="drugLogs" stripe size="small" v-loading="drugLogsLoading">
              <el-table-column prop="drugName"  label="药品名称" min-width="140" />
              <el-table-column prop="direction" label="类型" width="80" align="center">
                <template #default="{row}"><el-tag size="small" :type="row.direction===1?'success':'danger'">{{row.direction===1?'入库':'出库'}}</el-tag></template>
              </el-table-column>
              <el-table-column prop="quantity"  label="数量" width="80" align="center" />
              <el-table-column prop="remark"    label="备注/来源" min-width="200" show-overflow-tooltip />
              <el-table-column label="时间" width="150">
                <template #default="{row}">{{ fmtTime(row.opTime || row.changeTime) }}</template>
              </el-table-column>
            </el-table>
            <el-pagination v-model:current-page="drugLogPage" :page-size="20" :total="drugLogTotal"
              layout="total, prev, pager, next" small @current-change="loadDrugLogs"
              style="margin-top:12px;justify-content:flex-end" />
          </el-tab-pane>
        </el-tabs>
      </el-tab-pane>

      <!-- ════════════════════════════════════════
           疫苗管理
           ════════════════════════════════════════ -->
      <el-tab-pane label="💉 疫苗管理" name="vaccine">
        <div class="tab-header">
          <div v-if="vaccineWarningCount > 0" class="alert-bar">
            <AlertTriangle :size="15" class="alert-icon" />
            <span>有 <strong>{{ vaccineWarningCount }}</strong> 项疫苗库存不足或即将临期，<a class="alert-link" @click="loadVaccineAlerts">查看预警看板</a></span>
          </div>
          <div class="tab-header-right">
            <el-button type="warning" plain @click="loadVaccineAlerts"><AlertCircle class="btn-icon" />预警看板</el-button>
            <el-button type="primary" @click="showAddVaccine = true"><Plus class="btn-icon" />登记新疫苗</el-button>
          </div>
        </div>

        <div class="filter-section">
          <el-input v-model="vaccineKeyword" placeholder="搜索疫苗名称/批号" clearable style="width:240px" @keyup.enter="loadVaccines">
            <template #prefix><Search class="input-icon" /></template>
          </el-input>
          <el-button type="primary" @click="loadVaccines">查询库存</el-button>
        </div>

        <el-table :data="vaccines" stripe v-loading="vaccineLoading" empty-text="暂无疫苗记录">
          <el-table-column prop="vaccineCode" label="编码" width="120"><template #default="{row}"><span class="font-mono">{{row.vaccineCode}}</span></template></el-table-column>
          <el-table-column prop="vaccineName" label="疫苗名称" width="200"><template #default="{row}"><strong class="drug-name">{{row.vaccineName}}</strong></template></el-table-column>
          <el-table-column prop="manufacturer" label="生产厂家" width="150" show-overflow-tooltip />
          <el-table-column prop="batchNo" label="主用批号" width="120"><template #default="{row}"><span class="muted-text font-mono">{{row.batchNo||'--'}}</span></template></el-table-column>
          <el-table-column label="当前库存" width="120" align="center">
            <template #default="{row}">
              <span :class="['stock-qty', vaccineStockClass(row)]">{{row.quantity}}</span>
              <span class="stock-unit">支/剂</span>
            </template>
          </el-table-column>
          <el-table-column prop="expiryDate" label="近期有效期" width="130" align="center">
            <template #default="{row}"><span :class="{'text-danger': isExpiringSoon(row.expiryDate)}">{{row.expiryDate||'未关联'}}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{row}"><el-tag size="small" :type="row.status===1?'success':'danger'">{{row.status===1?'启用':'冻结'}}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{row}">
              <el-button link type="primary" @click="openVaccineAddStock(row)">入库</el-button>
              <el-button link type="primary" @click="viewVaccineLogs(row.id)">日志</el-button>
              <el-button link type="warning" @click="openVaccineEdit(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="vaccinePage" :page-size="20" :total="vaccineTotal"
          layout="prev, pager, next" @current-change="loadVaccines" style="margin-top:14px;justify-content:center" />
      </el-tab-pane>
    </el-tabs>

    <!-- ════ 药品批次明细抽屉 ════ -->
    <el-drawer v-model="showDrugStock" :title="`【${activeDrug?.genericName}】批次明细`" size="52%">
      <div style="padding:0 20px">
        <el-table :data="drugStocks" border stripe size="small">
          <el-table-column prop="batchNo"       label="批号"   width="160"><template #default="{row}"><span class="font-mono">{{row.batchNo}}</span></template></el-table-column>
          <el-table-column prop="quantity"      label="批次量" width="90"  align="center"><template #default="{row}"><strong>{{row.quantity}}</strong></template></el-table-column>
          <el-table-column prop="supplier"      label="供应商" />
          <el-table-column prop="purchasePrice" label="单价"   width="90"  align="right"><template #default="{row}">¥{{parseFloat(row.purchasePrice||0).toFixed(2)}}</template></el-table-column>
          <el-table-column prop="expireDate"    label="有效期" width="120" align="center"><template #default="{row}"><span :class="{'text-danger': isDrugExpired(row.expireDate)}">{{row.expireDate}}</span></template></el-table-column>
          <el-table-column prop="status" label="状态" width="70" align="center"><template #default="{row}"><el-tag size="small" :type="row.status===1?'success':'danger'">{{row.status===1?'上架':'失效'}}</el-tag></template></el-table-column>
        </el-table>
        <div style="margin-top:20px;text-align:right"><el-button @click="showDrugStock=false">关闭</el-button></div>
      </div>
    </el-drawer>

    <!-- ════ 新增药品弹窗 ════ -->
    <el-dialog v-model="showAddDrug" title="药品基准档案登记" width="580px">
      <el-form :model="drugForm" label-position="top">
        <div class="form-row">
          <el-form-item label="药品编码" style="flex:1"><el-input v-model="drugForm.drugCode" placeholder="如: XY-1002" /></el-form-item>
          <el-form-item label="生产厂家" style="flex:1"><el-input v-model="drugForm.manufacturer" placeholder="药厂全称" /></el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="通用名" style="flex:1"><el-input v-model="drugForm.genericName" placeholder="如: 对乙酰氨基酚" /></el-form-item>
          <el-form-item label="商品名" style="flex:1"><el-input v-model="drugForm.tradeName" placeholder="如: 泰诺林" /></el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="规格"><el-input v-model="drugForm.spec" placeholder="10mg*10片" /></el-form-item>
          <el-form-item label="剂型"><el-input v-model="drugForm.dosageForm" placeholder="口服片剂" /></el-form-item>
          <el-form-item label="单位"><el-input v-model="drugForm.unit" placeholder="盒" /></el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showAddDrug=false">取消</el-button>
        <el-button type="primary" @click="addDrug">归档并保存</el-button>
      </template>
    </el-dialog>

    <!-- ════ 疫苗预警弹窗 ════ -->
    <el-dialog v-model="showVaccineAlerts" title="疫苗预警看板" width="720px">
      <el-tabs v-model="vaccineAlertTab">
        <el-tab-pane label="库存短缺预警" name="stock">
          <el-table :data="vaccineStockAlerts" stripe border size="small">
            <el-table-column prop="vaccineName" label="疫苗名称" />
            <el-table-column prop="quantity" label="当前库存" width="100" align="center"><template #default="{row}"><strong class="stock-danger">{{row.quantity}}</strong></template></el-table-column>
            <el-table-column prop="alertQty" label="预警阈值" width="90" align="center" />
          </el-table>
          <el-empty v-if="!vaccineStockAlerts.length" description="无库存预警" :image-size="60" />
        </el-tab-pane>
        <el-tab-pane label="30日临期预警" name="expiry">
          <el-table :data="vaccineExpiryAlerts" stripe border size="small">
            <el-table-column prop="vaccineName" label="疫苗名称" />
            <el-table-column prop="batchNo" label="批号" width="120"><template #default="{row}"><span class="font-mono">{{row.batchNo}}</span></template></el-table-column>
            <el-table-column prop="expiryDate" label="有效期至" width="120" align="center"><template #default="{row}"><strong class="stock-danger">{{row.expiryDate}}</strong></template></el-table-column>
          </el-table>
          <el-empty v-if="!vaccineExpiryAlerts.length" description="无临期预警" :image-size="60" />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- ════ 新增疫苗弹窗 ════ -->
    <el-dialog v-model="showAddVaccine" title="登记新疫苗档案" width="580px">
      <el-form :model="vaccineForm" label-position="top">
        <div class="form-row">
          <el-form-item label="疫苗编码" style="flex:1"><el-input v-model="vaccineForm.vaccineCode" placeholder="如: VCX-09" /></el-form-item>
          <el-form-item label="生产厂家" style="flex:2"><el-input v-model="vaccineForm.manufacturer" placeholder="生产企业全称" /></el-form-item>
        </div>
        <el-form-item label="疫苗通用名称"><el-input v-model="vaccineForm.vaccineName" placeholder="如: 重组新型冠状病毒疫苗" /></el-form-item>
        <div class="form-row">
          <el-form-item label="批号"><el-input v-model="vaccineForm.batchNo" placeholder="批次钢印" /></el-form-item>
          <el-form-item label="有效期"><el-date-picker v-model="vaccineForm.expiryDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="初始数量(剂)"><el-input-number v-model="vaccineForm.quantity" :min="0" :controls="false" style="width:100%" /></el-form-item>
          <el-form-item label="预警阈值"><el-input-number v-model="vaccineForm.alertQty" :min="0" :controls="false" style="width:100%" /></el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showAddVaccine=false">取消</el-button>
        <el-button type="primary" @click="addVaccine">确认建档</el-button>
      </template>
    </el-dialog>

    <!-- ════ 疫苗入库弹窗 ════ -->
    <el-dialog v-model="showVaccineAddStock" title="新批次疫苗入库" width="420px">
      <el-form label-position="top">
        <el-form-item label="入库疫苗"><div style="font-weight:600">{{ vaccineAddStockTarget?.vaccineName }}</div></el-form-item>
        <div class="form-row">
          <el-form-item label="批号" style="flex:1"><el-input v-model="vaccineAddBatchNo" /></el-form-item>
          <el-form-item label="数量(剂)" style="flex:1"><el-input-number v-model="vaccineAddQty" :min="1" :controls="false" style="width:100%" /></el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showVaccineAddStock=false">取消</el-button>
        <el-button type="primary" @click="doVaccineAddStock">确认入库</el-button>
      </template>
    </el-dialog>

    <!-- ════ 疫苗日志抽屉 ════ -->
    <el-drawer v-model="showVaccineLogs" title="疫苗出入库日志" size="52%">
      <div style="padding:0 20px">
        <el-table :data="vaccineLogs" stripe border size="small">
          <el-table-column prop="opTime" label="时间" width="150"><template #default="{row}">{{ fmtTime(row.opTime) }}</template></el-table-column>
          <el-table-column prop="opType" label="类型" width="80" align="center">
            <template #default="{row}"><el-tag size="small" :type="row.opType===1?'success':'danger'">{{row.opType===1?'入库':'出库'}}</el-tag></template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column prop="balance" label="结余" width="80" align="center" />
          <el-table-column prop="batchNo" label="批号" width="110"><template #default="{row}"><span class="font-mono">{{row.batchNo}}</span></template></el-table-column>
          <el-table-column prop="operatorName" label="操作人" width="90" />
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        </el-table>
        <div style="margin-top:20px;text-align:right"><el-button @click="showVaccineLogs=false">关闭</el-button></div>
      </div>
    </el-drawer>

    <!-- ════ 疫苗编辑弹窗 ════ -->
    <el-dialog v-model="showVaccineEdit" title="疫苗属性维护" width="520px">
      <el-form :model="vaccineEditForm" label-position="top">
        <div class="form-row">
          <el-form-item label="疫苗编码" style="flex:1"><el-input v-model="vaccineEditForm.vaccineCode" /></el-form-item>
          <el-form-item label="生产厂家" style="flex:2"><el-input v-model="vaccineEditForm.manufacturer" /></el-form-item>
        </div>
        <el-form-item label="疫苗名称"><el-input v-model="vaccineEditForm.vaccineName" /></el-form-item>
        <div class="form-row">
          <el-form-item label="预警阈值" style="flex:1"><el-input-number v-model="vaccineEditForm.alertQty" :min="0" :controls="false" style="width:100%" /></el-form-item>
          <el-form-item label="状态" style="flex:1">
            <el-select v-model="vaccineEditForm.status" style="width:100%">
              <el-option label="启用" :value="1" /><el-option label="冻结" :value="0" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showVaccineEdit=false">取消</el-button>
        <el-button type="warning" @click="doVaccineEdit">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { Search, Plus, AlertTriangle, AlertCircle } from 'lucide-vue-next'

const mainTab = ref('drug')

// ══════════════════ 药品 ══════════════════
const drugTab        = ref('stock')
const drugs          = ref([])
const drugLoading    = ref(false)
const drugKeyword    = ref('')
const drugPage       = ref(1)
const drugTotal      = ref(0)
const drugStocks     = ref([])
const activeDrug     = ref(null)
const showDrugStock  = ref(false)
const showAddDrug    = ref(false)
const drugForm       = ref({ drugCode:'', genericName:'', tradeName:'', spec:'', dosageForm:'', unit:'', manufacturer:'' })
const drugLogs       = ref([])
const drugLogsLoading= ref(false)
const drugLogPage    = ref(1)
const drugLogTotal   = ref(0)
const drugLogKeyword = ref('')
const drugLogDir     = ref(null)

const drugWarningCount = computed(() => drugs.value.filter(d => d._computedTotal !== undefined && d._computedTotal <= d.alertQty).length)
const drugStockClass = (row) => {
  if (row._computedTotal <= row.alertQty) return 'stock-danger'
  if (row._computedTotal <= row.alertQty * 1.5) return 'stock-warning'
  return 'stock-safe'
}
const isDrugExpired = (d) => d ? new Date(d) < new Date() : false

async function loadDrugs() {
  drugLoading.value = true
  try {
    const { data } = await request.get('/admin/drug', { params: { page: drugPage.value, size: 10, keyword: drugKeyword.value } })
    const list = data.records || []
    await Promise.all(list.map(async d => {
      try { const { data: s } = await request.get(`/admin/drug/${d.id}/stock`); d._computedTotal = (s||[]).reduce((a,i)=>a+(i.quantity||0),0) }
      catch { d._computedTotal = 0 }
    }))
    drugs.value = list; drugTotal.value = data.total || 0
  } finally { drugLoading.value = false }
}

async function viewDrugStock(row) {
  activeDrug.value = row
  const { data } = await request.get(`/admin/drug/${row.id}/stock`)
  drugStocks.value = data || []
  showDrugStock.value = true
}

async function addDrug() {
  await request.post('/admin/drug', drugForm.value)
  ElMessage.success('药品档案添加成功')
  showAddDrug.value = false
  drugForm.value = { drugCode:'', genericName:'', tradeName:'', spec:'', dosageForm:'', unit:'', manufacturer:'' }
  loadDrugs()
}

async function loadDrugLogs() {
  drugLogsLoading.value = true
  try {
    const params = { page: drugLogPage.value, size: 20 }
    if (drugLogKeyword.value) params.keyword = drugLogKeyword.value
    if (drugLogDir.value !== null) params.changeType = drugLogDir.value
    const { data } = await request.get('/admin/drug/stock-logs', { params })
    drugLogs.value = data?.records || []; drugLogTotal.value = data?.total || 0
  } finally { drugLogsLoading.value = false }
}

// ══════════════════ 疫苗 ══════════════════
const vaccines            = ref([])
const vaccineLoading      = ref(false)
const vaccineKeyword      = ref('')
const vaccinePage         = ref(1)
const vaccineTotal        = ref(0)
const showVaccineAlerts   = ref(false)
const vaccineAlertTab     = ref('stock')
const vaccineStockAlerts  = ref([])
const vaccineExpiryAlerts = ref([])
const showAddVaccine      = ref(false)
const vaccineForm         = ref({ vaccineName:'', vaccineCode:'', manufacturer:'', batchNo:'', quantity:0, alertQty:10, expiryDate:'' })
const showVaccineAddStock = ref(false)
const vaccineAddStockTarget = ref(null)
const vaccineAddQty       = ref(1)
const vaccineAddBatchNo   = ref('')
const showVaccineLogs     = ref(false)
const vaccineLogs         = ref([])
const showVaccineEdit     = ref(false)
const vaccineEditForm     = ref({})

const vaccineWarningCount = computed(() => vaccines.value.filter(v => v.quantity <= v.alertQty || isExpiringSoon(v.expiryDate)).length)
const vaccineStockClass = (row) => {
  if (row.quantity <= row.alertQty) return 'stock-danger'
  if (row.quantity <= row.alertQty * 1.5) return 'stock-warning'
  return 'stock-safe'
}
const isExpiringSoon = (d) => { if (!d) return false; const diff = (new Date(d)-new Date())/86400000; return diff <= 30 && diff > 0 }

async function loadVaccines() {
  vaccineLoading.value = true
  try {
    const { data } = await request.get('/admin/vaccine-stock', { params: { page: vaccinePage.value, size: 20, keyword: vaccineKeyword.value } })
    vaccines.value = data.records || []; vaccineTotal.value = data.total || 0
  } finally { vaccineLoading.value = false }
}

async function loadVaccineAlerts() {
  const [s, e] = await Promise.all([request.get('/admin/vaccine-stock/stock-alert'), request.get('/admin/vaccine-stock/expiry-alert')])
  vaccineStockAlerts.value = s.data || []; vaccineExpiryAlerts.value = e.data || []
  vaccineAlertTab.value = 'stock'; showVaccineAlerts.value = true
}

async function addVaccine() {
  await request.post('/admin/vaccine-stock', vaccineForm.value)
  ElMessage.success('疫苗档案创建成功')
  showAddVaccine.value = false; loadVaccines()
}

function openVaccineAddStock(row) {
  vaccineAddStockTarget.value = row; vaccineAddQty.value = 1; vaccineAddBatchNo.value = row.batchNo || ''; showVaccineAddStock.value = true
}

async function doVaccineAddStock() {
  await request.post(`/admin/vaccine-stock/${vaccineAddStockTarget.value.id}/add-stock`, { quantity: vaccineAddQty.value, batchNo: vaccineAddBatchNo.value })
  ElMessage.success('入库登记成功'); showVaccineAddStock.value = false; loadVaccines()
}

async function viewVaccineLogs(id) {
  const { data } = await request.get('/admin/vaccine-stock/logs', { params: { vaccineId: id, page: 1, size: 50 } })
  vaccineLogs.value = data.records || []; showVaccineLogs.value = true
}

function openVaccineEdit(row) { vaccineEditForm.value = { ...row }; showVaccineEdit.value = true }

async function doVaccineEdit() {
  await request.put(`/admin/vaccine-stock/${vaccineEditForm.value.id}`, vaccineEditForm.value)
  ElMessage.success('疫苗配置已更新'); showVaccineEdit.value = false; loadVaccines()
}

// ══════ 工具 ══════
const fmtTime = (t) => t ? new Date(t).toLocaleString('zh-CN', { month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit' }) : '—'

onMounted(() => { loadDrugs(); loadDrugLogs(); loadVaccines() })
</script>

<style scoped>
.inventory-page { padding: 0; }
.main-tabs { }
.inner-tabs { margin-top: 0; }
.inner-tabs :deep(.el-tabs__header) { margin-bottom: 12px; }

.tab-header { display: flex; align-items: center; justify-content: space-between; padding: 10px 0 14px; flex-wrap: wrap; gap: 8px; }
.tab-header-right { display: flex; gap: 8px; }

.alert-bar  { display:flex; align-items:center; gap:8px; padding:8px 14px; background:var(--bg-warn,#fffbeb); border-radius:8px; color:var(--text-warning,#b45309); font-size:13px; }
.alert-icon { flex-shrink:0; }
.alert-link { color:var(--text-warning); text-decoration:underline; cursor:pointer; font-weight:600; }

.filter-section { display:flex; gap:8px; align-items:center; margin-bottom:12px; flex-wrap:wrap; }

.btn-icon, .input-icon { width:15px; height:15px; margin-right:5px; }
.input-icon { margin-right:0; color:var(--muted); }

.drug-name   { color: var(--text-strong); }
.muted-text  { color: var(--muted); }
.font-mono   { font-family: ui-monospace, monospace; font-size:12px; }
.stock-unit  { margin-left:3px; font-size:11px; color:var(--muted); }
.alert-tip   { font-size:10px; color:var(--text-warning); background:var(--surface-warm,#fdf6ec); border-radius:8px; padding:1px 5px; display:inline-block; margin-top:2px; }

.stock-qty     { font-size:16px; font-weight:700; }
.stock-safe    { color: #10b981; }
.stock-warning { color: #f59e0b; }
.stock-danger  { color: #ef4444; }
.text-danger   { color: #ef4444; }

.form-row { display: flex; gap: 14px; }
.form-row .el-form-item { flex: 1; }
.form-row :deep(.el-input-number), .form-row :deep(.el-date-editor) { width: 100%; }
</style>
