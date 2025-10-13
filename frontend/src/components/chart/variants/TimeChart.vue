<template>
  <BaseChart :option="option" :height="height" :theme="theme" :loading="loading" />
</template>

<script setup>
import { computed } from 'vue'
import BaseChart from '../BaseChart.vue'

/**
 * @component TimeSeriesChart
 * @description
 * 시계열 데이터를 선(line) 차트로 표현하는 컴포넌트  
 * - ECharts 기반 BaseChart 래퍼 사용  
 * - data = [{ t: Date|number|string, y: number }] 구조  
 * - 색상, 에어리어(면적), 스무딩, y축 스케일, 그리드, 툴팁 등 옵션 지원  
 * - locale/decimals로 숫자 포맷 지정 가능
 *
 * @example
 * <!-- 기본 사용 -->
 * <TimeSeriesChart :data="[{t:'2025-09-15', y:120}, {t:'2025-09-16', y:140}]" name="매출" />
 *
 * <!-- 면적/스무딩 -->
 * <TimeSeriesChart :data="points" name="방문자 수" color="#16a34a" area smooth />
 *
 * <!-- 툴팁 끄기 -->
 * <TimeSeriesChart :data="points" :show-tooltip="false" />
 *
 * <!-- y축 고정 스케일 -->
 * <TimeSeriesChart :data="points" :y-scale="false" />
 */

/**
 * @typedef {Object} TimeSeriesChartProps
 * @property {Array<{t: Date|number|string, y: number}>} data - 시계열 데이터
 * @property {string} [name=''] - 시리즈 이름
 * @property {number} [height=320] - 차트 높이(px)
 * @property {string|object} [theme=''] - ECharts 테마
 * @property {boolean} [loading=false] - 로딩 오버레이 표시 여부
 * @property {string} [color='#2563eb'] - 선/면 색상
 * @property {boolean} [area=false] - 면적 영역 표시 여부
 * @property {boolean} [smooth=false] - 곡선 스무딩 여부
 * @property {boolean} [yScale=true] - y축 scale 적용 여부
 * @property {boolean} [showGrid=true] - 그리드 표시 여부
 * @property {boolean} [showTooltip=true] - 툴팁 표시 여부
 * @property {string} [locale='ko-KR'] - 숫자 포맷 로케일
 * @property {number} [decimals=2] - 소수점 자리수
 */
const props = defineProps({
  /** [{ t: Date|number|string, y: number }] */
  data: { type: Array, default: () => [] },
  name: { type: String, default: '' },
  height: { type: Number, default: 320 },
  theme:  { type: [String, Object], default: '' },
  loading:{ type: Boolean, default: false },

  color: { type: String, default: '#2563eb' },
  area: { type: Boolean, default: false },
  smooth: { type: Boolean, default: false },
  yScale: { type: Boolean, default: true },
  showGrid: { type: Boolean, default: true },
  showTooltip: { type: Boolean, default: true },

  locale: { type: String, default: 'ko-KR' },
  decimals: { type: Number, default: 2 },
})

/**
 * ECharts에 전달할 데이터 포인트
 * - t는 Date/타임스탬프/문자열 → ms 단위 숫자
 * - y는 number 변환
 * @type {import('vue').ComputedRef<Array<[number, number]>>}
 */
const points = computed(() =>
  props.data.map(d => [new Date(d.t).getTime(), Number(d.y)])
)

/**
 * 최종 ECharts 옵션
 * - grid/xAxis/yAxis/tooltip/series 설정
 * - area/smooth/color/locale/decimals 반영
 * @type {import('vue').ComputedRef<object>}
 */
const option = computed(() => {
  const color = props.color
  const gradient = {
    type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
    colorStops: [
      { offset: 0, color: color + '66' },
      { offset: 1, color: color + '00' }
    ]
  }

  return {
    grid: { left: 54, right: 16, top: 24, bottom: 32, containLabel: false },
    xAxis: {
      type: 'time', boundaryGap: false,
      axisLine: { show: false }, axisTick: { show: false },
      splitLine: { show: props.showGrid, lineStyle: { color: '#e5e7eb' } },
      axisLabel: { hideOverlap: true }
    },
    yAxis: {
      type: 'value', scale: props.yScale,
      axisLine: { show: false }, axisTick: { show: false },
      splitLine: { show: props.showGrid, lineStyle: { color: '#e5e7eb' } },
      axisLabel: {
        formatter: (v) => new Intl.NumberFormat(props.locale, {
          maximumFractionDigits: props.decimals
        }).format(v)
      }
    },
    tooltip: props.showTooltip ? {
      trigger: 'axis',
      axisPointer: { type: 'line' },
      valueFormatter: (v) => new Intl.NumberFormat(props.locale, {
        minimumFractionDigits: props.decimals,
        maximumFractionDigits: props.decimals
      }).format(v)
    } : { show: false },
    series: [{
      type: 'line', name: props.name, data: points.value,
      showSymbol: false, sampling: 'lttb', smooth: props.smooth,
      lineStyle: { width: 2, color },
      areaStyle: props.area ? { color: gradient } : undefined
    }]
  }
})
</script>
