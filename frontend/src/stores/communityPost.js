import { useSessionStore } from '@/stores/session'
import apiClient from '@/api'
import { defineStore } from 'pinia'

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

export const useCommunityPostStore = defineStore('communityPost', {
  state: () => ({
    post: null,
    comments: [],
    isLoading: false,
    error: null,
  }),
  actions: {
    async load(postId) {
      this.isLoading = true
      try {
        const [postResp, commentsResp] = await Promise.all([
          apiClient.get(`/api/v1/board/posts/${postId}`),
          apiClient.get(`/api/v1/board/posts/${postId}/comments`),
        ])
        const postItems = Array.isArray(postResp.data?.items) ? postResp.data.items : []
        const commentItems = Array.isArray(commentsResp.data?.items) ? commentsResp.data.items : []
        this.post = postItems?.[0] ?? null
        this.comments = buildCommentTree(commentItems ?? [])
        this.error = null
      } catch (error) {
        this.error = error
      } finally {
        this.isLoading = false
      }
    },
    async loadComments(postId) {
      this.isLoading = true
      try {
        const commentsResp = await apiClient.get(`/api/v1/board/posts/${postId}/comments`)
        const commentItems = Array.isArray(commentsResp.data?.items)
          ? commentsResp.data.items
          : Array.isArray(commentsResp.data)
            ? commentsResp.data
            : []
        this.comments = buildCommentTree(commentItems ?? [])
        this.error = null
      } catch (error) {
        this.error = error
      } finally {
        this.isLoading = false
      }
    },
    async addComment(postId, { content, user, parentCommentId = null }) {
      const payload = {
        content,
        userId: user?.id,
        userName: user?.name,
        authorTierCode: user?.tier,
        parentCommentId,
      }
      const { data } = await apiClient.post(`/api/v1/board/posts/${postId}/comments`, {
        content: payload.content,
        userId: payload.userId,
        parentCommentId: payload.parentCommentId,
      })
      const items = Array.isArray(data?.items) ? data.items : []
      const [createdRaw] = items ?? []
      if (!createdRaw) return null
      const created = normalizeComment({ ...createdRaw, replies: [] })

      if (parentCommentId) {
        const parent = findComment(parentCommentId, this.comments)
        if (parent) {
          parent.replies = [...(parent.replies ?? []), created]
        }
      } else {
        this.comments = [created, ...this.comments]
      }

      if (this.post) {
        // If backend returned a totalCommentCount, use it to avoid double-counting
        const returnedTotal = typeof createdRaw.totalCommentCount === 'number' ? createdRaw.totalCommentCount : null
        if (returnedTotal !== null) {
          this.post.commentCount = returnedTotal
        } else {
          this.post.commentCount += 1
        }
      }
      return created
    },
    hydrateFromResponse(postItems = [], commentItems = []) {
            this.post = postItems?.[0] ?? null
            this.comments = buildCommentTree(commentItems ?? [])
          this.error = null},
    async toggleLike(postId) {
      if (!this.post || this.post.postId !== postId) return null
      const wasLiked = this.post.likedByMe
      this.post.likedByMe = !wasLiked
      this.post.likeCount += wasLiked ? -1 : 1
      try {
        const { data } = await apiClient.post(`/api/v1/board/posts/${postId}/like`, null)
        const items = Array.isArray(data?.items) ? data.items : []
        const [result] = items ?? []
        if (result) {
          if (typeof result.likeCount === 'number') this.post.likeCount = result.likeCount
          if (typeof result.commentCount === 'number') this.post.commentCount = result.commentCount
          if (typeof result.likedByMe === 'boolean') this.post.likedByMe = result.likedByMe
        }
      } catch (error) {
        this.post.likedByMe = wasLiked
        this.post.likeCount += wasLiked ? 1 : -1
        throw error
      }
    },
    clear() {
      this.post = null
      this.comments = []
      this.error = null
      this.isLoading = false
    },
  },
})



