<template>
  <component
    :is="isRouter ? 'RouterLink' : 'a'"
    class="link-reset"
    :class="{ 'is-disabled': disabled }"
    :to="isRouter ? to : undefined"
    :href="!isRouter ? href : undefined"
    :target="!isRouter ? target : undefined"
    :rel="finalRel"
    :aria-disabled="disabled ? 'true' : undefined"
    :tabindex="disabled ? -1 : undefined"
    @click="onClick"
  >
    <!-- 텍스트 스타일은 전부 Text 컴포넌트가 책임 -->
    <Text
      :as="textAs"
      :variant="variant"
      :size="size"
      :weight="weight"
      :align="align"
      :underline="underline"
      :color="color || undefined"
    >
      <slot />
    </Text>
  </component>
</template>

<script setup>
import { computed } from 'vue'
import Text from '../BaseText.vue'

/**
 * @component LinkText
 * @description
 * 텍스트 스타일은 `Text` 컴포넌트에 위임하고, 네비게이션은 `RouterLink` 또는 `<a>`를 자동 선택해주는 링크 래퍼.  
 * - `to`가 있으면 내부 라우팅(`RouterLink`), 없고 `href`가 있으면 외부 링크(`<a>`)로 렌더링  
 * - `target="_blank"` 외부 링크일 때 기본적으로 `rel="noopener noreferrer"` 자동 부여  
 * - `disabled` 시 클릭 차단 및 포커스/탭 이동 방지  
 * - 텍스트 톤/크기/굵기/정렬/밑줄/색상은 `Text` props로 그대로 전달
 *
 * @slot default 링크 텍스트/인라인 콘텐츠
 *
 * @example
 * <!-- 내부 라우트 링크 -->
 * <LinkText :to="{ name: 'post', params: { id: 1 } }">자세히 보기</LinkText>
 *
 * <!-- 외부 링크 (새 창) -->
 * <LinkText href="https://example.com" target="_blank">공식 사이트</LinkText>
 *
 * <!-- 커스텀 타이포 -->
 * <LinkText href="/terms" variant="muted" size="sm" :underline="false">약관 보기</LinkText>
 *
 * <!-- 비활성화 -->
 * <LinkText :to="{ name: 'profile' }" :disabled="true">프로필</LinkText>
 */

/**
 * @typedef {Object} LinkTextProps
 * @property {string|object|null} [to=null] - 내부 라우팅용 목적지 (존재하면 RouterLink 사용)
 * @property {string|null} [href=null] - 외부 링크 URL (to가 없고 이 값이 있으면 <a> 사용)
 * @property {string|null} [target=null] - 외부 링크 target (예: '_blank')
 * @property {string|null} [rel=null] - a 태그 rel (미지정 + target='_blank'면 'noopener noreferrer' 자동 설정)
 * @property {boolean} [disabled=false] - 비활성화(클릭/포커스 차단)
 * @property {'span'|'p'|'h1'|'h2'|'h3'|'h4'|'h5'|'h6'|string} [textAs='span'] - Text 컴포넌트의 렌더 태그
 * @property {'body'|'muted'|'caption'|'overline'|'link'|string} [variant='link'] - 텍스트 톤
 * @property {'xs'|'sm'|'md'|'lg'|'xl'|string} [size='md'] - 크기
 * @property {'light'|'normal'|'medium'|'semibold'|'bold'|string} [weight='normal'] - 굵기
 * @property {'start'|'center'|'end'|'justify'|string} [align='start'] - 정렬
 * @property {boolean} [underline=true] - 밑줄 여부
 * @property {string} [color=''] - 색상 오버라이드
 */
const props = defineProps({
  // 네비게이션
  to: { type: [String, Object], default: null },   // 내부 라우팅
  href: { type: String, default: null },           // 외부 링크
  target: { type: String, default: null },
  rel: { type: String, default: null },
  disabled: { type: Boolean, default: false },

  // 텍스트(타이포) 속성 - Text로 그대로 전달
  textAs:   { type: String, default: 'span' },
  variant:  { type: String, default: 'link' },
  size:     { type: String, default: 'md' },
  weight:   { type: String, default: 'normal' },
  align:    { type: String, default: 'start' },
  underline:{ type: Boolean, default: true },
  color:    { type: String, default: '' }
})

/**
 * 내부/외부 링크 판단
 * - to가 있고 href가 없으면 RouterLink로 렌더링
 * @type {import('vue').ComputedRef<boolean>}
 */
const isRouter = computed(() => !!props.to && !props.href)

/**
 * 최종 rel 계산
 * - 사용자가 rel을 지정하면 우선
 * - 외부 링크 + target="_blank"이면 noopener noreferrer 기본 추가
 * @type {import('vue').ComputedRef<string|undefined>}
 */
const finalRel = computed(() => {
  if (props.rel) return props.rel
  if (!isRouter.value && props.target === '_blank') return 'noopener noreferrer'
  return undefined
})

/**
 * 클릭 핸들러
 * - disabled일 때 네비게이션 차단
 */
function onClick(e) {
  if (props.disabled) {
    e.preventDefault()
    e.stopPropagation()
  }
}
</script>

<style scoped>
/* 앵커 기본 스타일 초기화: 시각은 Text가 담당 */
.link-reset {
  color: inherit;
  text-decoration: none;
  cursor: pointer;
}
.link-reset:focus-visible {
  outline: 2px solid rgba(37,99,235,.5);
  outline-offset: 2px;
}
.link-reset.is-disabled {
  pointer-events: none;
  cursor: not-allowed;
}
</style>
