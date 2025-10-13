<template>
  <!-- 
    동적 버튼 컴포넌트
    - props에 따라 <button>, <a>, <RouterLink> 로 렌더링 가능
    - variant, size, disabled, loading 상태 지원
    - 좌/우 아이콘 슬롯과 기본 슬롯 지원
  -->
  <component
    :is="tag"
    :class="classes"
    :disabled="isNativeDisabled"
    :aria-disabled="disabled || loading ? 'true' : undefined"
    :aria-busy="loading ? 'true' : undefined"
    v-bind="passThroughAttrs"
    @click="onClick"
  >
    <!-- 왼쪽 아이콘 슬롯 -->
    <slot name="iconLeft" />
    <!-- 기본 슬롯 (버튼 텍스트) -->
    <span v-if="$slots.default"><slot /></span>
    <!-- 오른쪽 아이콘 슬롯 -->
    <slot name="iconRight" />
    <!-- 로딩 상태일 때 스피너 표시 -->
    <span v-if="loading" class="spinner" aria-hidden="true"></span>
  </component>
</template>

<script setup>
import { computed, useAttrs } from 'vue'

/**
 * @component BaseButton
 * @description 
 * 범용 버튼 컴포넌트  
 * - 다양한 태그(`<button>`, `<a>`, `<RouterLink>`)로 렌더링 가능  
 * - variant, size, disabled, loading 상태 지원  
 * - 아이콘 슬롯(좌/우)과 기본 슬롯을 통해 확장성 제공  
 * - 접근성 속성(aria-*), 로딩 스피너 자동 처리
 *
 * @example
 * <!-- 기본 버튼 -->
 * <BaseButton variant="primary" size="md" @click="submitForm">
 *   저장
 * </BaseButton>
 *
 * <!-- 링크 버튼 -->
 * <BaseButton as="a" href="https://example.com" variant="secondary">
 *   외부 링크
 * </BaseButton>
 *
 * <!-- 라우터 링크 버튼 -->
 * <BaseButton as="router-link" :to="{ name: 'home' }" variant="ghost">
 *   홈으로
 * </BaseButton>
 *
 * <!-- 아이콘 포함 -->
 * <BaseButton>
 *   <template #iconLeft>
 *     <MyIcon />
 *   </template>
 *   확인
 * </BaseButton>
 */

/**
 * @typedef {Object} ButtonProps
 * @property {'primary'|'secondary'|'danger'|'ghost'} [variant='primary'] - 버튼 스타일
 * @property {'sm'|'md'|'lg'} [size='md'] - 버튼 크기
 * @property {boolean} [loading=false] - 로딩 상태 여부 (true이면 클릭 불가 + 스피너 표시)
 * @property {boolean} [disabled=false] - 비활성화 상태 여부
 * @property {'button'|'a'|'router-link'} [as='button'] - 어떤 태그로 렌더링할지
 * @property {string|object|null} [to=null] - router-link용 이동 경로
 * @property {string|null} [href=null] - a 태그용 링크 주소
 * @property {'button'|'submit'|'reset'} [type='button'] - button 태그일 때 type 속성
 */
const props = defineProps({
  variant: { type: String, default: 'primary' },
  size: { type: String, default: 'md' },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  as: { type: String, default: 'button' },
  to: { type: [String, Object], default: null },
  href: { type: String, default: null },
  type: { type: String, default: 'button' }
})

/**
 * @event click
 * @description 버튼이 disabled/loading 상태가 아닐 때 클릭되면 발생하는 이벤트
 * @property {MouseEvent} e - 네이티브 클릭 이벤트 객체
 */
const emit = defineEmits(['click'])

/**
 * 렌더링할 태그를 결정
 * - router-link → RouterLink
 * - a → a
 * - 기본 → button
 * @type {import('vue').ComputedRef<'button'|'a'|'RouterLink'>}
 */
const tag = computed(() => {
  if (props.as === 'router-link') return 'RouterLink'
  if (props.as === 'a') return 'a'
  return 'button'
})

/**
 * 네이티브 disabled 속성을 적용할지 여부
 * - button 태그에서만 사용 가능
 * - loading 상태일 때도 true로 처리
 * @type {import('vue').ComputedRef<boolean|undefined>}
 */
const isNativeDisabled = computed(() => 
  props.as === 'button' ? (props.disabled || props.loading) : undefined
)

/**
 * 버튼에 적용될 클래스 문자열
 * - 기본: btn
 * - variant: btn--primary, btn--secondary 등
 * - size: btn--sm, btn--md, btn--lg
 * - 상태: is-loading, is-disabled
 * @example
 * classes.value // "btn btn--primary btn--md"
 * @type {import('vue').ComputedRef<string>}
 */
const classes = computed(() => [
  'btn',
  `btn--${props.variant}`,
  `btn--${props.size}`,
  props.loading && 'is-loading',
  props.disabled && 'is-disabled'
].filter(Boolean).join(' '))

/**
 * 부모 컴포넌트로부터 전달된 속성
 */
const attrs = useAttrs()

/**
 * 실제 렌더링되는 태그에 맞춰 전달할 속성 결정
 * - <a> → href
 * - <router-link> → to
 * - <button> → type
 * @returns {Record<string, any>}
 */
const passThroughAttrs = computed(() => {
  const base = { ...attrs }
  if (props.as === 'a' && props.href) base.href = props.href
  if (props.as === 'router-link' && props.to) base.to = props.to
  if (props.as === 'button') base.type = props.type
  return base
})

/**
 * 클릭 이벤트 핸들러
 * - disabled 또는 loading 상태일 경우 클릭 막기
 * - 정상 클릭일 경우 "click" 이벤트 emit
 * @param {MouseEvent} e - 클릭 이벤트 객체
 */
function onClick(e) {
  if (props.disabled || props.loading) {
    e.preventDefault()
    e.stopPropagation()
    return
  }
  emit('click', e)
}
</script>

<style scoped>
/* 공통 버튼 스타일 */
.btn { display:inline-flex; align-items:center; gap:.5rem; padding:.5rem .9rem; border-radius:.5rem; border:1px solid transparent; cursor:pointer; }

/* variant 스타일 */
.btn--primary { background:#1f6feb; color:white; }
.btn--secondary { background:white; color:#1f2328; border-color:#d0d7de; }
.btn--danger { background:#d1242f; color:white; }
.btn--ghost { background:transparent; color:#1f2328; }

/* size 스타일 */
.btn--sm { padding:.35rem .65rem; font-size:.875rem; }
.btn--md { font-size:1rem; }
.btn--lg { padding:.65rem 1.1rem; font-size:1.125rem; }

/* 상태 스타일 */
.is-disabled { opacity:.6; cursor:not-allowed; }

/* 로딩 스피너 */
.spinner { width:1em; height:1em; border:.15em solid currentColor; border-right-color:transparent; border-radius:50%; animation:spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
