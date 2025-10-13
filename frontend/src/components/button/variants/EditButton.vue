<template>
  <BaseButton
    variant="primary"
    size="lg"
    v-bind="$attrs"
    @click="$emit('click', $event)"
  >
    수정하기
  </BaseButton>
</template>

<script setup>
import BaseButton from '../BaseButton.vue'

/**
 * @component EditButton
 * @description
 * "수정하기" 라벨을 가진 기본 제공 버튼 컴포넌트  
 * - BaseButton을 감싸서 일관된 스타일과 크기를 적용  
 * - 부모에서 전달한 속성($attrs)을 그대로 BaseButton에 전달  
 * - 클릭 시 click 이벤트를 그대로 내보냄
 *
 * @example
 * <!-- 기본 사용 -->
 * <EditButton @click="onEdit" />
 *
 * <!-- 추가 속성 전달 -->
 * <EditButton :disabled="isLocked" aria-label="편집하기" />
 */

/**
 * @event click
 * @description 버튼 클릭 시 발생하는 이벤트
 * @property {MouseEvent} e - 네이티브 클릭 이벤트
 */
defineEmits(['click'])
</script>
