<template>
  <article class="prediction-card">
    <div class="prediction-card__header">
      <span class="prediction-card__badge" :class="badgeClass">{{ prediction.sentimentLabel }}</span>
      <div class="prediction-card__meta">
        <span class="prediction-card__author">{{ prediction.author }}</span>
        <span class="prediction-card__timestamp">{{ formattedDate }}</span>
      </div>
    </div>
    <p class="prediction-card__body">
      {{ prediction.content }}
    </p>
    <footer class="prediction-card__footer">
      <span class="prediction-card__stat">❤ {{ prediction.likes }}</span>
      <span class="prediction-card__stat">💬 {{ prediction.comments }}</span>
      <span class="prediction-card__stat">{{ prediction.prediction === 'UP' ? 'UP' : 'DOWN' }}</span>
    </footer>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  prediction: {
    type: Object,
    required: true,
  },
})

const formattedDate = computed(() => {
  const value = props.prediction.predictedAt
  if (!(value instanceof Date)) {
    return new Date(value).toLocaleString('ko-KR', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }
  return value.toLocaleString('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
})

const badgeClass = computed(() => {
  if (props.prediction.prediction === 'UP') {
    return 'prediction-card__badge--positive'
  }
  if (props.prediction.prediction === 'DOWN') {
    return 'prediction-card__badge--negative'
  }
  return 'prediction-card__badge--neutral'
})
</script>

<style scoped>
.prediction-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 24px;
  border-radius: 18px;
  border: 1px solid #e5e7eb;
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.05);
}

.prediction-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.prediction-card__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.prediction-card__badge--positive {
  background-color: rgba(34, 197, 94, 0.12);
  color: #22c55e;
}

.prediction-card__badge--negative {
  background-color: rgba(239, 68, 68, 0.12);
  color: #ef4444;
}

.prediction-card__badge--neutral {
  background-color: rgba(107, 114, 128, 0.12);
  color: #6b7280;
}

.prediction-card__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #6b7280;
  font-size: 13px;
}

.prediction-card__author {
  font-weight: 600;
  color: #111827;
}

.prediction-card__timestamp {
  font-variant-numeric: tabular-nums;
}

.prediction-card__body {
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
  color: #374151;
}

.prediction-card__footer {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #6b7280;
}
</style>
