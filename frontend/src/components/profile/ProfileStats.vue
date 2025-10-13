<template>
  <div class="profile-stats">
    <div
      v-for="card in cards"
      :key="card.key"
      class="profile-stats__card"
      :class="{
        'profile-stats__card--active': activeFilter === card.filterKey,
        'profile-stats__card--clickable': card.filterKey !== null
      }"
      @click="card.filterKey && $emit('filter-change', card.filterKey)"
    >
      <span class="profile-stats__label">{{ card.label }}</span>
      <span class="profile-stats__value" :class="card.accent">{{ card.value }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  summary: {
    type: Object,
    required: true,
    validator(value) {
      return (
        typeof value.total === 'number' &&
        typeof value.success === 'number' &&
        typeof value.failure === 'number' &&
        typeof value.pending === 'number' &&
        typeof value.accuracy === 'number'
      )
    },
  },
  activeFilter: {
    type: String,
    default: 'all',
  },
})

defineEmits(['filter-change'])

const cards = computed(() => [
  { key: 'total', label: '전체', value: props.summary.total, accent: 'accent-neutral', filterKey: 'all' },
  { key: 'success', label: '예측 성공', value: props.summary.success, accent: 'accent-positive', filterKey: 'SUCCESS' },
  { key: 'failure', label: '예측 실패', value: props.summary.failure, accent: 'accent-negative', filterKey: 'FAILURE' },
  { key: 'pending', label: '결과 대기', value: props.summary.pending, accent: 'accent-info', filterKey: 'PENDING' },
  {
    key: 'accuracy',
    label: '정확도',
    value: `${props.summary.accuracy.toFixed(0)}%`,
    accent: 'accent-neutral',
    filterKey: null, // 정확도는 필터로 사용하지 않음
  },
])
</script>

<style scoped>
.profile-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
  width: 100%;
}

.profile-stats__card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 20px 12px;
  border: 1px solid #e6e8ec;
  border-radius: 16px;
  background-color: #fff;
  text-align: center;
  box-shadow: 0 3px 8px rgba(15, 23, 42, 0.04);
  transition: all 0.2s ease;
}

.profile-stats__card--clickable {
  cursor: pointer;
}

.profile-stats__card--clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.08);
  border-color: #3b82f6;
}

.profile-stats__card--active {
  border-color: #3b82f6;
  background-color: #eff6ff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.profile-stats__card--active .profile-stats__label {
  color: #3b82f6;
}

.profile-stats__label {
  font-size: 14px;
  color: #697489;
}

.profile-stats__value {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
}

.profile-stats__value.accent-positive {
  color: #22c55e;
}

.profile-stats__value.accent-negative {
  color: #ef4444;
}

.profile-stats__value.accent-info {
  color: #2563eb;
}
</style>
