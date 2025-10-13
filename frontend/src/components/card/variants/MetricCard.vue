<template>
  <BaseCard
    class="metric-card"
    :variant="variant"
    :pad="pad"
    :radius="radius"
    :clickable="clickable"
    :to="to"
    :href="href"
    :target="target"
    :rel="rel"
  >
    <!-- 헤더 -->
    <template #header>
      <div class="row head">
        <div class="title"><slot name="title">{{ title }}</slot></div>
        <div class="badge">
          <slot name="badge">
            <Pill v-if="badgeText" :text="badgeText" :variant="badgeVariant" tone="soft" size="sm" />
          </slot>
        </div>
      </div>
    </template>

    <!-- 본문 -->
    <div class="content">
      <div class="subtitle" v-if="$slots.subtitle || subtitle">
        <slot name="subtitle">{{ subtitle }}</slot>
      </div>

      <div class="spacer" />

      <div class="value">
        <slot name="value">
          <MetricValue
            :value="value"
            :decimals="decimals"
            :locale="locale"
            :currency="currency"
            size="xl"
          />
        </slot>
      </div>
    </div>
  </BaseCard>
</template>

<script setup>
import BaseCard from '@/components/card/BaseCard.vue'
import Pill from '@/components/ui/Pill.vue'
import MetricValue from '@/components/ui/MetricValue.vue'

/**
 * @component MetricCard
 * @description
 * 주요 지표(메트릭)를 표시하는 카드 컴포넌트  
 * - 제목(title), 서브타이틀(subtitle), 값(value) 표시  
 * - 배지(badge)를 통해 추가 상태 정보 제공  
 * - 값은 `MetricValue` 컴포넌트를 통해 로케일/통화/소수점 자리 맞춤 가능  
 * - BaseCard의 variant/pad/radius 옵션과 링크 기능 그대로 지원
 *
 * @slot title    카드 상단 제목 (기본: `title` prop)
 * @slot badge    제목 옆 배지 영역 (기본: `badgeText` + `badgeVariant`)
 * @slot subtitle 서브타이틀 영역 (기본: `subtitle` prop)
 * @slot value    메트릭 값 영역 (기본: MetricValue)
 *
 * @example
 * <!-- 기본 사용 -->
 * <MetricCard title="매출액" :value="1200000" currency="KRW" />
 *
 * <!-- 서브타이틀/배지 포함 -->
 * <MetricCard
 *   title="방문자 수"
 *   subtitle="지난주 대비"
 *   :value="3456"
 *   badge-text="+12%"
 *   badge-variant="success"
 * />
 *
 * <!-- 커스텀 슬롯 -->
 * <MetricCard title="서버 상태">
 *   <template #value>
 *     <StatusIndicator status="online" />
 *   </template>
 *   <template #badge>
 *     <Tag variant="info">실시간</Tag>
 *   </template>
 * </MetricCard>
 */

/**
 * @typedef {Object} MetricCardProps
 * @property {string}  [title='']         - 카드 제목
 * @property {string}  [subtitle='']      - 카드 서브타이틀
 * @property {number|string|null} [value=null] - 표시할 값
 * @property {number}  [decimals=1]       - 소수점 자리수
 * @property {string}  [locale='ko-KR']   - 숫자/통화 포맷 로케일
 * @property {string}  [currency='']      - 통화 코드 (예: 'USD', 'KRW')
 * @property {string}  [badgeText='']     - 배지 텍스트
 * @property {'neutral'|'success'|'danger'|'info'|'warning'} [badgeVariant='neutral'] - 배지 색상
 * @property {string}  [variant='outlined'] - BaseCard variant
 * @property {string}  [pad='lg']         - BaseCard padding
 * @property {string}  [radius='xl']      - BaseCard radius
 * @property {boolean} [clickable=false]  - 카드 클릭 가능 여부
 * @property {string|object|null} [to=null] - router-link 목적지
 * @property {string}  [href='']          - a 태그 링크
 * @property {string}  [target='_self']   - 링크 target
 * @property {string}  [rel='']           - 링크 rel
 */
const props = defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  value: { type: [Number, String, null], default: null },
  decimals: { type: Number, default: 1 },
  locale: { type: String, default: 'ko-KR' },
  currency: { type: String, default: '' },

  badgeText: { type: String, default: '' },
  // 'neutral' | 'success' | 'danger' | 'info' | 'warning'
  badgeVariant: { type: String, default: 'neutral' },

  /* BaseCard 전달용 (옵션) */
  variant: { type: String, default: 'outlined' },
  pad: { type: String, default: 'lg' },
  radius: { type: String, default: 'xl' },
  clickable: { type: Boolean, default: false },
  to: { type: [String, Object], default: null },
  href: { type: String, default: '' },
  target: { type: String, default: '_self' },
  rel: { type: String, default: '' },
})
</script>

<style scoped>
/* BaseCard가 테두리/패딩을 담당 — 여긴 내부 레이아웃만 */
.content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: var(--metric-card-min-height, 130px);
}
.row.head{ display:flex; justify-content:space-between; align-items:center; gap:12px; }
.title{ font-weight:700; font-size:18px; color:#0f172a; letter-spacing:.01em; }
.subtitle{ color:#6b7280; }
.spacer{ flex:1 1 auto; }
.value{ display:flex; align-items:flex-end; }
</style>
