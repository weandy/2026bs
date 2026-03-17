<template>
  <div class="article-page">
    <div class="page-header"><h2>健康资讯</h2></div>
    <div class="category-tabs">
      <button v-for="c in categories" :key="c.value" :class="{ active: activeCat === c.value }"
        @click="activeCat = c.value; loadArticles()">{{ c.label }}</button>
    </div>
    <div v-for="a in articles" :key="a.id" class="article-card" @click="viewArticle(a)">
      <strong>{{ a.title }}</strong>
      <div class="article-meta">
        <span>{{ a.authorName || '社区卫生中心' }}</span>
        <span>{{ formatDate(a.publishedAt) }}</span>
        <span>{{ a.viewCount || 0 }} 阅读</span>
      </div>
    </div>
    <div v-if="articles.length === 0" class="empty-tip">暂无资讯</div>
    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetail" :title="detailArticle?.title" width="600px">
      <div v-if="detailArticle" class="article-content" v-html="detailArticle.content"></div>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
const categories = [
  { value: '', label: '全部' }, { value: 'chronic', label: '慢病' },
  { value: 'diet', label: '饮食' }, { value: 'exercise', label: '运动' },
  { value: 'vaccine', label: '疫苗' }
]
const activeCat = ref('')
const articles = ref([])
const showDetail = ref(false)
const detailArticle = ref(null)
function formatDate(d) { return d ? String(d).substring(0, 10) : '' }
async function loadArticles() {
  try {
    const params = { page: 1, size: 20 }
    if (activeCat.value) params.category = activeCat.value
    const res = await request.get('/resident/article', { params })
    articles.value = res.data?.records || []
  } catch { articles.value = [] }
}
async function viewArticle(a) {
  try {
    const res = await request.get(`/resident/article/${a.id}`)
    detailArticle.value = res.data
    showDetail.value = true
  } catch { /* silent */ }
}
onMounted(() => loadArticles())
</script>
<style scoped>
.article-page { padding: 16px; max-width: 500px; margin: 0 auto; }
.category-tabs { display: flex; gap: 6px; margin-bottom: 14px; flex-wrap: wrap; }
.category-tabs button { padding: 6px 14px; border: 1px solid var(--border); border-radius: 20px; background: var(--surface); font-size: 13px; cursor: pointer; font-family: var(--font-sans); }
.category-tabs button.active { background: var(--primary); color: #fff; border-color: var(--primary); }
.article-card { background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 14px 16px; margin-bottom: 10px; cursor: pointer; transition: transform 0.15s; }
.article-card:hover { transform: translateY(-1px); }
.article-card strong { font-size: 15px; display: block; margin-bottom: 6px; }
.article-meta { font-size: 12px; color: var(--muted); display: flex; gap: 12px; }
.article-content { line-height: 1.8; font-size: 14px; }
.empty-tip { text-align: center; padding: 40px; color: var(--muted); }
</style>
