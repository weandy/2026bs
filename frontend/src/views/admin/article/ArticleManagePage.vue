<template>
  <div class="article-manage"><div class="toolbar-bar"><el-button type="primary" @click="openForm()">新建文章</el-button></div>
    <el-table :data="records" stripe border v-loading="loading">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="category" label="分类" width="80">
        <template #default="{row}">{{ {chronic:'慢病',diet:'饮食',exercise:'运动',vaccine:'疫苗',general:'综合'}[row.category] || row.category }}</template>
      </el-table-column>
      <el-table-column prop="viewCount" label="阅读量" width="80" />
      <el-table-column prop="isPublished" label="状态" width="80">
        <template #default="{row}"><el-tag size="small" :type="row.isPublished ? 'success' : 'info'">{{ row.isPublished ? '已发布' : '草稿' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{row}">
          <el-button size="small" text @click="openForm(row)">编辑</el-button>
          <el-button size="small" text :type="row.isPublished ? 'warning' : 'success'" @click="togglePublish(row)">{{ row.isPublished ? '下架' : '发布' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="showForm" :title="editId ? '编辑文章' : '新建文章'" width="700px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width:100%">
            <el-option label="慢病" value="chronic" /><el-option label="饮食" value="diet" /><el-option label="运动" value="exercise" />
            <el-option label="疫苗" value="vaccine" /><el-option label="综合" value="general" />
          </el-select>
        </el-form-item>
        <el-form-item label="作者"><el-input v-model="form.authorName" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="10" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showForm=false">取消</el-button><el-button type="primary" @click="submitForm">{{ editId ? '保存' : '创建' }}</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
const records = ref([]), loading = ref(false), showForm = ref(false), editId = ref(null)
const form = reactive({ title: '', category: 'general', authorName: '', content: '' })
async function load() { loading.value = true; try { const r = await request.get('/admin/article'); records.value = r.data?.records || [] } catch {} finally { loading.value = false } }
function openForm(row) {
  if (row) { editId.value = row.id; Object.assign(form, { title: row.title, category: row.category, authorName: row.authorName, content: row.content }) }
  else { editId.value = null; Object.assign(form, { title: '', category: 'general', authorName: '', content: '' }) }
  showForm.value = true
}
async function submitForm() {
  try {
    if (editId.value) await request.put(`/admin/article/${editId.value}`, form)
    else await request.post('/admin/article', form)
    ElMessage.success('操作成功'); showForm.value = false; load()
  } catch { ElMessage.error('失败') }
}
async function togglePublish(row) {
  try { await request.put(`/admin/article/${row.id}/publish`, null, { params: { publish: row.isPublished ? 0 : 1 } }); ElMessage.success('操作成功'); load() } catch {}
}
onMounted(() => load())
</script>
<style scoped>.article-manage { padding: 20px; } .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }</style>
