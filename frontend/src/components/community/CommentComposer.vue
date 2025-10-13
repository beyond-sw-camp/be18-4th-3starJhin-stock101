<template>
  <div class="comment-composer" :class="{ 'comment-composer--locked': !isLoggedIn }">
    <div v-if="props.showAvatar" class="comment-composer__avatar" aria-hidden="true">
      <img v-if="sessionStore.user?.imageUrl" :src="sessionStore.user.imageUrl" alt="avatar" class="comment-composer__avatar-img" />
    </div>
    <div class="comment-composer__body">
      <input
        ref="inputRef"
        type="text"
        class="comment-composer__input"
        :value="modelValue"
        :readonly="!isLoggedIn || disabled"
  :placeholder="''"
        @mousedown="handleLockedInteraction"
        @focus="handleFocus"
        @input="handleInput"
        @keyup.enter="handleSubmit"
      />
      <button type="button" class="comment-composer__button" :disabled="!canSubmit" @click="handleSubmit">
        Comment
      </button>
    </div>
  </div>
</template>

<script setup>
import { useSessionStore } from '@/stores/session'
import { computed, ref } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  isLoggedIn: {
    type: Boolean,
    default: true,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  maxLength: {
    type: Number,
    default: 300,
  },
  showAvatar: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['update:modelValue', 'submit', 'exceed', 'login-required'])

const inputRef = ref(null)
const sessionStore = useSessionStore()

const canSubmit = computed(() => props.isLoggedIn && !props.disabled && props.modelValue.trim().length > 0)

function emitLoginRequired() {
  emit('login-required')
}

function handleLockedInteraction() {
  if (!props.isLoggedIn || props.disabled) {
    emitLoginRequired()
  }
}

function handleFocus(event) {
  if (!props.isLoggedIn || props.disabled) {
    emitLoginRequired()
    event?.target?.blur?.()
  }
}

function handleInput(event) {
  if (!props.isLoggedIn || props.disabled) {
    emitLoginRequired()
    event.target.value = props.modelValue
    return
  }
  const value = event.target.value
  if (value.length > props.maxLength) {
    event.target.value = value.slice(0, props.maxLength)
    emit('update:modelValue', event.target.value)
    emit('exceed')
    return
  }
  emit('update:modelValue', value)
}

function handleSubmit() {
  if (!props.isLoggedIn) {
    emitLoginRequired()
    return
  }
  if (!canSubmit.value) return
  emit('submit')
}

function focus() {
  inputRef.value?.focus?.()
}

defineExpose({ focus })
</script>

<style scoped>
.comment-composer {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 18px;
  border: 1px solid #d1d5db;
  background-color: #ffffff;
}

.comment-composer--locked {
  border-color: #303047;
  background-color: #ffffff;
}

.comment-composer__avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e5e7eb, #f3f4f6);
  flex-shrink: 0;
}

.comment-composer__avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.comment-composer__body {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 14px;
  align-items: center;
}

.comment-composer__input {
  border: none;
  background: transparent;
  font-size: 15px;
  color: #111827;
}

.comment-composer__input:read-only {
  cursor: not-allowed;
  color: #111827;
}

.comment-composer__input::placeholder {
  color: rgba(17, 24, 39, 0.5);
}

.comment-composer--locked .comment-composer__body {
  background-color: #f9fafb;
  border-radius: 12px;
  padding: 8px;
}

.comment-composer--locked .comment-composer__input {
  background-color: #f9fafb;
  border-radius: 8px;
  padding: 8px;
}

.comment-composer__button {
  min-width: 96px;
  padding: 12px 20px;
  border-radius: 14px;
  border: none;
  background-color: #0b091d;
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
}

.comment-composer__button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.comment-composer__button:not(:disabled):hover {
  background-color: #1d1a3b;
}
</style>
