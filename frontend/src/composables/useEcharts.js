import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

/**
 * useEcharts — 安全 ECharts 生命周期管理
 *
 * @param {Ref<HTMLElement>} containerRef - 挂载容器的 template ref
 * @param {Function} getOption - 返回 ECharts option 的函数（可以是异步的）
 * @param {Object} opts - echarts.init 参数（可选）
 *
 * 用法：
 *   const chartRef = ref(null)
 *   const { setOption, resize } = useEcharts(chartRef, () => ({ ... }))
 */
export function useEcharts(containerRef, getOption, opts = {}) {
  let chartInstance = null

  async function init() {
    if (!containerRef.value) return
    chartInstance = echarts.init(containerRef.value, null, opts)
    const option = await getOption()
    if (option) chartInstance.setOption(option)
  }

  function setOption(option) {
    if (chartInstance) chartInstance.setOption(option)
  }

  function resize() {
    if (chartInstance) chartInstance.resize()
  }

  // 响应父容器大小变化
  let resizeObserver = null

  onMounted(async () => {
    await init()
    if (containerRef.value) {
      resizeObserver = new ResizeObserver(() => resize())
      resizeObserver.observe(containerRef.value)
    }
  })

  onUnmounted(() => {
    if (resizeObserver) resizeObserver.disconnect()
    if (chartInstance) {
      chartInstance.dispose()
      chartInstance = null
    }
  })

  return { setOption, resize }
}
