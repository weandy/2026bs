<template>
  <div class="performance-page">
    <el-table :data="records" stripe border v-loading="loading">
      <el-table-column prop="staffName" label="医生" width="100" />
      <el-table-column prop="deptName" label="科室" width="120" />
      <el-table-column prop="visitCount" label="接诊量" width="80" sortable />
      <el-table-column prop="prescCount" label="处方量" width="80" sortable />
      <el-table-column prop="followUpCount" label="随访量" width="80" sortable />
      <el-table-column prop="avgScore" label="评分" width="80"><template #default="{row}">{{ row.avgScore ? Number(row.avgScore).toFixed(1) : '--' }}</template></el-table-column>
    </el-table>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
const records = ref([]), loading = ref(false)
onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/report/staff-performance')
    records.value = res.data || []
  } catch {} finally { loading.value = false }
})
</script>
<style scoped>.performance-page { padding: 20px; }</style>
