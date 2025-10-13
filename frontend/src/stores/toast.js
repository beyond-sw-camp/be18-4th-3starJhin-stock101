import { defineStore } from 'pinia'

let toastId = 0

export const useToastStore = defineStore('toast', {
  state: () => ({
    toasts: [],
  }),
  actions: {
    pushToast({ message, tone = 'info', duration = 3000 }) {
      const id = ++toastId
      this.toasts.push({ id, message, tone })
      if (duration > 0) {
        setTimeout(() => this.removeToast(id), duration)
      }
      return id
    },
    removeToast(id) {
      this.toasts = this.toasts.filter((toast) => toast.id !== id)
    },
    clear() {
      this.toasts = []
    },
  },
})
