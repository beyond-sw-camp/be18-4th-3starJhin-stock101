<template>
  <component
    :is="isRouter ? 'RouterLink' : (href ? 'a' : as)"
    class="base-card"
    :class="[
      `v-${variant}`, 
      `p-${pad}`, 
      `r-${radius}`,
      { 'is-clickable': isClickable, 'is-disabled': disabled }
    ]"
    :to="(isRouter && !disabled) ? to : undefined"
    :href="(!isRouter && !disabled) ? href : undefined"
    :target="(!isRouter && !disabled) ? target : undefined"
    :rel="finalRel"
    :aria-disabled="disabled || undefined"
    :tabindex="disabled ? -1 : (isClickable ? 0 : undefined)"
    v-bind="$attrs"
  >
    <!-- 미디어 영역 (예: 아바타, 이미지) -->
    <slot name="media" />

    <!-- 헤더 -->
    <div v-if="$slots.header" class="card__header">
      <slot name="header" />
    </div>

    <!-- 본문 -->
    <div class="card__body">
      <slot />
    </div>

    <!-- 푸터 -->
    <div v-if="$slots.footer" class="card__footer">
      <slot name="footer" />
    </div>
  </component>
</template>

<script setup>
import { computed } from 'vue'

/**
 * @component BaseCard
 * @description
 * 범용 카드 컴포넌트  
 * - 다양한 variant(외곽선, 그림자, 소프트, 고스트) 지원  
 * - padding, radius 옵션 제공  
 * - 클릭 가능 상태(clickable)와 링크/라우터 기능 지원  
 * - header, body, footer 슬롯 구조로 콘텐츠 구성 가능  
 *
 * @example
 * <!-- 단순 카드 -->
 * <BaseCard variant="outlined" pad="md">
 *   내용
 * </BaseCard>
 *
 * <!-- 링크 카드 -->
 * <BaseCard href="https://example.com" variant="elevated" clickable>
 *   외부 링크 카드
 * </BaseCard>
 *
 * <!-- 라우터 카드 -->
 * <BaseCard :to="{ name: 'profile' }" variant="soft">
 *   프로필 카드
 * </BaseCard>
 *
 * <!-- 슬롯 활용 -->
 * <BaseCard variant="ghost">
 *   <template #header>
 *     <Avatar /> <span>타이틀</span>
 *   </template>
 *   <p>본문 내용</p>
 *   <template #footer>
 *     <Button>액션</Button>
 *   </template>
 * </BaseCard>
 */

/**
 * @typedef {Object} BaseCardProps
 * @property {string} [as='div'] - 기본 태그 (div, section, article 등)
 * @property {'outlined'|'elevated'|'soft'|'ghost'} [variant='outlined'] - 카드 스타일
 * @property {'none'|'sm'|'md'|'lg'} [pad='md'] - 내부 여백 크기
 * @property {'md'|'lg'|'xl'|'full'} [radius='xl'] - 모서리 둥글기
 * @property {boolean} [clickable=false] - 클릭 가능 여부 (hover/active 효과 포함)
 * @property {boolean} [disabled=false] - 비활성화 여부
 * @property {string|object|null} [to=null] - router-link 목적지
 * @property {string} [href=''] - a 태그 링크
 * @property {string} [target='_self'] - a 태그 target
 * @property {string} [rel=''] - a 태그 rel (target="_blank"일 경우 기본값 자동 추가)
 */
const props = defineProps({
  as: { type: String, default: 'div' },             // section|div|article…
  variant: { type: String, default: 'outlined' },   // outlined | elevated | soft | ghost
  pad: { type: String, default: 'md' },             // none | sm | md | lg
  radius: { type: String, default: 'xl' },          // md | lg | xl | full
  clickable: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },

  // 링크/라우팅
  to: { type: [String, Object], default: null },
  href: { type: String, default: '' },
  target: { type: String, default: '_self' },
  rel: { type: String, default: '' },
})

/**
 * @slot media - 카드 상단/좌측 미디어 콘텐츠 (아바타, 이미지 등)
 * @slot header - 카드 헤더 콘텐츠
 * @slot default - 카드 본문 콘텐츠
 * @slot footer - 카드 푸터 콘텐츠
 */

/**
 * 라우터 링크 여부
 * @type {import('vue').ComputedRef<boolean>}
 */
const isRouter = computed(() => !!props.to && !props.href)

/**
 * 클릭 가능 여부 (clickable prop, router-link, href 기준)
 * @type {import('vue').ComputedRef<boolean>}
 */
const isClickable = computed(() => props.clickable || isRouter.value || !!props.href)

/**
 * rel 속성 최종값 계산
 * - rel이 지정되지 않았고 target="_blank"인 경우 "noopener noreferrer" 기본 적용
 * @type {import('vue').ComputedRef<string|undefined>}
 */
const finalRel = computed(() => {
  if (props.rel) return props.rel
  if (!isRouter.value && props.href && props.target === '_blank') {
    return 'noopener noreferrer'
  }
  return undefined
})
</script>

<style scoped>
.base-card {
  --bg: #fff;
  --fg: #111827;
  --bd: #cfd4dc;
  --ring: #3b82f633;
  --shadow: 0 1px 2px rgba(0, 0, 0, .06), 0 1px 1px rgba(0, 0, 0, .04);

  background: var(--bg);
  color: var(--fg);
  border: 1px solid var(--bd);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  text-decoration: none;
  transition:
    box-shadow .15s ease,
    transform .15s ease,
    border-color .15s ease,
    background .15s ease;
}

/* Variants */
.v-elevated { box-shadow: var(--shadow); border-color: transparent; }
.v-soft { background: #f8fafc; border-color: #e5e7eb; }
.v-ghost { background: transparent; border-color: transparent; }

/* Padding */
.p-none { padding: 0; }
.p-sm { padding: 10px; }
.p-md { padding: 16px; }
.p-lg { padding: 24px; }

/* Radius */
.r-md { border-radius: 12px; }
.r-lg { border-radius: 16px; }
.r-xl { border-radius: 18px; }
.r-full { border-radius: 999px; }

/* States */
.base-card.is-clickable { cursor: pointer; }
.base-card.is-clickable:hover { box-shadow: var(--shadow); transform: translateY(-1px); }
.base-card.is-clickable:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--ring), var(--shadow);
}
.base-card.is-clickable:active { transform: translateY(0); }
.base-card.is-disabled { opacity: .6; pointer-events: none; }

/* Slots */
.card__header { margin-bottom: 8px; display: flex; align-items: center; gap: 8px; }
.card__body { min-width: 0; }
.card__footer { margin-top: 12px; display: flex; align-items: center; gap: 8px; justify-content: space-between; }
</style>
