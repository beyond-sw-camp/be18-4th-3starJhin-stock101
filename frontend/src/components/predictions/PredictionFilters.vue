<template>
  <div class="prediction-filters">
    <label class="prediction-filters__label" for="prediction-result-filter">결과</label>
    <select
      id="prediction-result-filter"
      class="prediction-filters__select"
      :value="modelValue"
      @change="$emit('update:modelValue', $event.target.value)"
    >
      <option value="all">전체</option>
      <option value="SUCCESS">예측 성공</option>
      <option value="FAILURE">예측 실패</option>
      <option value="PENDING">결과 대기</option>
    </select>
  </div>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: 'all',
  },
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.prediction-filters {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.prediction-filters__label {
  font-size: 14px;
  color: #4b5563;
}

.prediction-filters__select {
  min-width: 140px;
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid #d1d5db;
  background-color: #fff;
  font-size: 14px;
}
</style>
