<!-- CommentButton.vue -->
<template>
  <BaseButton
    class="rb v-neutral"
    type="button"
    variant="ghost"
    size="sm"
    :loading="loading"
    :disabled="disabled || loading"
    :aria-label="ariaLabel || label || '댓글'"
    @click="$emit('click')"
  >
    <!-- 아이콘 슬롯: 사용자 지정 없으면 기본 말풍선 아이콘 표시 -->
    <slot name="icon">
      <svg viewBox="0 0 20 20" width="18" height="18" aria-hidden="true">
        <path d="M3 3h14v10H8l-5 4V3z" fill="currentColor"/>
      </svg>
    </slot>

    <!-- 라벨 표시 -->
    <span v-if="label" class="label">{{ label }}</span>
    <!-- 댓글 수 표시 -->
    <span v-if="showCount" class="count">{{ count }}</span>
  </BaseButton>
</template>

<script setup>
import BaseButton from '../BaseButton.vue'

/**
 * @component CommentButton
 * @description 
 * 댓글 버튼 컴포넌트  
 * - BaseButton을 기반으로 아이콘, 라벨, 댓글 수(count)를 함께 표시  
 * - 아이콘 슬롯을 지원하며 기본 말풍선 아이콘 제공  
 * - 로딩/비활성화 상태 관리 및 접근성(aria-label) 지원
 *
 * @example
 * <!-- 기본 사용 -->
 * <CommentButton :count="12" @click="onClick" />
 *
 * <!-- 라벨과 함께 -->
 * <CommentButton label="댓글 달기" :count="5" />
 *
 * <!-- 아이콘 교체 -->
 * <CommentButton>
 *   <template #icon>
 *     <MyCustomIcon />
 *   </template>
 * </CommentButton>
 *
 * <!-- 로딩 상태 -->
 * <CommentButton :loading="true" />
 */

/**
 * @typedef {Object} CommentButtonProps
 * @property {number} [count=0] - 댓글 수
 * @property {string} [label='댓글'] - 버튼에 표시할 라벨 텍스트
 * @property {string} [ariaLabel=''] - 접근성용 aria-label (없으면 label 또는 '댓글' 사용)
 * @property {boolean} [showCount=true] - 댓글 수를 표시할지 여부
 * @property {boolean} [loading=false] - 로딩 상태 (스피너 표시 + 클릭 차단)
 * @property {boolean} [disabled=false] - 버튼 비활성화 여부
 */
defineProps({
  count: { type: Number, default: 0 },
  label: { type: String, default: '댓글' },
  ariaLabel: { type: String, default: '' },
  showCount: { type: Boolean, default: true },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})

/**
 * @event click
 * @description 버튼 클릭 시 발생 (disabled/loading 상태에서는 차단됨)
 */
defineEmits(['click'])
</script>

<style scoped>
/* 댓글 버튼 스타일 - BaseButton 위에 오버레이하는 톤 다운 테마 */
.rb{
  --btn-bg: transparent;
  --btn-border: transparent;
  --btn-fg: #6b7280;
  padding: 6px 8px;
  gap: 6px;
  border-radius: 8px;
}

/* hover 효과 */
.rb:hover{ background:#f3f4f6; }

/* 비활성화 상태 */
.rb:where(.is-disabled){ opacity:.6; cursor:not-allowed; }

/* 기본 텍스트 컬러 */
.v-neutral{ color:#6b7280; }

/* 카운트 최소 폭 보장 */
.count{ min-width: 1ch; }
</style>