<template>
  <article class="community-post-card" @click="handleSelect">
    <div class="community-post-card__top">
      <div class="community-post-card__profile">
        <template v-if="post.imageUrl">
          <img :src="post.imageUrl" alt="avatar" class="community-post-card__avatar-img" />
        </template>
        <template v-else>
          <div class="community-post-card__avatar" aria-hidden="true"></div>
        </template>
        <div class="community-post-card__info">
          <div class="community-post-card__identity">
            <span class="community-post-card__badge" :class="badgeClass">{{ post.opinion }}</span>
            <div class="community-post-card__user">
              <span class="community-post-card__author">{{ post.userName }}</span>
              <img
                v-if="tierBadgeSrc"
                :src="tierBadgeSrc"
                :alt="`${post.authorTierCode ?? ''} tier`"
                class="community-post-card__tier-badge"
              />
            </div>
          </div>
          <p class="community-post-card__content">{{ post.content }}</p>
        </div>
      </div>
      <span class="community-post-card__timestamp">{{ formattedDate }}</span>
    </div>

    <footer class="community-post-card__footer" @click.stop>
      <button
        type="button"
        class="community-post-card__icon"
        :class="{ 'community-post-card__icon--liked': isLiked }"
        @click.stop="handleLike"
      >
        <svg width="22" height="22" viewBox="0 0 28 26" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M14 23.25s-7.45-5.74-10.6-9.37c-3.15-3.63-2.63-8.6 1.1-11.01 2.68-1.73 6.05-.82 7.98 1.73 1.93-2.55 5.3-3.46 7.98-1.73 3.73 2.41 4.25 7.38 1.1 11.01-3.15 3.63-10.6 9.37-10.6 9.37z"
            :fill="isLiked ? '#f05665' : 'none'"
            :stroke="isLiked ? '#f05665' : '#6b7280'"
            stroke-width="1.6"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <span>{{ post.likeCount }}</span>
      </button>
  <button type="button" class="community-post-card__icon" @click.stop="handleComment">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M21 12c0 4.418-4.03 8-9 8-1.013 0-1.99-.154-2.905-.44L3 20l1.58-3.162C3.59 15.695 3 13.91 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
            stroke="#6b7280"
            stroke-width="1.6"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <span>{{ post.commentCount }}</span>
      </button>
    </footer>
  </article>
</template>

<script setup>
import { getTierBadgeSrc } from '@/utils/tierBadge'
import { computed } from 'vue'

const props = defineProps({
  post: {
    type: Object,
    required: true,
  },
  isLoggedIn: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['select', 'like', 'comment', 'login-required'])

const badgeClass = computed(() => {
  if (props.post.opinion === 'Hold') return 'community-post-card__badge--neutral'
  if (props.post.opinion === 'Strong Sell' || props.post.opinion === 'Sell') {
    return 'community-post-card__badge--negative'
  }
  return 'community-post-card__badge--positive'
})

const tierBadgeSrc = computed(() => getTierBadgeSrc(props.post.authorTierCode))

const formattedDate = computed(() => {
  const value = props.post.createdAt
  return new Date(value).toLocaleString('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
})

// robust liked state computed from possible server field names/shapes
const isLiked = computed(() => {
  const p = props.post || {}
  return !!(p.likedByMe ?? p.liked ?? p.isLiked ?? p.liked_by_me ?? p.likedByUser)
})

function handleSelect() {
  emit('select', props.post)
}

function handleLike(event) {
  // prevent the like button from propagating to the article click
  event?.stopPropagation?.()
  if (!props.isLoggedIn) {
    emit('login-required')
    return
  }
  emit('like', props.post)
}

function handleComment() {
  emit('comment', props.post)
}
</script>

<style scoped>
.community-post-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 24px;
  border-radius: 18px;
  border: 1px solid #d1d5db;
  background-color: #ffffff;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  cursor: pointer;
}

.community-post-card:hover {
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
  transform: translateY(-2px);
}

.community-post-card__top {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.community-post-card__profile {
  display: flex;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.community-post-card__avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e5e7eb, #f3f4f6);
  flex-shrink: 0;
}

.community-post-card__avatar-img {
  width: 44px;
  height: 44px;
  object-fit: cover;
  border-radius: 50%;
}

.community-post-card__info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.community-post-card__identity {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.community-post-card__user {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.community-post-card__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  background-color: #f3f4f6;
  color: #111827;
}

.community-post-card__badge--positive {
  background-color: #d8f5df;
  color: #0f8c4f;
}

.community-post-card__badge--negative {
  background-color: #ffd9de;
  color: #cb2943;
}

.community-post-card__badge--neutral {
  background-color: #fff4b8;
  color: #946c00;
}

.community-post-card__author {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.community-post-card__tier-badge {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

.community-post-card__timestamp {
  font-size: 13px;
  color: #9ca3af;
  white-space: nowrap;
}

.community-post-card__content {
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
  color: #374151;
  word-break: keep-all;
}

.community-post-card__footer {
  display: flex;
  gap: 20px;
}

.community-post-card__icon {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: #6b7280;
  font-size: 14px;
  cursor: pointer;
  padding: 4px 6px;
}

.community-post-card__icon--liked {
  color: #f05665;
}

.community-post-card__icon span {
  font-variant-numeric: tabular-nums;
}
</style>
