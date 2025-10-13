<template>
  <div class="community-post-detail">
    <BaseBackButton class="community-post-detail__back" @click="handleBack">Back</BaseBackButton>

    <article v-if="post" class="community-post-detail__card">
      <div class="community-post-detail__top">
        <div class="community-post-detail__profile">
          <template v-if="post.imageUrl">
            <img :src="post.imageUrl" alt="avatar" class="community-post-detail__avatar-img" />
          </template>
          <template v-else>
            <div class="community-post-detail__avatar" aria-hidden="true"></div>
          </template>
          <div class="community-post-detail__info">
            <div class="community-post-detail__identity">
              <span class="community-post-detail__badge" :class="badgeClass">{{ post.opinion }}</span>
              <div class="community-post-detail__user">
                <span class="community-post-detail__author">{{ post.userName }}</span>
                <img
                  v-if="tierBadgeSrc"
                  :src="tierBadgeSrc"
                  :alt="`${post.authorTierCode ?? ''} tier`"
                  class="community-post-detail__tier-badge"
                />
              </div>
            </div>
            <p class="community-post-detail__content">{{ post.content }}</p>
          </div>
        </div>
        <span class="community-post-detail__timestamp">{{ formattedDate }}</span>
      </div>

      <footer class="community-post-detail__footer" @click.stop>
        <button
          type="button"
          class="community-post-detail__icon"
          :class="{ 'community-post-detail__icon--liked': post.likedByMe }"
          @click="handleToggleLike"
        >
          <svg width="22" height="22" viewBox="0 0 28 26" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M14 23.25s-7.45-5.74-10.6-9.37c-3.15-3.63-2.63-8.6 1.1-11.01 2.68-1.73 6.05-.82 7.98 1.73 1.93-2.55 5.3-3.46 7.98-1.73 3.73 2.41 4.25 7.38 1.1 11.01-3.15 3.63-10.6 9.37-10.6 9.37z"
              :fill="post.likedByMe ? '#f05665' : 'none'"
              :stroke="post.likedByMe ? '#f05665' : '#6b7280'"
              stroke-width="1.6"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <span>{{ post.likeCount }}</span>
        </button>
        <button type="button" class="community-post-detail__icon" @click="focusCommentComposer">
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

    <section class="community-post-detail__comments">
      <h2 class="community-post-detail__section-title">의견을 남겨 보세요.</h2>
      <CommentComposer
        ref="composerRef"
        v-model="commentContent"
        :is-logged-in="isLoggedIn"
        :show-avatar="false"
        @submit="handleSubmitComment"
        @exceed="notifyMaxChars"
        @login-required="requireLogin"
      />

      <div class="community-post-detail__comment-list">
        <CommunityCommentItem
          v-for="comment in comments"
          :key="comment.commentId"
          :comment="comment"
          :is-logged-in="isLoggedIn"
          @reply="handleSubmitReply"
          @login-required="requireLogin"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import CommentComposer from '@/components/community/CommentComposer.vue'
import CommunityCommentItem from '@/components/community/CommunityCommentItem.vue'
import BaseBackButton from '@/components/shared/BaseBackButton.vue'
import { useAuthStore } from '@/stores/authStore'
import { useSessionStore } from '@/stores/session'
import { useToastStore } from '@/stores/toast'
import { getTierBadgeSrc } from '@/utils/tierBadge'
import axios from 'axios'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

const LOGIN_REQUIRED_MESSAGE = '\ub85c\uadf8\uc778 \ud6c4 \uc774\uc6a9\ud574 \uc8fc\uc138\uc694'

const sessionStore = useSessionStore()
const authStore = useAuthStore()
const toastStore = useToastStore()

const post = ref(null)
const comments = ref([])
const isLoading = ref(false)
const error = ref(null)

const commentContent = ref('')
const composerRef = ref(null)

// Consider user logged in only when an access token exists
// Prefer authStore token (set by login) and fall back to sessionStore
const isLoggedIn = computed(() => !!(authStore.userInfo?.accessToken ?? sessionStore.accessToken))

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

function toDate(value) {
  return value ? new Date(value).getTime() : 0
}

function sortByNewest(a, b) {
  return toDate(b.createdAt) - toDate(a.createdAt)
}

function normalizeComment(raw) {
  const nested = raw.replies ?? raw.childComments ?? raw.children ?? []
  return {
    ...raw,
    replies: nested.map(normalizeComment),
  }
}

function buildCommentTree(items = []) {
  const lookup = new Map()
  items.forEach((item) => {
    const normalized = normalizeComment({ ...item, replies: [] })
    lookup.set(normalized.commentId, normalized)
  })

  const roots = []
  lookup.forEach((comment) => {
    if (comment.parentCommentId) {
      const parent = lookup.get(comment.parentCommentId)
      if (parent) {
        parent.replies.push(comment)
      } else {
        roots.push(comment)
      }
    } else {
      roots.push(comment)
    }
  })

  const sortReplies = (list) => {
    list.sort(sortByNewest)
    list.forEach((comment) => {
      if (comment.replies?.length) {
        sortReplies(comment.replies)
      }
    })
  }

  sortReplies(roots)
  return roots
}

function findComment(targetId, list) {
  for (const comment of list) {
    if (comment.commentId === targetId) return comment
    const nested = findComment(targetId, comment.replies ?? [])
    if (nested) return nested
  }
  return null
}

const badgeClass = computed(() => {
  if (!post.value) return ''
  if (post.value.opinion === 'Hold') return 'community-post-detail__badge--neutral'
  if (post.value.opinion === 'Strong Sell' || post.value.opinion === 'Sell') {
    return 'community-post-detail__badge--negative'
  }
  return 'community-post-detail__badge--positive'
})

const tierBadgeSrc = computed(() => {
  if (!post.value) return null
  return getTierBadgeSrc(post.value.authorTierCode)
})

const formattedDate = computed(() => {
  if (!post.value) return ''
  return new Date(post.value.createdAt).toLocaleString('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
})

async function loadPostData(postId) {
  if (!postId) return
  isLoading.value = true
  try {
  const headers = {}
  // Prefer authStore token (populated by login); fallback to sessionStore. Avoid sending demo token.
  const token = authStore.userInfo?.accessToken ?? sessionStore.accessToken
  if (token && token !== 'demo-access-token') headers.Authorization = `Bearer ${token}`
    const base = apiBaseUrl ? `${apiBaseUrl}/api/v1/board/posts` : '/api/v1/board/posts'
    const [postResp, commentsResp] = await Promise.all([
      axios.get(`${base}/${postId}`, { headers }),
      axios.get(`${base}/${postId}/comments`, { headers }),
    ])
    const postItems = Array.isArray(postResp.data?.items)
      ? postResp.data.items
      : Array.isArray(postResp.data)
        ? postResp.data
        : []
    const commentItems = Array.isArray(commentsResp.data?.items)
      ? commentsResp.data.items
      : Array.isArray(commentsResp.data)
        ? commentsResp.data
        : []
    post.value = postItems?.[0]
    if (post.value) {
      post.value.likedByMe = !!post.value.likedByMe
      post.value.likeCount = typeof post.value.likeCount === 'number' ? post.value.likeCount : Number(post.value.likeCount || 0)
    }
    comments.value = buildCommentTree(commentItems ?? [])
    error.value = null
  } catch (err) {
    console.error('[CommunityPostDetailView] loadPostData failed', err)
    error.value = err
    if (isLoggedIn.value) {
      toastStore.pushToast({ message: 'Unable to load the post.', tone: 'error' })
    }
    // If initial request failed (often because demo token caused server error), retry without auth headers
    try {
      const base = apiBaseUrl ? `${apiBaseUrl}/api/v1/board/posts` : '/api/v1/board/posts'
      const { data } = await axios.get(`${base}/${postId}`)
      const items = Array.isArray(data?.items) ? data.items : Array.isArray(data) ? data : []
      post.value = items?.[0] ?? null
      // try to fetch comments without auth as well
      try {
        const commentsResp = await axios.get(`${base}/${postId}/comments`)
        const commentItems = Array.isArray(commentsResp.data?.items)
          ? commentsResp.data.items
          : Array.isArray(commentsResp.data)
            ? commentsResp.data
            : []
        comments.value = buildCommentTree(commentItems ?? [])
      } catch (e) {
        console.error('[CommunityPostDetailView] fallback comments load failed', e)
      }
    } catch (e) {
      console.error('[CommunityPostDetailView] fallback post load failed', e)
    }
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  const postId = Number(route.params.postId)
  if (Number.isNaN(postId)) return
  loadPostData(postId)
})

onUnmounted(() => {
  post.value = null
  comments.value = []
  error.value = null
  isLoading.value = false
  commentContent.value = ''
})

function requireLogin() {
  toastStore.pushToast({ message: LOGIN_REQUIRED_MESSAGE, tone: 'info' })
}

function notifyMaxChars() {
  toastStore.pushToast({ message: '최대 300자까지 입력할 수 있어요.', tone: 'error' })
}

function focusCommentComposer() {
  composerRef.value?.focus?.()
}

async function handleSubmitComment() {
  if (!isLoggedIn.value) {
    requireLogin()
    commentContent.value = ''
    return
  }
  if (!post.value) return
  const content = commentContent.value.trim()
  if (!content) return
  try {
  const headers = {}
  const token = authStore.userInfo?.accessToken ?? sessionStore.accessToken
  if (token && token !== 'demo-access-token') headers.Authorization = `Bearer ${token}`
    const base = apiBaseUrl ? `${apiBaseUrl}/api/v1/board/posts` : '/api/v1/board/posts'
    const { data } = await axios.post(`${base}/${post.value.postId}/comments`, {
      content,
      userId: sessionStore.user?.id,
      parentCommentId: null,
    }, { headers })
    const items = Array.isArray(data?.items) ? data.items : []
    const [createdRaw] = items ?? []
    if (createdRaw) {
      const created = normalizeComment({ ...createdRaw, replies: [] })
      comments.value = [created, ...comments.value]
      // update post commentCount if returned
      const returnedTotal = typeof createdRaw.totalCommentCount === 'number' ? createdRaw.totalCommentCount : null
      if (post.value) {
        if (returnedTotal !== null) post.value.commentCount = returnedTotal
        else post.value.commentCount = (post.value.commentCount || 0) + 1
      }
    }
    commentContent.value = ''
    toastStore.pushToast({ message: '댓글이 등록되었어요.', tone: 'success' })
  } catch (err) {
    toastStore.pushToast({ message: '로그인 후 사용해 주세요.', tone: 'error' })
    console.error(err)
  }
}

async function handleSubmitReply(payload) {
  if (!isLoggedIn.value) {
    requireLogin()
    // ensure reply composer does not open and draft cleared
    payload.onComplete?.()
    return
  }
  if (!post.value) return
  try {
  const headers = {}
  const token = authStore.userInfo?.accessToken ?? sessionStore.accessToken
  if (token && token !== 'demo-access-token') headers.Authorization = `Bearer ${token}`
    const base = apiBaseUrl ? `${apiBaseUrl}/api/v1/board/posts` : '/api/v1/board/posts'
    const { data } = await axios.post(`${base}/${post.value.postId}/comments`, {
      content: payload.content,
      userId: sessionStore.user?.id,
      parentCommentId: payload.parentCommentId,
    }, { headers })
    const items = Array.isArray(data?.items) ? data.items : []
    const [createdRaw] = items ?? []
    if (createdRaw) {
      const created = normalizeComment({ ...createdRaw, replies: [] })
      if (payload.parentCommentId) {
        const parent = findComment(payload.parentCommentId, comments.value)
        if (parent) parent.replies = [...(parent.replies ?? []), created]
      } else {
        comments.value = [created, ...comments.value]
      }
      // update post commentCount if returned
      const returnedTotal = typeof createdRaw.totalCommentCount === 'number' ? createdRaw.totalCommentCount : null
      if (post.value) {
        if (returnedTotal !== null) post.value.commentCount = returnedTotal
        else post.value.commentCount = (post.value.commentCount || 0) + 1
      }
    }
    payload.onComplete?.()
    toastStore.pushToast({ message: '답글이 등록되었어요.', tone: 'success' })
  } catch (err) {
    toastStore.pushToast({ message: '답글 등록에 실패하였어요.', tone: 'error' })
    console.error(err)
  }
}

async function handleToggleLike() {
  if (!isLoggedIn.value) {
    requireLogin()
    return
  }
  if (!post.value) return
  try {
  const wasLiked = post.value.likedByMe
  // optimistic toggle for visual feedback, but DO NOT compute likeCount locally
  post.value.likedByMe = !wasLiked
  const headers = {}
  const token = authStore.userInfo?.accessToken ?? sessionStore.accessToken
  if (token && token !== 'demo-access-token') headers.Authorization = `Bearer ${token}`
    const base = apiBaseUrl ? `${apiBaseUrl}/api/v1/board/posts` : '/api/v1/board/posts'
    const { data } = await axios.post(`${base}/${post.value.postId}/like`, null, { headers })
    const items = Array.isArray(data?.items) ? data.items : []
    const [result] = items ?? []
    if (result) {
      if (typeof result.likeCount === 'number') post.value.likeCount = result.likeCount
      if (typeof result.likedByMe === 'boolean') post.value.likedByMe = result.likedByMe
      if (typeof result.commentCount === 'number') post.value.commentCount = result.commentCount
    }
    toastStore.pushToast({ message: wasLiked ? '좋아요가 취소되었어요.' : '좋아요가 반영되었어요.', tone: wasLiked ? 'info' : 'success' })
  } catch (err) {
  // revert optimistic liked state
  post.value.likedByMe = wasLiked
    toastStore.pushToast({ message: '로그인 후 이용해 주세요.', tone: 'error' })
    console.error(err)
  }
}

function handleBack() {
  router.back()
}
</script>

<style scoped>
.community-post-detail {
  display: flex;
  flex-direction: column;
  gap: 36px;
  padding: 40px;
  max-width: 880px;
  margin: 0 auto;
}

.community-post-detail__back {
  align-self: flex-start;
}

.community-post-detail__card {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 24px;
  border-radius: 22px;
  border: 1px solid #d1d5db;
  background-color: #ffffff;
}

.community-post-detail__top {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
}

.community-post-detail__profile {
  display: flex;
  gap: 18px;
  flex: 1;
  min-width: 0;
}

.community-post-detail__avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e5e7eb, #f3f4f6);
  flex-shrink: 0;
}

.community-post-detail__avatar-img {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 50%;
}

.community-post-detail__info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.community-post-detail__identity {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px;
}

.community-post-detail__user {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.community-post-detail__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
}

.community-post-detail__badge--positive {
  background-color: #d8f5df;
  color: #0f8c4f;
}

.community-post-detail__badge--negative {
  background-color: #ffd9de;
  color: #cb2943;
}

.community-post-detail__badge--neutral {
  background-color: #fff4b8;
  color: #946c00;
}

.community-post-detail__author {
  font-weight: 600;
  color: #111827;
  font-size: 16px;
}

.community-post-detail__tier-badge {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.community-post-detail__timestamp {
  font-size: 13px;
  color: #9ca3af;
  white-space: nowrap;
}

.community-post-detail__content {
  margin: 8px 0 0;
  font-size: 16px;
  line-height: 1.6;
  color: #374151;
}

.community-post-detail__footer {
  display: flex;
  gap: 20px;
}

.community-post-detail__icon {
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

.community-post-detail__icon--liked {
  color: #f05665;
}

.community-post-detail__icon span {
  font-variant-numeric: tabular-nums;
}

.community-post-detail__comments {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.community-post-detail__section-title {
  margin: 8px 0 0;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.community-post-detail__comment-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

@media (max-width: 768px) {
  .community-post-detail {
    padding: 24px;
  }
}
</style>
