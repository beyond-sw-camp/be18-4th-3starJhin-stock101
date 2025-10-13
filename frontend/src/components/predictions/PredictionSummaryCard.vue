<template>
  <article class="prediction-summary" :class="cardClass">
    <div class="prediction-summary__status-indicator" :class="statusIndicatorClass">
      <!-- <div class="prediction-summary__status-icon">{{ statusIcon }}</div> -->
    </div>

    <div class="prediction-summary__content">
      <div class="prediction-summary__info">
        <h3 class="prediction-summary__symbol">{{ prediction.stockName }}</h3>
        <p class="prediction-summary__timestamp">{{ formattedDate }}</p>
      </div>

      <div class="prediction-summary__badges">
        <span class="prediction-summary__direction-badge" :class="directionBadgeClass">
          {{ prediction.prediction === 'UP' ? '↗ UP' : '↘ DOWN' }}
        </span>
        <span class="prediction-summary__result-badge" :class="resultBadgeClass">
          {{ resultText }}
        </span>
      </div>
    </div>
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
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }
  return value.toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
})

const directionBadgeClass = computed(() =>
  props.prediction.prediction === 'UP' ? 'prediction-summary__direction-badge--up' : 'prediction-summary__direction-badge--down',
)

const resultText = computed(() => {
  const result = props.prediction.result
  if (result === 'SUCCESS') return '성공'
  if (result === 'FAILURE') return '실패'
  if (result === 'PENDING' || result === null) return '대기'
  return '대기'
})

const resultBadgeClass = computed(() => {
  const result = props.prediction.result
  if (result === 'SUCCESS') return 'prediction-summary__result-badge--success'
  if (result === 'FAILURE') return 'prediction-summary__result-badge--failure'
  if (result === 'PENDING' || result === null) return 'prediction-summary__result-badge--pending'
  return 'prediction-summary__result-badge--pending'
})

const statusIcon = computed(() => {
  const result = props.prediction.result
  if (result === 'SUCCESS') return '✓'
  if (result === 'FAILURE') return '✕'
  if (result === 'PENDING' || result === null) return '⏳'
  return '⏳'
})

const statusIndicatorClass = computed(() => {
  const result = props.prediction.result
  if (result === 'SUCCESS') return 'prediction-summary__status-indicator--success'
  if (result === 'FAILURE') return 'prediction-summary__status-indicator--failure'
  if (result === 'PENDING' || result === null) return 'prediction-summary__status-indicator--pending'
  return 'prediction-summary__status-indicator--pending'
})

const cardClass = computed(() => {
  const result = props.prediction.result
  if (result === 'SUCCESS') return 'prediction-summary--success'
  if (result === 'FAILURE') return 'prediction-summary--failure'
  if (result === 'PENDING' || result === null) return 'prediction-summary--pending'
  return 'prediction-summary--pending'
})
</script>

<style scoped>
.prediction-summary {
  display: flex;
  align-items: stretch;
  padding: 0;
  border-radius: 16px;
  border: 2px solid #e5e7eb;
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
  overflow: hidden;
  transition: all 0.2s ease;
}

.prediction-summary:hover {
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.12);
  transform: translateY(-2px);
}

/* Card variants based on result */
.prediction-summary--success {
  border-color: rgba(34, 197, 94, 0.3);
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.02) 0%, #fff 100%);
}

.prediction-summary--failure {
  border-color: rgba(239, 68, 68, 0.3);
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.02) 0%, #fff 100%);
}

.prediction-summary--pending {
  border-color: rgba(107, 114, 128, 0.3);
  background: linear-gradient(135deg, rgba(107, 114, 128, 0.02) 0%, #fff 100%);
}

/* Status indicator strip */
.prediction-summary__status-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 4px;
  min-height: 100%;
  position: relative;
}

.prediction-summary__status-indicator--success {
  background: linear-gradient(180deg, #22c55e 0%, #16a34a 100%);
}

.prediction-summary__status-indicator--failure {
  background: linear-gradient(180deg, #ef4444 0%, #dc2626 100%);
}

.prediction-summary__status-indicator--pending {
  background: linear-gradient(180deg, #f59e0b 0%, #d97706 100%);
}

.prediction-summary__status-icon {
  position: absolute;
  left: -8px;
  background: inherit;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  color: white;
  border: 2px solid white;
}

/* Content area */
.prediction-summary__content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 22px;
  flex: 1;
}

.prediction-summary__info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.prediction-summary__symbol {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #111827;
}

.prediction-summary__timestamp {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

/* Badges container */
.prediction-summary__badges {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
}

/* Direction badge styles */
.prediction-summary__direction-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.5px;
  min-width: 60px;
  text-align: center;
}

.prediction-summary__direction-badge--up {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.15) 0%, rgba(34, 197, 94, 0.08) 100%);
  color: #15803d;
  border: 1px solid rgba(34, 197, 94, 0.2);
}

.prediction-summary__direction-badge--down {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.15) 0%, rgba(239, 68, 68, 0.08) 100%);
  color: #dc2626;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

/* Result badge styles */
.prediction-summary__result-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  min-width: 50px;
  text-align: center;
}

.prediction-summary__result-badge--success {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: white;
  box-shadow: 0 2px 4px rgba(34, 197, 94, 0.3);
}

.prediction-summary__result-badge--failure {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.3);
}

.prediction-summary__result-badge--pending {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  box-shadow: 0 2px 4px rgba(245, 158, 11, 0.3);
}

/* Responsive design */
@media (max-width: 768px) {
  .prediction-summary__content {
    padding: 16px 18px;
  }

  .prediction-summary__symbol {
    font-size: 16px;
  }

  .prediction-summary__badges {
    gap: 6px;
  }

  .prediction-summary__direction-badge,
  .prediction-summary__result-badge {
    font-size: 11px;
    padding: 4px 8px;
  }
}
</style>