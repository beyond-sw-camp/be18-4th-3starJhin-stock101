<template>
  <BaseChart :option="option" :height="chartHeight" :theme="theme" :loading="loading" />
</template>

<script setup>
import { computed } from 'vue'
import BaseChart from '../BaseChart.vue'

/**
 * @component GaugeChart
 * @description
 * 게이지 차트(계기판) 컴포넌트  
 * - ECharts gauge series 기반  
 * - 최소/최대 범위, 시작/끝 각도, 두께, 세그먼트 색상, 포인터 색상 설정 가능  
 * - ticks 옵션으로 분할선 표시 가능  
 * - BaseChart 래퍼를 통해 로딩/테마 옵션 그대로 지원
 *
 * @example
 * <!-- 기본 사용 -->
 * <GaugeChart :value="72" />
 *
 * <!-- 색상 세그먼트 커스터마이즈 -->
 * <GaugeChart
 *   :value="35"
 *   :segments="[[0.5,'#ef4444'], [1.0,'#22c55e']]"
 * />
 *
 * <!-- 두께와 각도 변경 -->
 * <GaugeChart :value="90" :thickness="30" :start-angle="180" :end-angle="0" />
 *
 * <!-- 분할선 표시 -->
 * <GaugeChart :value="45" ticks />
 */

/**
 * @typedef {Object} GaugeChartProps
 * @property {number} [value=50] - 현재 값
 * @property {number} [min=0] - 최소값
 * @property {number} [max=100] - 최대값
 * @property {number} [startAngle=210] - 시작 각도 (시계 방향, 0=우측)
 * @property {number} [endAngle=-30] - 끝 각도
 * @property {number} [thickness=20] - 게이지 두께(px)
 * @property {number} [height=300] - 차트 높이(px)
 * @property {number|null} [size=null] - height alias(게이지 전체 크기)
 * @property {string|number} [radius='90%'] - 게이지 반경
 * @property {string|object} [theme=''] - ECharts 테마
 * @property {boolean} [loading=false] - 로딩 오버레이 표시 여부
 * @property {Array<[number,string]>} [segments] - 게이지 색상 세그먼트 [[stop(0~1), color], ...]
 * @property {string} [pointerColor='#0b0f19'] - 포인터 색상
 * @property {boolean} [ticks=false] - 분할선 표시 여부
 * @property {Array<string>} [bandLabels] - 게이지 구간 라벨
 * @property {number} [labelDistance=30] - 구간 라벨과 게이지 간격
 * @property {boolean} [labelsOutside=false] - 라벨을 게이지 외곽에 배치할지 여부
 */
const props = defineProps({
  value: { type: Number, default: 50 },
  min: { type: Number, default: 0 },
  max: { type: Number, default: 100 },
  startAngle: { type: Number, default: 210 },   // 시계 방향, 0=우측
  endAngle: { type: Number, default: -30 },
  thickness: { type: Number, default: 20 },
  height: { type: Number, default: 300 },
  size: { type: Number, default: null },
  radius: { type: [String, Number], default: '90%' },
  theme:  { type: [String, Object], default: '' },
  loading:{ type: Boolean, default: false },
  // [[stop(0~1), color], ...]
  segments: {
    type: Array,
    default: () => [
      [0.2, '#ef4444'], [0.4, '#f97316'], [0.6, '#facc15'],
      [0.8, '#22c55e'], [1.0, '#16a34a']
    ]
  },
  pointerColor: { type: String, default: '#0b0f19' },
  ticks: { type: Boolean, default: false },
  bandLabels: {
    type: Array,
    default: () => ['Strong Sell', 'Sell', 'Hold', 'Buy', 'Strong Buy']
  },
  labelDistance: { type: Number, default: 28 },
  labelsOutside: { type: Boolean, default: false }
})

const chartHeight = computed(() => props.size ?? props.height)

const resolvedLabelDistance = computed(() => {
  const distance = Math.abs(props.labelDistance)
  return props.labelsOutside ? -distance : distance
})

const labelStops = computed(() => {
  const labels = Array.isArray(props.bandLabels) ? props.bandLabels.filter(Boolean) : []
  if (!labels.length) return []

  const steps = labels.length - 1
  if (steps <= 0) return []

  const gap = (props.max - props.min) / steps
  return labels.map((label, index) => ({
    value: props.min + gap * index,
    label,
  }))
})

/**
 * 최종 ECharts 옵션
 * - 게이지 타입
 * - 축선 색상/두께, 포인터, 앵커 등 스타일 적용
 * - detail은 숨김 (단순 게이지 표시용)
 * @type {import('vue').ComputedRef<object>}
 */
const option = computed(() => {
  const labels = labelStops.value
  const labelMap = new Map(labels.map(({ value, label }) => [Number(value.toFixed(6)), label]))
  const labelStep = labels.length > 1 ? (props.max - props.min) / (labels.length - 1) : null

  return {
    grid: { top: 10, bottom: 0, left: 0, right: 0 },
    series: [{
      type: 'gauge',
      min: props.min,
      max: props.max,
      startAngle: props.startAngle,
      endAngle: props.endAngle,
      radius: props.radius,
      splitNumber: labels.length > 1 ? labels.length - 1 : 4,
      axisLine: {
        lineStyle: {
          width: props.thickness,
          color: props.segments,
          cap: 'round',
        },
      },
      splitLine: { show: props.ticks, length: 10, distance: 0 },
      axisTick:  { show: false },
      axisLabel: {
        show: labels.length > 0,
        distance: resolvedLabelDistance.value,
        color: '#111827',
        fontSize: 12,
        fontWeight: 600,
        formatter(value) {
          if (!labels.length) return ''
          if (labelStep == null) {
            const key = Number(value.toFixed(6))
            return labelMap.get(key) ?? ''
          }
          const relative = Math.round((value - props.min) / labelStep)
          const key = Number((props.min + relative * labelStep).toFixed(6))
          return labelMap.get(key) ?? ''
        },
      },
      progress:  { show: false },
      pointer:   {
        show: true,
        length: '70%',
        width: 6,
        itemStyle: { color: props.pointerColor },
      },
      anchor:    {
        show: true,
        size: 12,
        itemStyle: { color: props.pointerColor },
      },
      detail:    { show: false },
      data: [{ value: props.value }],
    }],
  }
})
</script>
