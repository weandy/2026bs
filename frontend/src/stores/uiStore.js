import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

/**
 * UI 全局状态
 * - elderMode: 敬老版开关（同步到 body.elder-mode class + localStorage）
 * - headerHeight: 当前 layout header 高度（px），用于动态计算 --header-h CSS 变量
 */
export const useUiStore = defineStore('ui', () => {
  // ── 从 localStorage 恢复状态 ──
  const elderMode = ref(localStorage.getItem('elderMode') === 'on')

  // ── 同步 body class + localStorage ──
  function applyElderMode(val) {
    if (val) {
      document.body.classList.add('elder-mode')
      localStorage.setItem('elderMode', 'on')
    } else {
      document.body.classList.remove('elder-mode')
      localStorage.setItem('elderMode', 'off')
    }
  }

  // 立刻同步初始值
  applyElderMode(elderMode.value)

  // 监听变化
  watch(elderMode, applyElderMode)

  function toggleElder() {
    elderMode.value = !elderMode.value
  }

  // ── Header 高度（由 Layout 组件通过 ref 注入） ──
  const headerHeight = ref(56)

  function setHeaderHeight(h) {
    headerHeight.value = h
    // 同步到 CSS 变量，驱动 workspace-h
    document.documentElement.style.setProperty('--header-h', `${h}px`)
  }

  return {
    elderMode,
    toggleElder,
    headerHeight,
    setHeaderHeight,
  }
})
