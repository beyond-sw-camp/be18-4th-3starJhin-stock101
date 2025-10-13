<!-- src/components/card/PredictionCard.vue -->
<template>
  <BaseCard
    class="prediction-card"
    variant="outlined"
    :radius="radius"
    :pad="pad"
    :clickable="!!to || !!href"
    :to="to"
    :href="href"
    :target="target"
  >
    <div class="row">
      <div class="name"><slot name="name">{{ name }}</slot></div>
      <div v-if="dateText" class="date"><slot name="date">{{ dateText }}</slot></div>
      <div class="spacer" />
      <div class="badge">
        <slot name="badge">
          <Pill :text="badgeLabel" :variant="badgeVariant" tone="soft" size="sm" />
        </slot>
      </div>
    </div>
  </BaseCard>
</template>

<script setup>
import { computed } from 'vue'
import BaseCard from '@/components/card/BaseCard.vue'
import Pill from '@/components/ui/Pill.vue'

/**
 * @component PredictionCard
 * @description
 * 예측 결과를 표시하는 카드 컴포넌트  
 * - 이름(name), 날짜(dateText), 예측 상태(status)를 표시  
 * - 상태에 따라 배지 텍스트/색상이 자동 결정됨  
 * - 슬롯을 통해 name/date/badge 영역을 커스터마이즈 가능  
 * - BaseCard를 기반으로 하며, 링크(to/href) 지원
 *
 * @slot name   이름 영역 (기본: `name` prop)
 * @slot date   날짜 영역 (기본: `dateText` prop)
 * @slot badge  상태 배지 영역 (기본: status에 따른 Pill)
 *
 * @example
 * <!-- 기본 사용 -->
 * <PredictionCard name="삼성전자" date-text="2025-09-15" status="up" />
 *
 * <!-- 상태 텍스트 오버라이드 -->
 * <PredictionCard name="Apple" status="down" status-text="하락 예상" />
 *
 * <!-- 슬롯 커스터마이즈 -->
 * <PredictionCard>
 *   <template #name>커스텀 이름</template>
 *   <template #date>오늘</template>
 *   <template #badge><Tag variant="info">직접 지정</Tag></template>
 * </PredictionCard>
 */

/**
 * @typedef {Object} PredictionCardProps
 * @property {string} [name=''] - 이름
 * @property {string} [dateText=''] - 날짜 문자열
 * @property {'up'|'down'|'neutral'} [status='up'] - 예측 상태
 * @property {string} [statusText=''] - 상태 텍스트 오버라이드 (기본은 UP/DOWN/HOLD)
 * @property {string|object|null} [to=null] - router-link 목적지
 * @property {string} [href=''] - a 태그 링크
 * @property {string} [target='_self'] - 링크 target
 * @property {string} [pad='lg'] - 카드 padding
 * @property {string} [radius='xl'] - 카드 모서리 둥글기
 */
const props = defineProps({
  name: { type: String, default: '' },
  dateText: { type: String, default: '' },

  /** 상태: 'up' | 'down' | 'neutral' */
  status: { type: String, default: 'up' },
  statusText: { type: String, default: '' },   // 표시 텍스트를 커스터마이즈하고 싶을 때

  // 링크 옵션
  to: { type: [String, Object], default: null },
  href: { type: String, default: '' },
  target: { type: String, default: '_self' },

  // 카드 스타일 옵션
  pad: { type: String, default: 'lg' },
  radius: { type: String, default: 'xl' },
})

/**
 * 상태 배지 텍스트
 * - statusText가 있으면 우선 사용
 * - 기본은 'UP' / 'DOWN' / 'HOLD'
 */
const badgeLabel = computed(() =>
  props.statusText ||
  (props.status === 'up' ? 'UP' : props.status === 'down' ? 'DOWN' : 'HOLD')
)

/**
 * 상태 배지 색상
 * - up → success (초록)
 * - down → danger (빨강)
 * - neutral → neutral (회색)
 */
const badgeVariant = computed(() => {
  switch (props.status) {
    case 'up': return 'success'
    case 'down': return 'danger'
    default: return 'neutral'
  }
})
</script>

<style scoped>
.prediction-card { min-height: 76px; display:flex; align-items:center; }
.row { display:flex; align-items:center; gap:16px; width:100%; }
.name { font-weight: 800; font-size: 20px; color:#111827; }
.date { color:#9ca3af; }
.spacer { flex:1 1 auto; }
.badge :deep(.pill) { /* 선택: 배지와 간격 조정이 필요하면 사용 */ }
</style>
