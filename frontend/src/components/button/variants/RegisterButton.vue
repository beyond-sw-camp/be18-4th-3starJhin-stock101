<template>
  <BaseButton
    variant="primary"
    size="lg"
    v-bind="$attrs"
    @click="$emit('click', $event)"
  >
    등록하기
  </BaseButton>
</template>

<script setup>
import BaseButton from '../BaseButton.vue'

/**
 * @component RegisterButton
 * @description
 * "등록하기" 라벨을 가진 전용 버튼 컴포넌트  
 * - BaseButton을 감싸서 일관된 스타일(Primary, Large)을 적용  
 * - 부모로부터 전달된 속성($attrs)을 그대로 BaseButton에 전달  
 * - 클릭 시 `click` 이벤트를 그대로 방출
 *
 * @example
 * <!-- 기본 사용 -->
 * <RegisterButton @click="onRegister" />
 *
 * <!-- 추가 속성 전달 -->
 * <RegisterButton :disabled="isSubmitting" aria-label="등록" />
 */

/**
 * @event click
 * @description 버튼 클릭 시 발생하는 이벤트
 * @property {MouseEvent} e - 네이티브 클릭 이벤트 객체
 */
defineEmits(['click'])
</script>
