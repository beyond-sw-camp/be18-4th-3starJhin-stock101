<template>
  <div class="opinion-selector">
    <button
      v-for="option in options"
      :key="option.value"
      type="button"
      class="opinion-selector__button"
      :class="{
        'opinion-selector__button--selected': modelValue === option.value,
        'opinion-selector__button--positive': option.tone === 'positive' && modelValue === option.value,
        'opinion-selector__button--negative': option.tone === 'negative' && modelValue === option.value,
        'opinion-selector__button--neutral': option.tone === 'neutral' && modelValue === option.value,
      }"
      :disabled="disabled"
      @click="handleClick(option.value, $event)"
    >
      {{ option.label }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  disabled: {
    type: Boolean,
    default: false,
  },
})

const options = [
  { label: 'Strong Sell', value: 'Strong Sell', tone: 'negative' },
  { label: 'Sell', value: 'Sell', tone: 'negative' },
  { label: 'Hold', value: 'Hold', tone: 'neutral' },
  { label: 'Buy', value: 'Buy', tone: 'positive' },
  { label: 'Strong Buy', value: 'Strong Buy', tone: 'positive' },
]
</script>

<script>
export default {
  methods: {
    handleClick(value, event) {
      if (this.disabled) {
        this.$emit('login-required')
        return
      }
      this.$emit('update:modelValue', value)
    },
  },
}
</script>

<style scoped>
.opinion-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.opinion-selector__button {
  min-width: 88px;
  padding: 10px 16px;
  border-radius: 999px;
  border: 1px solid #d1d5db;
  background-color: #ffffff;
  color: #1f2937;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.opinion-selector__button--selected {
  border-color: #1f2937;
  color: #1f2937;
}

.opinion-selector__button--positive {
  background-color: #d8f5df;
  border-color: #1fb673;
  color: #0f8c4f;
}

.opinion-selector__button--negative {
  background-color: #ffd9de;
  border-color: #f05665;
  color: #cb2943;
}

.opinion-selector__button--neutral {
  background-color: #fff4b8;
  border-color: #f5c542;
  color: #946c00;
}

.opinion-selector__button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
</style>
