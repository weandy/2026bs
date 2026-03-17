<template>
  <el-select
    v-model="selectedValue"
    filterable
    remote
    :remote-method="searchIcd"
    :loading="loading"
    placeholder="输入疾病名称或 ICD 编码检索..."
    clearable
    style="width: 100%"
    @change="handleChange"
    :data-testid="dataTestid"
  >
    <el-option
      v-for="item in options"
      :key="item.code"
      :label="`${item.code} ${item.nameCn}`"
      :value="`${item.code} ${item.nameCn}`"
    >
      <div class="icd-option">
        <span class="icd-code">{{ item.code }}</span>
        <span class="icd-name">{{ item.nameCn }}</span>
        <span class="icd-category">{{ item.category }}</span>
      </div>
    </el-option>
  </el-select>
</template>

<script setup>
import { ref, watch } from 'vue'
import request from '@/utils/request'

const props = defineProps({
  modelValue: { type: String, default: '' },
  dataTestid: { type: String, default: 'icd-search' }
})
const emit = defineEmits(['update:modelValue'])

const selectedValue = ref(props.modelValue)
const options = ref([])
const loading = ref(false)

watch(() => props.modelValue, (v) => { selectedValue.value = v })

async function searchIcd(keyword) {
  if (!keyword || keyword.length < 1) { options.value = []; return }
  loading.value = true
  try {
    const res = await request.get('/admin/icd/search', { params: { keyword } })
    options.value = res.data || []
  } catch { options.value = [] }
  finally { loading.value = false }
}

function handleChange(val) {
  emit('update:modelValue', val)
}
</script>

<style scoped>
.icd-option { display: flex; align-items: center; gap: 8px; }
.icd-code { font-weight: 600; color: var(--primary); font-size: 13px; min-width: 50px; }
.icd-name { flex: 1; font-size: 13px; }
.icd-category { font-size: 11px; color: var(--muted); background: var(--surface-soft); padding: 2px 6px; border-radius: 4px; }
</style>
