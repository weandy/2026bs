/**
 * chartTheme.js — 统一 ECharts 绿系主题配置
 * 在所有图表初始化时使用，保证视觉一致性
 */

export const PRIMARY   = '#2F6B57'
export const MEDICAL   = '#4D6F99'
export const WARN_CLR  = '#E67E22'
export const LABEL_CLR = '#63756D'
export const GRID_LINE = '#EEF2EF'

/** 通用 axis 基础配置 */
export function axisBase(overrides = {}) {
  return {
    axisLine:    { lineStyle: { color: '#E4E7ED' } },
    axisTick:    { show: false },
    axisLabel:   { color: LABEL_CLR, fontSize: 11 },
    splitLine:   { lineStyle: { color: GRID_LINE } },
    ...overrides,
  }
}

/** 通用 tooltip */
export const tooltipBase = {
  backgroundColor: '#fff',
  borderColor: '#E4E7ED',
  textStyle: { color: '#23312D', fontSize: 12 },
}

/** 通用 grid */
export const gridBase = { top: 14, right: 14, bottom: 30, left: 42 }

/**
 * 折线面积图 option 工厂
 * @param { xData: string[], series: number[], title?: string } opts
 */
export function lineAreaOption({ xData, series, color = PRIMARY }) {
  return {
    grid: gridBase,
    tooltip: { trigger: 'axis', ...tooltipBase },
    xAxis: { type: 'category', data: xData, ...axisBase() },
    yAxis: { type: 'value', ...axisBase() },
    series: [{
      data: series,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 5,
      lineStyle:  { color, width: 2.5 },
      itemStyle:  { color },
      areaStyle:  { color: color + '14' }, // 8% 透明
    }],
  }
}

/**
 * 横向条形图 option 工厂（药品 TOP5 等）
 * @param { yData: string[], series: number[], color? } opts
 */
export function barHorizOption({ yData, series, color = PRIMARY }) {
  return {
    grid: { top: 4, right: 20, bottom: 4, left: 120 },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, ...tooltipBase },
    xAxis: {
      type: 'value',
      axisLabel: { color: LABEL_CLR, fontSize: 11 },
      splitLine:  { lineStyle: { color: GRID_LINE } },
      axisLine:   { show: false },
    },
    yAxis: {
      type: 'category',
      data: yData,
      axisLabel: { color: LABEL_CLR, fontSize: 11 },
      axisLine:  { show: false },
      axisTick:  { show: false },
    },
    series: [{
      type: 'bar',
      data: series,
      barMaxWidth: 14,
      itemStyle: { color, borderRadius: [0, 4, 4, 0] },
    }],
  }
}

/**
 * 甜甜圈饼图 option 工厂
 * @param { data: [{name, value, color?}][] } opts
 */
export function donutOption({ data }) {
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      ...tooltipBase,
    },
    legend: {
      bottom: 0,
      textStyle: { color: LABEL_CLR, fontSize: 11 },
      itemGap: 16,
    },
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '44%'],
      label: { show: false },
      data: data.map((d, i) => ({
        name:      d.name,
        value:     d.value,
        itemStyle: { color: d.color || [PRIMARY, MEDICAL, '#7D3C98', WARN_CLR][i % 4] },
      })),
    }],
  }
}

/**
 * 多折线对比图（随访趋势用）
 * @param { xData, seriesList: [{name, data, color}][] }
 */
export function multiLineOption({ xData, seriesList }) {
  return {
    grid: gridBase,
    tooltip: { trigger: 'axis', ...tooltipBase },
    legend: {
      bottom: 0,
      textStyle: { color: LABEL_CLR, fontSize: 11 },
    },
    xAxis: { type: 'category', data: xData, ...axisBase() },
    yAxis: { type: 'value', ...axisBase() },
    series: seriesList.map(s => ({
      name:      s.name,
      type:      'line',
      data:      s.data,
      smooth:    true,
      symbol:    'circle',
      symbolSize: 5,
      lineStyle: { color: s.color, width: 2 },
      itemStyle: { color: s.color },
    })),
  }
}
