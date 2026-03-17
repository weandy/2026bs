<template>
  <el-select v-model="selectedValue" filterable remote :remote-method="searchDrug" :loading="loading"
    placeholder="输入药品名称检索..." clearable style="width: 100%" @change="handleChange">
    <el-option v-for="item in options" :key="item.id" :label="item.genericName" :value="item.genericName">
      <div class="drug-option">
        <span class="drug-name">{{ item.genericName }}</span>
        <span class="drug-spec">{{ item.spec }}</span>
        <span class="drug-form">{{ item.dosageForm }}</span>
      </div>
    </el-option>
  </el-select>
</template>
<script setup>
import { ref, watch } from 'vue'
import request from '@/utils/request'
const props = defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue', 'select'])
const selectedValue = ref(props.modelValue)
const options = ref([])
const loading = ref(false)
watch(() => props.modelValue, v => { selectedValue.value = v })
async function searchDrug(keyword) {
  if (!keyword || keyword.length < 1) { options.value = []; return }
  loading.value = true
  try {
    const res = await request.get('/admin/drug', { params: { page: 1, size: 20, keyword } })
    options.value = res.data?.records || []
  } catch { options.value = [] }
  finally { loading.value = false }
}
function handleChange(val) {
  emit('update:modelValue', val)
  const selected = options.value.find(d => d.genericName === val)
  if (selected) emit('select', selected)
}
</script>
<style scoped>
.drug-option { display: flex; align-items: center; gap: 8px; }
.drug-name { font-weight: 600; font-size: 13px; }
.drug-spec { font-size: 12px; color: var(--muted); }
.drug-form { font-size: 11px; color: var(--muted); background: var(--surface-soft); padding: 1px 6px; border-radius: 4px; }
</style>
