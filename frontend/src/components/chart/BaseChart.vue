<template>
  <div class="chart-wrap" :style="{ height: height + 'px', width }">
    <VChart
      ref="chartRef"
      :option="mergedOption"
      :theme="theme || undefined"
      :autoresize="true"
    />
    <div v-if="loading" class="overlay">로딩 중…</div>
    <div v-else-if="empty" class="overlay empty">{{ emptyText }}</div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import {
  GridComponent, TooltipComponent, LegendComponent, TitleComponent, AriaComponent
} from 'echarts/components'
import { GaugeChart, LineChart, BarChart, PieChart } from 'echarts/charts'

/**
 * @component BaseECharts
 * @description
 * ECharts용 범용 래퍼 컴포넌트  
 * - 필요한 렌더러/차트/컴포넌트를 한 곳에서 등록하고 공통 옵션을 병합해 렌더링  
 * - `loading`/`empty` 상태 오버레이 제공  
 * - `getInstance()`를 통해 내부 ECharts 인스턴스 접근 가능(줌/리사이즈/dispatchAction 등)
 *
 * @example
 * <!-- 라인 차트 -->
 * <BaseECharts
 *   :option="{
 *     xAxis: { type: 'category', data: ['Mon','Tue','Wed'] },
 *     yAxis: { type: 'value' },
 *     series: [{ type: 'line', data: [120, 200, 150] }]
 *   }"
 * />
 *
 * <!-- 로딩/빈 상태 -->
 * <BaseECharts :option="opt" :loading="isLoading" :empty="!data.length" empty-text="데이터 없음" />
 *
 * <!-- 인스턴스 사용 -->
 * <BaseECharts ref="chart">
 * </BaseECharts>
 * // setup() 내에서:
 * const chart = ref(null)
 * onMounted(() => {
 *   const inst = chart.value?.getInstance()
 *   inst?.resize()
 * })
 */

/**
 * 필요한 모듈을 전역 등록(이 컴포넌트에서만 수행)
 */
use([
  CanvasRenderer,
  GridComponent, TooltipComponent, LegendComponent, TitleComponent, AriaComponent,
  GaugeChart, LineChart, BarChart, PieChart
])

/**
 * @typedef {Object} BaseEChartsProps
 * @property {object} option - ECharts 옵션 객체(필수). 이 컴포넌트의 기본 옵션과 병합됨
 * @property {number} [height=260] - 차트 영역 높이(px)
 * @property {string} [width='100%'] - 차트 영역 너비(CSS 값)
 * @property {boolean} [loading=false] - 로딩 오버레이 표시 여부
 * @property {boolean} [empty=false] - 빈 상태 오버레이 표시 여부
 * @property {string} [emptyText='데이터가 없습니다.'] - 빈 상태 오버레이 문구
 * @property {string|object} [theme=''] - ECharts 테마 이름 또는 테마 객체
 */
const props = defineProps({
  option: { type: Object, required: true },
  height: { type: Number, default: 260 },
  width:  { type: String,  default: '100%' },
  loading:{ type: Boolean, default: false },
  empty:  { type: Boolean, default: false },
  emptyText: { type: String, default: '데이터가 없습니다.' },
  theme:  { type: [String, Object], default: '' }
})

/**
 * 프로젝트 공통 기본 옵션
 * - 폰트/애니메이션/툴팁 기본값 설정
 */
const baseOption = {
  textStyle: { fontFamily: 'ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto' },
  animationDuration: 500,
  tooltip: { show: true, appendToBody: true }
}

/**
 * 최종 옵션(기본 옵션 + 사용자 옵션 병합)
 * @type {import('vue').ComputedRef<object>}
 */
const mergedOption = computed(() => ({ ...baseOption, ...props.option }))

/**
 * 내부 ECharts 인스턴스 참조 및 노출 메서드
 * - getInstance(): ECharts 인스턴스 반환(없으면 undefined)
 *   예) getInstance()?.dispatchAction({ type: 'highlight', seriesIndex: 0 })
 *   예) getInstance()?.resize()
 */
const chartRef = ref(null)
/** @returns {any|undefined} ECharts 인스턴스 */
const getInstance = () => chartRef.value?.chart
defineExpose({ getInstance })
</script>

<style scoped>
.chart-wrap { position: relative; }
.overlay {
  position:absolute; inset:0; display:grid; place-items:center;
  background: rgba(255,255,255,.6); backdrop-filter: blur(2px);
  font-size:.95rem; color:#6b7280; border-radius: 8px;
}
.empty { background: transparent; }
</style>
