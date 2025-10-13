<template>
  <!-- 컨테이너 태그 커스텀 가능 -->
  <component :is="as" class="grid" :style="gridStyle">
    <!-- 비어있을 때 -->
    <slot name="empty" v-if="!items || !items.length">
      <!-- 기본 empty UI -->
      <div class="grid__empty">데이터가 없습니다.</div>
    </slot>

    <!-- 아이템 렌더 -->
    <component
      v-else
      v-for="(it, i) in items"
      :key="keyOf(it, i)"
      :is="itemTag"
      class="grid__item"
      :class="itemClass"
      :style="itemStyle"
    >
      <!-- 기본 슬롯: 카드 템플릿 -->
      <slot :item="it" :index="i">
        <!-- 기본 대체 템플릿 -->
        <pre>{{ it }}</pre>
      </slot>
    </component>
  </component>
</template>

<script setup>
import { computed } from 'vue'

/**
 * @component Grid
 * @description
 * 데이터 배열을 받아 CSS Grid 형태로 배치하는 범용 레이아웃 컴포넌트  
 * - `items` 배열을 v-for로 렌더링  
 * - 고정 `cols` 또는 반응형 `minColWidth` 기반 자동 레이아웃 지원  
 * - `empty` 슬롯 제공 (기본: "데이터가 없습니다.")  
 * - itemTag/class/style로 아이템 요소를 커스터마이즈 가능
 *
 * @example
 * <!-- 기본 사용 -->
 * <Grid :items="list">
 *   <template #default="{ item }">
 *     <Card :data="item" />
 *   </template>
 * </Grid>
 *
 * <!-- 고정 컬럼 수 -->
 * <Grid :items="list" :cols="3" />
 *
 * <!-- 반응형 minColWidth -->
 * <Grid :items="list" :min-col-width="240" gap="16px" />
 *
 * <!-- empty 슬롯 -->
 * <Grid :items="[]">
 *   <template #empty>⚠️ 데이터 없음</template>
 * </Grid>
 */

/**
 * @typedef {Object} GridProps
 * @property {Array} [items=[]] - 렌더링할 데이터 배열
 * @property {number|string|null} [cols=null] - 고정 컬럼 수 (있으면 우선 적용)
 * @property {number|string} [minColWidth=220] - 반응형 최소 컬럼 너비(px 또는 문자열)
 * @property {string} [gap='12px'] - 행/열 간격 (rowGap/colGap 미지정 시 사용)
 * @property {string} [rowGap] - 행 간격
 * @property {string} [colGap] - 열 간격
 * @property {string} [as='div'] - 컨테이너 태그명
 * @property {string} [itemTag='div'] - 아이템 태그명
 * @property {string|Array|Object} [itemClass=''] - 아이템 클래스
 * @property {string|Object} [itemStyle=''] - 아이템 인라인 스타일
 * @property {string|Function|null} [itemKey=null] - 아이템 key 결정 (필드명 또는 함수)
 * @property {'start'|'center'|'end'|'stretch'} [alignItems='stretch'] - CSS align-items
 * @property {'start'|'center'|'end'|'stretch'} [justifyItems='stretch'] - CSS justify-items
 */
const props = defineProps({
  items: { type: Array, default: () => [] },
  cols: { type: [Number, String], default: null },
  minColWidth: { type: [Number, String], default: 220 },
  gap: { type: String, default: '12px' },
  rowGap: { type: String, default: '' },
  colGap: { type: String, default: '' },
  as: { type: String, default: 'div' },
  itemTag: { type: String, default: 'div' },
  itemClass: { type: [String, Array, Object], default: '' },
  itemStyle: { type: [String, Object], default: '' },
  itemKey: { type: [String, Function], default: null },
  alignItems: { type: String, default: 'stretch' },
  justifyItems: { type: String, default: 'stretch' },
})

/** 숫자값을 px 단위 문자열로 변환 */
function toPx(v) {
  if (v == null || v === '') return ''
  return typeof v === 'number' ? `${v}px` : v
}

/**
 * 최종 grid 스타일 계산
 * - cols 우선 적용, 없으면 auto-fit + minColWidth
 * - gap/rowGap/colGap 적용
 */
const gridStyle = computed(() => {
  const style = {
    display: 'grid',
    alignItems: props.alignItems,
    justifyItems: props.justifyItems,
  }
  if (props.rowGap || props.colGap) {
    if (props.rowGap) style.rowGap = props.rowGap
    if (props.colGap) style.columnGap = props.colGap
  } else {
    style.gap = props.gap
  }
  if (props.cols != null && props.cols !== '') {
    style.gridTemplateColumns = `repeat(${props.cols}, minmax(0, 1fr))`
  } else {
    const minw = toPx(props.minColWidth) || '220px'
    style.gridTemplateColumns = `repeat(auto-fit, minmax(${minw}, 1fr))`
  }
  return style
})

/** 각 아이템의 key 결정 */
function keyOf(it, i) {
  if (!props.itemKey) return i
  if (typeof props.itemKey === 'function') return props.itemKey(it, i)
  return it?.[props.itemKey] ?? i
}
</script>

<style scoped>
.grid { width: 100%; }
.grid__item { min-width: 0; } /* 카드 내부 텍스트 말줄임 등에 필요 */
.grid__empty { color:#6b7280; font-size:.9rem; padding: 12px 0; }
</style>
