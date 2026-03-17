<template>
  <div class="not-found" style="display:flex;flex-direction:column;align-items:center;justify-content:center;height:80vh;">
    <h1 style="font-size:72px;color:var(--border);">404</h1>
    <p style="color:var(--text-secondary);">页面不存在</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>
<script setup></script>
