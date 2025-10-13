<template>
  <component
    :is="as"
    class="txt"
    :class="[
      `txt--variant-${variant}`,
      `txt--size-${size}`,
      `txt--weight-${weight}`,
      `txt--align-${align}`,
      underline && 'txt--underline'
    ]"
    :style="styleObj"
  >
    <slot />
  </component>
</template>

<script setup>
import { computed } from 'vue'

/**
 * @component Text
 * @description
 * 재사용 가능한 타이포그래피 컴포넌트.  
 * - 요소 태그(as), 톤(variant), 크기(size), 굵기(weight), 정렬(align) 제어  
 * - 밑줄, 컬러 오버라이드, 한 줄/N줄 말줄임(truncate) 지원
 *
 * @slot default 텍스트/인라인 콘텐츠
 *
 * @example
 * <!-- 기본 본문 -->
 * <Text>안녕하세요</Text>
 *
 * <!-- 링크 톤 + 밑줄 -->
 * <Text variant="link" underline @click="go()">자세히 보기</Text>
 *
 * <!-- 서브텍스트(회색), 작게 -->
 * <Text variant="muted" size="sm">업데이트: 2025-09-15</Text>
 *
 * <!-- 제목처럼 쓰기 -->
 * <Text as="h3" size="xl" weight="bold">대시보드</Text>
 *
 * <!-- 한 줄 말줄임 -->
 * <Text :truncate="true" style="max-width:220px">아주 긴 제목이 여기에 들어갑니다…</Text>
 *
 * <!-- 2줄 말줄임 -->
 * <Text :truncate="2" size="sm" variant="muted" style="max-width:260px">
 *   멀티라인 콘텐츠가 길 경우 두 줄까지만 보여주고 말줄임 처리합니다.
 * </Text>
 */

/**
 * @typedef {Object} TextProps
 * @property {'span'|'p'|'h1'|'h2'|'h3'|'h4'|'h5'|'h6'|string} [as='span'] - 렌더링 태그
 * @property {'body'|'muted'|'caption'|'overline'|'link'|string} [variant='body'] - 텍스트 톤
 * @property {'xs'|'sm'|'md'|'lg'|'xl'|string} [size='md'] - 글자 크기
 * @property {'light'|'normal'|'medium'|'semibold'|'bold'|string} [weight='normal'] - 글자 굵기
 * @property {'start'|'center'|'end'|'justify'|string} [align='start'] - 정렬
 * @property {boolean} [underline=false] - 밑줄 표시
 * @property {string} [color=''] - 인라인 색상 오버라이드(필요 시)
 * @property {boolean|number} [truncate=false] - 말줄임: true=1줄, 숫자=N줄
 */
const props = defineProps({
  as: { type: String, default: 'span' },          // 'span' | 'p' | 'h1'~'h6' 등
  variant: { type: String, default: 'body' },      // 'body' | 'muted' | 'caption' | 'overline' | 'link'
  size: { type: String, default: 'md' },           // 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  weight: { type: String, default: 'normal' },     // 'light' | 'normal' | 'medium' | 'semibold' | 'bold'
  align: { type: String, default: 'start' },       // 'start' | 'center' | 'end' | 'justify'
  underline: { type: Boolean, default: false },    // 필요 시 밑줄
  color: { type: String, default: '' },            // 임시 색상 오버라이드
  truncate: { type: [Boolean, Number], default: false } // true: 한 줄, 숫자: N줄 말줄임
})

/**
 * 인라인 스타일 계산
 * - color 오버라이드 적용
 * - truncate=true → 한 줄 말줄임
 * - truncate=숫자 → WebKit 2줄+ 말줄임
 * @type {import('vue').ComputedRef<Record<string,string>>}
 */
const styleObj = computed(() => {
  const s = {}
  if (props.color) s.color = props.color
  if (props.truncate === true) {
    Object.assign(s, { whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' })
  } else if (typeof props.truncate === 'number' && props.truncate > 1) {
    Object.assign(s, {
      display: '-webkit-box',
      WebkitBoxOrient: 'vertical',
      WebkitLineClamp: String(props.truncate),
      overflow: 'hidden'
    })
  }
  return s
})
</script>

<style scoped>
/* 기본 색/톤 */
.txt--variant-body     { color:#111827; }
.txt--variant-muted    { color:#6b7280; }
.txt--variant-caption  { color:#6b7280; text-transform:none; font-size:.875rem; }
.txt--variant-overline { color:#6b7280; text-transform:uppercase; letter-spacing:.06em; font-size:.75rem; }
.txt--variant-link     { color:#2563eb; text-decoration:underline; cursor:pointer; }

/* 크기 */
.txt--size-xs{ font-size:.75rem; line-height:1.1; }
.txt--size-sm{ font-size:.875rem; line-height:1.25; }
.txt--size-md{ font-size:1rem; line-height:1.4; }
.txt--size-lg{ font-size:1.125rem; line-height:1.5; }
.txt--size-xl{ font-size:1.25rem; line-height:1.6; }

/* 두께 */
.txt--weight-light{ font-weight:300; }
.txt--weight-normal{ font-weight:400; }
.txt--weight-medium{ font-weight:500; }
.txt--weight-semibold{ font-weight:600; }
.txt--weight-bold{ font-weight:700; }

/* 정렬 */
.txt--align-start{ text-align:left; }
.txt--align-center{ text-align:center; }
.txt--align-end{ text-align:right; }
.txt--align-justify{ text-align:justify; }

.txt--underline{ text-decoration: underline; }
</style>
