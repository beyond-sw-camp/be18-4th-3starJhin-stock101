<template>
  <span
    class="metric"
    :class="[`s-${size}`]"
    role="text"
    :aria-label="a11yLabel"
    :title="a11yLabel"
  >
    <span class="val">{{ formatted }}</span>
    <span v-if="unit" class="unit">{{ unit }}</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: { type: [Number, String, null], default: null },

  // Intl
  locale: { type: String, default: 'ko-KR' },
  currency: { type: String, default: '' },       // 'USD' 같은 ISO 통화코드
  compact: { type: Boolean, default: false },    // 1.2K/3.4M…
  useGrouping: { type: Boolean, default: true }, // 천단위 구분기호

  // 소수점 제어 (min/max 분리)
  minDecimals: { type: Number, default: 0 },
  maxDecimals: { type: Number, default: 2 },

  // 표시 관련
  unit: { type: String, default: '' },
  size: { type: String, default: 'xl' },         // sm | md | lg | xl
  fallback: { type: String, default: '—' },      // null/undefined/''/NaN 시
})

const isNil = (v) => v === null || v === undefined || v === ''
const toNumber = (v) => (typeof v === 'number' ? v : Number(v))

const nfOptions = computed(() => ({
  minimumFractionDigits: props.compact ? 0 : props.minDecimals,
  maximumFractionDigits: Math.max(props.minDecimals, props.maxDecimals),
  useGrouping: props.useGrouping,
}))

const formatted = computed(() => {
  if (isNil(props.value)) return props.fallback

  const num = toNumber(props.value)
  if (Number.isNaN(num)) return props.fallback

  // 통화 우선
  if (props.currency) {
    return new Intl.NumberFormat(props.locale, {
      ...nfOptions.value,
      style: 'currency',
      currency: props.currency,
    }).format(num)
  }

  // compact
  if (props.compact) {
    return new Intl.NumberFormat(props.locale, {
      ...nfOptions.value,
      notation: 'compact',
      compactDisplay: 'short',
    }).format(num)
  }

  // 일반 숫자
  return new Intl.NumberFormat(props.locale, nfOptions.value).format(num)
})

const a11yLabel = computed(() =>
  props.unit ? `${formatted.value} ${props.unit}` : String(formatted.value)
)
</script>

<style scoped>
.metric{
  /* 토큰(치수만 바꿔 여러 사이즈 지원) */
  --val-size: 40px;
  --val-weight: 800;
  --val-line: 1.1;
  --unit-color: #6b7280;

  display: inline-flex;
  align-items: baseline;
  gap: .25rem;
  color: #0b0f19;
}
.val{
  font-size: var(--val-size);
  font-weight: var(--val-weight);
  line-height: var(--val-line);
}
.unit{
  color: var(--unit-color);
  font-weight: 600;
}

/* 사이즈 프리셋 */
.s-sm { --val-size: 18px; --val-weight: 700; --val-line: 1.3; }
.s-md { --val-size: 22px; --val-weight: 700; --val-line: 1.25; }
.s-lg { --val-size: 28px; --val-weight: 700; --val-line: 1.2; }
.s-xl { --val-size: 40px; --val-weight: 800; --val-line: 1.1; }
</style>
