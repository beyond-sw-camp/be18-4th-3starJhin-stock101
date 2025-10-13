<!-- LikeButton.vue -->
<template>
  <BaseButton
    class="rb v-success"
    type="button"
    variant="ghost"
    size="sm"
    :loading="loading"
    :disabled="disabled || loading"
    :aria-pressed="String(!!modelValue)"
    :aria-label="ariaLabel || label || '좋아요'"
    @click="onClick"
  >
    <!-- 아이콘 슬롯: 기본은 하트 -->
    <slot name="icon">
      <svg viewBox="0 0 20 20" width="18" height="18" aria-hidden="true">
        <path
          d="M3.2 10.4 9.2 16.5a1 1 0 0 0 1.6 0l6.0-6.1A4 4 0 0 0 11 5.6l-.55.53-.45-.45A4 4 0 0 0 3.2 10.4z"
          fill="currentColor"
        />
      </svg>
    </slot>

    <span v-if="label" class="label">{{ label }}</span>
    <span v-if="showCount" class="count">{{ displayCount }}</span>
  </BaseButton>
</template>

<script setup>
import { computed } from 'vue'
import BaseButton from '../BaseButton.vue'

/**
 * @component LikeButton
 * @description
 * 좋아요(Like) 버튼 컴포넌트  
 * - BaseButton을 기반으로 하트 아이콘 + 라벨 + 카운트 표시  
 * - `v-model`로 좋아요 상태를 제어  
 * - 낙관적 업데이트(optimistic) 모드 지원 (UI 즉시 반영)  
 * - 로딩/비활성화 상태 및 접근성 속성 지원
 *
 * @example
 * <!-- 기본 사용 -->
 * <LikeButton v-model="liked" :count="10" />
 *
 * <!-- 카운트 숨김 -->
 * <LikeButton v-model="liked" :show-count="false" />
 *
 * <!-- 비관적 업데이트 모드 -->
 * <LikeButton v-model="liked" :count="serverCount" :optimistic="false" />
 *
 * <!-- 아이콘 교체 -->
 * <LikeButton v-model="liked">
 *   <template #icon>
 *     <StarIcon />
 *   </template>
 * </LikeButton>
 */

/**
 * @typedef {Object} LikeButtonProps
 * @property {boolean} [modelValue=false] - 좋아요 상태 (v-model)
 * @property {number} [count=0] - 서버에서 내려온 좋아요 수
 * @property {boolean} [optimistic=true] - 낙관적 업데이트 여부 (true면 즉시 +1 표시)
 * @property {boolean} [showCount=true] - 좋아요 수 표시 여부
 * @property {string} [label='좋아요'] - 버튼 라벨
 * @property {string} [ariaLabel=''] - 접근성용 aria-label
 * @property {boolean} [loading=false] - 로딩 상태
 * @property {boolean} [disabled=false] - 비활성화 상태
 */
const props = defineProps({
  modelValue: { type: Boolean, default: false }, // v-model:liked
  count: { type: Number, default: 0 },
  optimistic: { type: Boolean, default: true },
  showCount: { type: Boolean, default: true },

  label: { type: String, default: '좋아요' },
  ariaLabel: { type: String, default: '' },

  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
})

/**
 * @event update:modelValue
 * @description v-model 양방향 바인딩 이벤트
 * @property {boolean} value - 변경된 좋아요 상태
 *
 * @event toggle
 * @description 좋아요 상태가 토글될 때 발생
 * @property {boolean} value - 새로운 상태
 *
 * @event click
 * @description 클릭 이벤트 (항상 발생)
 */
const emit = defineEmits(['update:modelValue', 'toggle', 'click'])

/**
 * 화면에 표시할 카운트
 * - optimistic=true → UI 즉시 반영 (count + modelValue)
 * - optimistic=false → 서버 값 그대로 표시
 */
const displayCount = computed(() => {
  if (!props.optimistic) return props.count
  return props.count + (props.modelValue ? 1 : 0)
})

/**
 * 클릭 핸들러
 * - click 이벤트 발생
 * - modelValue 반전 → update:modelValue emit
 * - toggle 이벤트 emit
 */
function onClick() {
  emit('click')
  const next = !props.modelValue
  emit('update:modelValue', next)
  emit('toggle', next)
}
</script>

<style scoped>
/* Reaction 버튼 공통 톤 */
.rb {
  --btn-bg: transparent;
  --btn-border: transparent;
  --btn-fg: #6b7280;
  padding: 6px 8px;
  gap: 6px;
  border-radius: 8px;
}

.rb:hover { background: #f3f4f6; }
.rb:where(.is-disabled) { opacity: .6; cursor: not-allowed; }

/* 선택(pressed) 상태 색상 */
.v-success.on,
.rb[aria-pressed="true"] { color: #16a34a; }

.count { min-width: 1ch; }
</style>
