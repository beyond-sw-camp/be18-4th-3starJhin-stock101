<template>
  <BaseCard
    class="stat-card"
    :variant="variant"
    :pad="pad"
    :radius="radius"
    :clickable="clickable || !!to || !!href"
    :to="to"
    :href="href"
    :target="target"
    :rel="rel"
  >
    <!-- 헤더 -->
    <template #header>
      <div class="head">
        <div class="title">
          <slot name="title">{{ title }}</slot>
        </div>
        <div class="badge">
          <slot name="badge">
            <Pill
              v-if="showBadgeComputed"
              :text="badgeText || formattedChange"
              :variant="badgeVariantAuto"
              tone="soft"
              size="sm"
            />
          </slot>
        </div>
      </div>
    </template>

    <!-- 본문 -->
    <div :class="['content', `trend-${trend}`]">
      <div class="subtitle" v-if="$slots.subtitle || subtitle">
        <slot name="subtitle">{{ subtitle }}</slot>
      </div>

      <div class="spacer" />

      <div class="value">
        <slot name="value">
          <MetricValue
            :value="value"
            :locale="locale"
            :decimals="decimals"
            :currency="currency"
            :compact="compact"
            size="xl"
          />
        </slot>
      </div>

      <div class="sub" v-if="showSub">
        <slot name="sub">
          <span class="label" v-if="subPrefix">{{ subPrefix }}</span>
          <span class="delta">{{ formattedChange }}</span>
        </slot>
      </div>
    </div>
  </BaseCard>
</template>

<script setup>
import { computed } from 'vue'
import BaseCard from '../BaseCard.vue'
import Pill from '@/components/ui/Pill.vue'
import MetricValue from '@/components/ui/MetricValue.vue' // 경로는 프로젝트 구조에 맞게 조정

/**
 * @component StatCard
 * @description
 * 통계/지표를 표시하는 카드 컴포넌트  
 * - 제목/부제목/값을 표시하고, 변화량(`change`)을 배지 및 하단 보조 텍스트로 제공  
 * - `percentMode`를 통해 변화량을 % 또는 절대값으로 표기  
 * - `trend`(up/down/flat)를 직접 지정하거나 `change`로부터 자동 도출  
 * - BaseCard의 스타일/링크 옵션을 그대로 지원
 *
 * @slot title    카드 상단 제목 (기본: `title` prop)
 * @slot badge    제목 옆 배지 영역 (기본: 변화량 Pill 또는 `badgeText`)
 * @slot subtitle 부제목 영역 (기본: `subtitle` prop)
 * @slot value    메인 값 영역 (기본: MetricValue 렌더링)
 * @slot sub      하단 보조 영역 (기본: `subPrefix` + 변화량)
 *
 * @example
 * <!-- 기본 사용 -->
 * <StatCard title="일일 방문자" :value="12345" :change="+2.5" />
 *
 * <!-- 절대값 변화 모드 -->
 * <StatCard
 *   title="매출"
 *   :value="1200000"
 *   currency="KRW"
 *   :change="-35000"
 *   :percent-mode="false"
 * />
 *
 * <!-- 트렌드 강제 및 커스텀 배지 -->
 * <StatCard title="에러율" :value="0.12" :trend="'down'">
 *   <template #badge><Tag variant="info">안정</Tag></template>
 * </StatCard>
 */

/**
 * @typedef {Object} StatCardProps
 * @property {string}   [title='']         - 카드 제목
 * @property {string}   [subtitle='']      - 카드 부제목
 * @property {number|string} [value='']    - 표시할 메트릭 값
 * @property {string}   [locale='ko-KR']   - 숫자/통화 포맷 로케일
 * @property {number}   [decimals=2]       - 소수점 자리수
 * @property {string}   [currency='']      - 통화 코드 (예: 'KRW','USD')
 * @property {boolean}  [compact=false]    - 축약 표기 (예: 1.2K)
 * @property {number|null} [change=null]   - 변화량 (양수/음수/0)
 * @property {boolean}  [percentMode=true] - 변화량을 %로 표기할지 여부
 * @property {'up'|'down'|'flat'|''} [trend=''] - 추세(미지정 시 change로 자동 산출)
 * @property {string}   [subPrefix='어제보다'] - 하단 변화 설명 접두어
 * @property {string}   [badgeText='']     - 배지 텍스트(우선 적용)
 * @property {boolean}  [showBadge=true]   - 배지 표시 여부
 * @property {'outlined'|'elevated'|'soft'|'ghost'} [variant='outlined'] - BaseCard variant
 * @property {'none'|'sm'|'md'|'lg'} [pad='lg'] - BaseCard padding
 * @property {'md'|'lg'|'xl'|'full'} [radius='xl'] - BaseCard radius
 * @property {boolean}  [clickable=false]  - 카드 클릭 가능 여부
 * @property {string|object|null} [to=null] - router-link 목적지
 * @property {string}   [href='']          - a 태그 링크
 * @property {string}   [target='_self']   - 링크 target
 * @property {string}   [rel='']           - 링크 rel
 */
const props = defineProps({
  /* 콘텐츠 */
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  value: { type: [Number, String], default: '' },
  locale: { type: String, default: 'ko-KR' },
  decimals: { type: Number, default: 2 },
  currency: { type: String, default: '' },
  compact: { type: Boolean, default: false },

  /* 변화/추세 */
  change: { type: Number, default: null },
  percentMode: { type: Boolean, default: true },
  trend: { type: String, default: '' },     // 'up' | 'down' | 'flat' | ''
  subPrefix: { type: String, default: '어제보다' },

  /* 뱃지 */
  badgeText: { type: String, default: '' },
  showBadge: { type: Boolean, default: true },

  /* BaseCard 전달용(옵션) */
  variant: { type: String, default: 'outlined' }, // outlined|elevated|soft|ghost
  pad: { type: String, default: 'lg' },           // none|sm|md|lg
  radius: { type: String, default: 'xl' },        // md|lg|xl|full
  clickable: { type: Boolean, default: false },
  to: { type: [String, Object], default: null },
  href: { type: String, default: '' },
  target: { type: String, default: '_self' },
  rel: { type: String, default: '' },
})

/**
 * 실제 표시할 추세
 * - trend prop이 있으면 그대로 사용
 * - 없으면 change 값으로 자동 도출
 * @type {import('vue').ComputedRef<'up'|'down'|'flat'>}
 */
const effTrend = computed(() => {
  if (props.trend) return props.trend
  if (props.change == null) return 'flat'
  if (props.change > 0) return 'up'
  if (props.change < 0) return 'down'
  return 'flat'
})
/** 템플릿 사용을 위한 별칭 */
const trend = effTrend

/**
 * 하단 변화 표시 영역 노출 여부
 * - change가 null/undefined가 아니면 노출
 * @type {import('vue').ComputedRef<boolean>}
 */
const showSub = computed(() => props.change !== null && props.change !== undefined)

/**
 * 변화량 포맷 문자열
 * - percentMode=true → ±xx.xx%
 * - percentMode=false → 로케일/소수점 기준 숫자 포맷(부호 포함)
 * @type {import('vue').ComputedRef<string>}
 */
const formattedChange = computed(() => {
  if (!showSub.value) return ''
  const sign = props.change > 0 ? '+' : props.change < 0 ? '−' : ''
  const abs = Math.abs(props.change)
  return props.percentMode
    ? `${sign}${abs.toFixed(2)}%`
    : new Intl.NumberFormat(props.locale, {
        minimumFractionDigits: props.decimals,
        maximumFractionDigits: props.decimals
      }).format(props.change) // 부호 포함
})

/**
 * 배지 색상 자동 결정
 * - up => success, down => danger, flat => neutral
 * @type {import('vue').ComputedRef<'success'|'danger'|'neutral'>}
 */
const badgeVariantAuto = computed(
  () => ({ up:'success', down:'danger', flat:'neutral' }[effTrend.value] || 'neutral')
)

/**
 * 배지 표시 여부
 * - showBadge가 true이고, badgeText 또는 변화량이 있을 때
 * @type {import('vue').ComputedRef<boolean>}
 */
const showBadgeComputed = computed(() => props.showBadge && (props.badgeText || showSub.value))
</script>

<style scoped>
/* BaseCard가 테두리/패딩 담당 → 내부 레이아웃만 */
.head{ display:flex; align-items:center; justify-content:flex-start; gap: 9px;}
.title{ color:#111827; font-weight:700; letter-spacing:.01em; font-size:18px; }

.content{ display:flex; flex-direction:column; gap:12px; min-height:160px; }
.subtitle{ color:#6b7280; }
.spacer{ flex:1 1 auto; }
.value{ margin-top:4px; }

.sub{ font-weight:600; color:#16a34a; }
.trend-down .sub{ color:#ef4444; }
.trend-flat .sub{ color:#6b7280; }
.sub .label{ margin-right:6px; font-weight:500; color:#6b7280; }
</style>
