<template>
  <button type="button" class="base-back-button" @click="$emit('click')">
    <svg class="base-back-button__icon" width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        d="M6.667 3.333L2 8l4.667 4.667M2.667 8h11.333"
        stroke="currentColor"
        stroke-width="1.6"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </svg>
    <span class="base-back-button__label"><slot>돌아가기</slot></span>
  </button>
</template>

<script setup>
defineEmits(['click'])
</script>

<style scoped>
.base-back-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid #d1d5db;
  background: #ffffff;
  color: #4b5563;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}

.base-back-button:hover {
  background-color: #f3f4f6;
  border-color: #cbd5f5;
  color: #111827;
}

.base-back-button__icon {
  flex-shrink: 0;
}

.base-back-button__label {
  line-height: 1;
}
</style>
