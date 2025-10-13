<template>
  <article class="community-comment-item">
    <template v-if="comment.imageUrl">
      <img :src="comment.imageUrl" alt="avatar" class="community-comment-item__avatar-img" />
    </template>
    <template v-else>
      <div class="community-comment-item__avatar" aria-hidden="true"></div>
    </template>
    <div class="community-comment-item__content">
      <header class="community-comment-item__header">
        <span class="community-comment-item__author">{{ comment.userName }}</span>
        <img
          v-if="tierBadgeSrc"
          :src="tierBadgeSrc"
          :alt="`${comment.authorTierCode ?? ''} tier`"
          class="community-comment-item__tier-badge"
        />
        <span class="community-comment-item__meta">{{ formattedDate }}</span>
      </header>
      <p class="community-comment-item__body">{{ comment.content }}</p>
      <div class="community-comment-item__actions">
        <button type="button" class="community-comment-item__reply" @click="handleReplyClick">
          답글 달기
        </button>
      </div>

      <div v-if="showReplyComposer" class="community-comment-item__reply-composer">
        <input
          v-model="replyDraft"
          type="text"
          class="community-comment-item__reply-input"
          placeholder="답글을 입력하세요"
          @keyup.enter="submitReply"
        />
        <div class="community-comment-item__reply-controls">
          <button type="button" class="community-comment-item__reply-cancel" @click="closeReplyComposer">
            취소
          </button>
          <button
            type="button"
            class="community-comment-item__reply-submit"
            :disabled="!replyDraft.trim()"
            @click="submitReply"
          >
            등록
          </button>
        </div>
      </div>

      <div v-if="replies.length" class="community-comment-item__replies">
        <div v-for="reply in visibleReplies" :key="reply.commentId" class="community-comment-item__reply-item">
          <div class="community-comment-item__reply-row">
            <template v-if="reply.imageUrl">
              <img :src="reply.imageUrl" alt="avatar" class="community-comment-item__reply-avatar" />
            </template>
            <template v-else>
              <div class="community-comment-item__reply-avatar" aria-hidden="true"></div>
            </template>
            <div class="community-comment-item__reply-body-wrapper">
              <header class="community-comment-item__reply-header">
                <span class="community-comment-item__reply-author">{{ reply.userName }}</span>
                <img
                  v-if="getReplyBadge(reply)"
                  :src="getReplyBadge(reply)"
                  :alt="`${reply.authorTierCode ?? ''} tier`"
                  class="community-comment-item__tier-badge"
                />
                <span class="community-comment-item__reply-meta">{{ formatReplyDate(reply.createdAt) }}</span>
              </header>
              <p class="community-comment-item__reply-body">{{ reply.content }}</p>
            </div>
          </div>
        </div>
        <button
          v-if="hasHiddenReplies || repliesExpanded"
          type="button"
          class="community-comment-item__replies-toggle"
          @click="toggleReplies"
        >
          {{ repliesExpanded ? '닫기' : `더보기 (${replies.length - 3})` }}
        </button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { getTierBadgeSrc } from '@/utils/tierBadge'
import { computed, ref } from 'vue'

const props = defineProps({
  comment: {
    type: Object,
    required: true,
  },
  isLoggedIn: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['reply', 'login-required'])

const replyDraft = ref('')
const showReplyComposer = ref(false)
const repliesExpanded = ref(false)

const tierBadgeSrc = computed(() => getTierBadgeSrc(props.comment.authorTierCode))

const replies = computed(() => props.comment.replies ?? [])

const visibleReplies = computed(() => {
  if (repliesExpanded.value) return replies.value
  return replies.value.slice(0, 3)
})

const hasHiddenReplies = computed(() => replies.value.length > 3 && !repliesExpanded.value)

const formattedDate = computed(() => formatReplyDate(props.comment.createdAt))

function formatReplyDate(value) {
  if (!value) return ''
  return new Date(value).toLocaleString('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function getReplyBadge(reply) {
  return getTierBadgeSrc(reply.authorTierCode)
}

function handleReplyClick() {
  if (!props.isLoggedIn) {
    emit('login-required')
    return
  }
  showReplyComposer.value = !showReplyComposer.value
  if (!showReplyComposer.value) {
    replyDraft.value = ''
  }
}

function closeReplyComposer() {
  showReplyComposer.value = false
  replyDraft.value = ''
}

function submitReply() {
  const content = replyDraft.value.trim()
  if (!content) return
  emit('reply', {
    parentCommentId: props.comment.commentId,
    content,
    onComplete: () => {
      replyDraft.value = ''
      showReplyComposer.value = false
    },
  })
}

function toggleReplies() {
  repliesExpanded.value = !repliesExpanded.value
}
</script>

<style scoped>
.community-comment-item {
  display: flex;
  gap: 14px;
  padding: 18px;
  border-radius: 16px;
  border: 1px solid #d1d5db;
  background-color: #ffffff;
}

.community-comment-item__avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e5e7eb, #f3f4f6);
  flex-shrink: 0;
}

.community-comment-item__avatar-img {
  width: 44px;
  height: 44px;
  object-fit: cover;
  border-radius: 50%;
}

.community-comment-item__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.community-comment-item__header {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.community-comment-item__author {
  font-weight: 600;
  color: #111827;
}

.community-comment-item__tier-badge {
  width: 16px;
  height: 16px;
  object-fit: contain;
}

.community-comment-item__meta {
  font-size: 12px;
  color: #9ca3af;
}

.community-comment-item__body {
  margin: 0;
  font-size: 15px;
  line-height: 1.5;
  color: #374151;
}

.community-comment-item__actions {
  display: flex;
  gap: 12px;
}

.community-comment-item__reply {
  border: none;
  background: none;
  color: #6b7280;
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}

.community-comment-item__reply-composer {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  border-radius: 12px;
  background-color: #f9fafb;
}

.community-comment-item__reply-input {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  background-color: #ffffff;
}

.community-comment-item__reply-controls {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.community-comment-item__reply-cancel,
.community-comment-item__reply-submit {
  border: none;
  border-radius: 10px;
  padding: 8px 16px;
  font-size: 13px;
  cursor: pointer;
}

.community-comment-item__reply-cancel {
  background-color: transparent;
  color: #6b7280;
}

.community-comment-item__reply-submit {
  background-color: #14122a;
  color: #ffffff;
}

.community-comment-item__reply-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.community-comment-item__replies {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-left: calc(44px + 14px);
}

.community-comment-item__reply-item {
  padding: 12px 16px;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background-color: #f9fafb;
}

.community-comment-item__reply-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.community-comment-item__reply-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  background: linear-gradient(135deg, #e5e7eb, #f3f4f6);
}

.community-comment-item__reply-body-wrapper {
  flex: 1;
}

.community-comment-item__reply-header {
  display: flex;
  gap: 6px;
  align-items: center;
  flex-wrap: wrap;
}

.community-comment-item__reply-author {
  font-weight: 600;
  color: #1f2937;
  font-size: 14px;
}

.community-comment-item__reply-meta {
  font-size: 11px;
  color: #9ca3af;
}

.community-comment-item__reply-body {
  margin: 6px 0 0;
  font-size: 14px;
  line-height: 1.5;
  color: #4b5563;
}

.community-comment-item__replies-toggle {
  align-self: flex-start;
  border: none;
  background: none;
  color: #6b7280;
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}
</style>
