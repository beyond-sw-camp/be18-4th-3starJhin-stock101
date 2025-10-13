<template>
  <nav class="profile-tabs" role="tablist">
    <button
      v-for="tab in tabs"
      :key="tab.value"
      type="button"
      role="tab"
      :aria-selected="modelValue === tab.value"
      :class="['profile-tabs__item', { 'profile-tabs__item--active': modelValue === tab.value }]"
      @click="$emit('update:modelValue', tab.value)"
    >
      {{ tab.label }}
    </button>
  </nav>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    required: true,
  },
  user: {
    type: Object,
    default: () => ({}),
  },
  tabs: {
    type: Array,
    default: () => [
      { label: '예측 목록', value: 'predictions' },
      { label: '게시물 목록', value: 'posts' },
    ],
  },
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.profile-tabs {
  display: inline-flex;
  gap: 32px;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 24px;
}

.profile-tabs__item {
  position: relative;
  padding: 12px 4px;
  font-size: 16px;
  font-weight: 500;
  color: #6b7280;
  background: none;
  border: none;
  cursor: pointer;
}

.profile-tabs__item--active {
  color: #111827;
}

.profile-tabs__item--active::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -1px;
  width: 100%;
  height: 3px;
  border-radius: 999px;
  background-color: #2563eb;
}
</style>
