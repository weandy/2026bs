<template>
  <div class="family-page">
    <div class="page-header">
      <h2>家庭成员</h2>
      <button class="add-btn" @click="showForm = true">添加成员</button>
    </div>

    <div v-for="m in members" :key="m.id" class="member-card">
      <div class="member-info">
        <strong>{{ m.memberName }}</strong>
        <span class="relation-tag">{{ relationLabel(m.relation) }}</span>
      </div>
      <div class="member-meta">
        <span v-if="m.memberPhone">{{ m.memberPhone }}</span>
        <span v-if="m.idCard">{{ maskIdCard(m.idCard) }}</span>
      </div>
      <div class="member-actions">
        <button class="action-link" @click="editMember(m)">编辑</button>
        <button class="action-link danger" @click="deleteMember(m.id)">删除</button>
      </div>
    </div>

    <div v-if="members.length === 0" class="empty-tip">
      暂无家庭成员，添加后可代为预约挂号
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog v-model="showForm" :title="editId ? '编辑成员' : '添加成员'" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="form.memberName" /></el-form-item>
        <el-form-item label="关系">
          <el-select v-model="form.relation" style="width:100%">
            <el-option label="配偶" value="spouse" />
            <el-option label="子女" value="child" />
            <el-option label="父母" value="parent" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.memberPhone" /></el-form-item>
        <el-form-item label="身份证号"><el-input v-model="form.idCard" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForm = false">取消</el-button>
        <el-button type="primary" @click="submitForm">{{ editId ? '保存' : '添加' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const members = ref([])
const showForm = ref(false)
const editId = ref(null)
const form = reactive({ memberName: '', relation: 'spouse', memberPhone: '', idCard: '' })

function relationLabel(r) {
  return { spouse: '配偶', child: '子女', parent: '父母', other: '其他' }[r] || r
}

function maskIdCard(card) {
  if (!card || card.length < 8) return card
  return card.substring(0, 4) + '****' + card.substring(card.length - 4)
}

async function loadMembers() {
  try {
    const res = await request.get('/resident/family')
    members.value = res.data || []
  } catch { members.value = [] }
}

function editMember(m) {
  editId.value = m.id
  Object.assign(form, { memberName: m.memberName, relation: m.relation, memberPhone: m.memberPhone, idCard: m.idCard })
  showForm.value = true
}

async function submitForm() {
  try {
    if (editId.value) {
      await request.put(`/resident/family/${editId.value}`, form)
      ElMessage.success('已更新')
    } else {
      await request.post('/resident/family', form)
      ElMessage.success('已添加')
    }
    showForm.value = false
    editId.value = null
    Object.assign(form, { memberName: '', relation: 'spouse', memberPhone: '', idCard: '' })
    loadMembers()
  } catch (e) { ElMessage.error('操作失败') }
}

async function deleteMember(id) {
  await ElMessageBox.confirm('确定删除该成员吗？', '提示', { type: 'warning' })
  try {
    await request.delete(`/resident/family/${id}`)
    ElMessage.success('已删除')
    loadMembers()
  } catch { ElMessage.error('删除失败') }
}

onMounted(() => loadMembers())
</script>

<style scoped>
.family-page { padding: 16px; max-width: 500px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.add-btn {
  padding: 8px 16px; border: none; border-radius: 8px;
  background: var(--primary); color: #fff; font-size: 13px; font-weight: 600;
  cursor: pointer; font-family: var(--font-sans);
}

.member-card {
  background: var(--surface); border: 1px solid var(--border); border-radius: 14px;
  padding: 14px 16px; margin-bottom: 10px;
}
.member-info { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.member-info strong { font-size: 15px; }
.relation-tag {
  font-size: 11px; padding: 2px 8px; border-radius: 4px;
  background: rgba(47,107,87,0.1); color: var(--primary);
}
.member-meta { font-size: 12px; color: var(--muted); margin-bottom: 8px; display: flex; gap: 12px; }
.member-actions { display: flex; gap: 12px; }
.action-link {
  background: none; border: none; color: var(--primary); font-size: 13px;
  cursor: pointer; font-family: var(--font-sans);
}
.action-link.danger { color: var(--danger); }
.empty-tip { text-align: center; padding: 40px; color: var(--muted); font-size: 13px; }
</style>
