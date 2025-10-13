<template>
  <div class="toast-host" aria-live="polite" aria-atomic="true">
    <transition-group name="toast" tag="div">
      <div v-for="toast in toasts" :key="toast.id" class="toast-host__toast" :class="`toast-host__toast--${toast.tone}`">
        {{ toast.message }}
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { storeToRefs } from 'pinia'
import { useToastStore } from '@/stores/toast'

const toastStore = useToastStore()
const { toasts } = storeToRefs(toastStore)
</script>

<style scoped>
.toast-host {
  position: fixed;
  right: 24px;
  bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 1000;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.25s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(16px);
}

.toast-host__toast {
  min-width: 220px;
  padding: 14px 18px;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  background-color: #ffffff;
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.18);
}

.toast-host__toast--success {
  border: 1px solid #1fb673;
}

.toast-host__toast--error {
  border: 1px solid #f05665;
}

.toast-host__toast--info {
  border: 1px solid #9ca3af;
}
</style>
