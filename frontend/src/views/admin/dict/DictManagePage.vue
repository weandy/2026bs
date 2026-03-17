<template>
  <div class="dict-page">
    <div class="page-header">
      <div class="page-header-main">
        <h2>基础数据字典</h2>
        <p>中央系统静态枚举值与树形参数维护</p>
      </div>
      <div class="page-header-actions">
        <el-button type="primary" @click="openAdd">
          <Plus class="btn-icon"/> 新增字典项
        </el-button>
      </div>
    </div>

    <div class="panel">
      <!-- 搜索过滤器 -->
      <div class="filter-section">
        <el-form :inline="true" class="compact-form">
          <el-form-item label="所属大类" style="margin-bottom:0">
            <el-select v-model="selectedType" placeholder="全部类型" clearable @change="loadList" style="width: 200px">
              <el-option label="诊断与疾病码 (disease)" value="disease" />
              <el-option label="药品品类码 (drug)" value="drug" />
              <el-option label="疫苗种类码 (vaccine)" value="vaccine" />
              <el-option label="健康检测项 (exam)" value="exam" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <!-- 字典树形表格 -->
      <el-table 
        :data="dictList" 
        stripe 
        v-loading="loading" 
        row-key="id" 
        default-expand-all
        class="custom-table"
        :empty-text="'暂无对应的字典数据'"
      >
        <el-table-column prop="name" label="字典名称(名称/标识)" width="260">
          <template #default="{ row }">
            <div class="dict-name-cell">
              <span class="dict-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="字典编码(Code)" width="180">
          <template #default="{ row }"><span class="font-mono">{{ row.code }}</span></template>
        </el-table-column>
        <el-table-column prop="type" label="归属大类" width="140">
          <template #default="{ row }">
            <span class="category-tag">{{ typeLabel(row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="parentCode" label="上级挂载节点" width="160">
           <template #default="{ row }">
             <span v-if="row.parentCode" class="muted-text font-mono">{{ row.parentCode }}</span>
             <span v-else class="muted-text">—</span>
           </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序权重" width="100" align="center">
           <template #default="{ row }"><span class="sort-badge">{{ row.sortOrder }}</span></template>
        </el-table-column>
        <el-table-column prop="status" label="运行状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', row.status === 1 ? 'done' : 'cancelled']">{{ row.status === 1 ? '启用中' : '已废弃' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="节点操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">修 改</el-button>
            <span class="action-divider">|</span>
            <el-button link type="danger" @click="doDelete(row.id)">移 除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑字典项弹窗 -->
    <el-dialog v-model="showDlg" :title="editMode ? '更正字典节点配置' : '下发新字典节点'" width="540px" class="custom-dialog">
      <div class="dialog-content">
        <el-form :model="form" :rules="dictRules" ref="dictFormRef" label-position="top">
          <div class="form-row">
            <el-form-item label="归属大类(Type)" prop="type" style="flex:1">
              <el-select v-model="form.type" :disabled="editMode" style="width:100%">
                <el-option label="疾病码 (disease)" value="disease" />
                <el-option label="药品码 (drug)" value="drug" />
                <el-option label="疫苗码 (vaccine)" value="vaccine" />
                <el-option label="检验项 (exam)" value="exam" />
              </el-select>
            </el-form-item>
            <el-form-item label="唯一编码(Code)" prop="code" style="flex:1">
              <el-input v-model="form.code" placeholder="如: G10.1" />
            </el-form-item>
          </div>
          
          <el-form-item label="字典显示名称(Name)" prop="name">
            <el-input v-model="form.name" placeholder="如: 亨廷顿病" />
          </el-form-item>
          
          <div class="form-row">
            <el-form-item label="上级挂载点(ParentCode)" style="flex:2">
              <el-input v-model="form.parentCode" placeholder="顶层节点请留空" />
            </el-form-item>
            <el-form-item label="显示排序" style="flex:1">
              <el-input-number v-model="form.sortOrder" :min="0" :controls="false" style="width:100%" />
            </el-form-item>
          </div>

          <el-form-item label="启停状态">
            <el-radio-group v-model="form.status" style="width:100%;">
              <el-radio-button :value="1">正常启用</el-radio-button>
              <el-radio-button :value="0">冻结废弃</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDlg = false">放弃编辑</el-button>
          <el-button type="primary" @click="doSave">确认并写入库</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { Plus } from 'lucide-vue-next'

const dictList = ref([])
const loading = ref(false)
const selectedType = ref('')
const showDlg = ref(false)
const editMode = ref(false)
const form = ref({ type: 'disease', code: '', name: '', parentCode: '', sortOrder: 0, status: 1 })
const dictFormRef = ref()
const dictRules = {
  type: [{ required: true, message: '请选择归属大类', trigger: 'change' }],
  code: [{ required: true, message: '请输入字典编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入字典名称', trigger: 'blur' }]
}

function typeLabel(t) { 
  return { disease: '诊断类', drug: '药品类', vaccine: '疫苗类', exam: '检验类' }[t] || t 
}

async function loadList() {
  loading.value = true
  try {
    const params = {}
    if (selectedType.value) params.type = selectedType.value
    const { data } = await request.get('/admin/dict/all', { params })
    dictList.value = data || []
  } catch (e) {
    console.warn(e)
  } finally { 
    loading.value = false 
  }
}

function openAdd() {
  editMode.value = false
  form.value = { type: selectedType.value || 'disease', code: '', name: '', parentCode: '', sortOrder: 0, status: 1 }
  showDlg.value = true
}

function openEdit(row) {
  editMode.value = true
  form.value = { ...row }
  showDlg.value = true
}

async function doSave() {
  const valid = await dictFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (editMode.value) {
      await request.put(`/admin/dict/${form.value.id}`, form.value)
    } else {
      await request.post('/admin/dict', form.value)
    }
    ElMessage.success('字典配置存储成功')
    showDlg.value = false
    loadList()
  } catch (e) { /* handled */ }
}

async function doDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除该字典节点及其子节点？此操作不可撤销。', '确认删除', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
    await request.delete(`/admin/dict/${id}`)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(loadList)
</script>

<style scoped>
.dict-page { padding: 20px; max-width: 1200px; margin: 0 auto; }

.btn-icon { width: 16px; height: 16px; margin-right: 6px; }

/* 过滤搜索条 */
.filter-section {
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--border);
}
.compact-form { display: flex; align-items: flex-end; gap: 12px; }

/* 表格公用定义 */
.custom-table { border-radius: 8px; overflow: hidden; }
.font-mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; letter-spacing: 0.5px; }
.muted-text { color: var(--muted); }
.action-divider { color: var(--border); margin: 0 6px; font-size: 12px; }

.dict-name-cell { display: flex; align-items: center; gap: 8px; }
.dict-name { font-weight: 600; color: var(--text-strong); }

.category-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(47, 107, 87, 0.08); /* 主色低透明度 */
  color: var(--primary);
  font-size: 12px;
}
.sort-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--surface-soft);
  color: var(--muted);
  min-width: 24px;
  height: 24px;
  border-radius: 12px;
  font-size: 12px;
  font-family: monospace;
}

/* 弹框内容修饰 */
.dialog-content { padding: 10px 0; }
.form-row { display: flex; gap: 16px; align-items:flex-end; margin-bottom: 18px; }

/* 覆盖原生单选按钮样式 */
:deep(.el-radio-button__inner) {
  padding: 10px 30px;
}

/* 响应式 */
@media (max-width: 768px) {
  .dict-page { padding: 12px; }
}
</style>
