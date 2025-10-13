<!-- src/components/card/CountCard.vue -->
<template>
  <BaseCard
    class="count-card"
    variant="outlined"
    :pad="pad"
    :radius="radius"
    :clickable="!!to || !!href"
    :to="to"
    :href="href"
    :target="target"
  >
    <div class="value">
      <slot name="value">{{ formatted }}</slot>
    </div>
    <div class="label">
      <slot>{{ label }}</slot>
    </div>
  </BaseCard>
</template>

<script setup>
import { computed } from 'vue'
import BaseCard from '../BaseCard.vue'

/**
 * @component CountCard
 * @description
 * 수치와 라벨을 표시하는 카드 컴포넌트  
 * - 숫자를 locale/decimals에 맞춰 포맷팅해서 표시  
 * - `value`/`label` 기본 props 제공, 필요 시 슬롯으로 커스텀 가능  
 * - 카드 자체를 링크(`to`, `href`)로 만들 수 있음  
 * - pad, radius 옵션으로 레이아웃 커스터마이징 가능
 *
 * @slot value  표시할 값 영역 (기본은 포맷된 `value`)
 * @slot default 레이블 영역 (기본은 `label`)
 *
 * @example
 * <!-- 기본 사용 -->
 * <CountCard :value="1234" label="팔로워" />
 *
 * <!-- 소수점 표시 -->
 * <CountCard :value="3.14159" :decimals="2" label="평균 점수" />
 *
 * <!-- 링크 카드 -->
 * <CountCard :value="42" label="게시글" :to="{ name: 'posts' }" />
 *
 * <!-- 슬롯 커스터마이즈 -->
 * <CountCard :value="99">
 *   <template #value>
 *     <StarIcon /> 99
 *   </template>
 *   <template #default>
 *     특별한 수치
 *   </template>
 * </CountCard>
 */

/**
 * @typedef {Object} CountCardProps
 * @property {number|string} [value=0] - 표시할 값
 * @property {string} [label=''] - 라벨 텍스트
 * @property {number} [decimals=0] - 소수점 자리수
 * @property {string} [locale='ko-KR'] - 숫자 포맷팅에 사용할 로케일
 * @property {string|object|null} [to=null] - router-link 목적지
 * @property {string} [href=''] - a 태그 링크
 * @property {string} [target='_self'] - a 태그 target
 * @property {string} [pad='lg'] - 카드 padding 크기
 * @property {string} [radius='xl'] - 카드 모서리 둥글기
 */
const props = defineProps({
  value: { type: [Number, String], default: 0 },
  label: { type: String, default: '' },
  decimals: { type: Number, default: 0 },
  locale: { type: String, default: 'ko-KR' },

  // 링크 옵션(선택)
  to: { type: [String, Object], default: null },
  href: { type: String, default: '' },
  target: { type: String, default: '_self' },

  // 카드 패딩/라운드 커스터마이즈(선택)
  pad: { type: String, default: 'lg' },
  radius: { type: String, default: 'xl' },
})

/**
 * 표시할 숫자 포맷팅
 * - value가 숫자이면 Intl.NumberFormat으로 로케일/소수점 적용
 * - 숫자가 아니면 문자열로 그대로 출력
 */
const formatted = computed(() => {
  const n = Number(props.value)
  if (Number.isFinite(n)) {
    return new Intl.NumberFormat(props.locale, {
      minimumFractionDigits: props.decimals,
      maximumFractionDigits: props.decimals
    }).format(n)
  }
  return String(props.value ?? '')
})
</script>

<style scoped>
.count-card{
  display:flex; flex-direction:column; justify-content:center; align-items:center;
  text-align:center; min-height:86px;
}
.value{ font-weight:700; font-size:22px; line-height:1.1; color:#111827; }
.label{ margin-top:6px; font-size:14px; color:#9ca3af; }
</style>
