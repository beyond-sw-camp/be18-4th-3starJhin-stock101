<template>
  <div class="profile-page">
    <BaseBackButton class="profile-page__back" @click="handleBack">돌아가기</BaseBackButton>
    <h1 class="profile-page__title">{{ pageTitle }}</h1>

    <ProfileStats :summary="summary" :active-filter="resultFilter" @filter-change="handleFilterChange" />

    <ProfileHeader :user="profile.user" :is-owner="isOwner" :current-user="currentUser" class="profile-page__header" @profile-updated="handleProfileUpdate" />

    <ProfileTabs v-model="activeTab" :user="profile.user" />

    <section v-if="activeTab === 'predictions'" class="profile-page__section">
      <div v-if="loading" class="profile-page__loading">
        <p>데이터를 불러오는 중...</p>
      </div>

      <!-- Always show PredictionSummaryCard in grid layout -->
      <div v-else class="profile-page__list profile-page__list--grid">
        <PredictionSummaryCard
          v-for="prediction in filteredPredictions"
          :key="prediction.predictionId"
          :prediction="prediction"
        />
      </div>

            <div v-if="!loading && filteredPredictions.length === 0" class="profile-page__empty">
        <p>해당 조건에 맞는 예측이 없습니다.</p>
      </div>
    </section>

    <section v-else class="profile-page__section">
      <div class="profile-page__list profile-page__list--stack">
        <CommunityPostCard
          v-for="post in transformedPosts"
          :key="post.postId"
          :post="post"
          :is-logged-in="!!authStore.userInfo?.accessToken"
          @select="handlePostSelect"
          @like="handlePostLike"
          @comment="handlePostComment"
          @login-required="handleLoginRequired"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import BaseBackButton from '@/components/shared/BaseBackButton.vue'
import ProfileHeader from '@/components/profile/ProfileHeader.vue'
import ProfileStats from '@/components/profile/ProfileStats.vue'
import ProfileTabs from '@/components/profile/ProfileTabs.vue'
import PredictionSummaryCard from '@/components/predictions/PredictionSummaryCard.vue'
import CommunityPostCard from '@/components/community/CommunityPostCard.vue'
import { getUserPredictions, getUserPredictionsById, transformPredictionData } from '@/services/predictionApi'
import { myProfile, otherProfile, calculatePredictionSummary } from '@/data/profileDemo'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const profile = ref({
  user: myProfile.user,
  predictions: [],
  posts: [] // Start with empty posts array - will be populated from API
})

// Computed properties based on route parameters and auth status
const userId = computed(() => {
  // Use the ID parameter from /profile/:id
  if (route.params.id) {
    return route.params.id
  }

  // Fallback to user ID 1 if no parameter
  return '1'
})

const tab = computed(() => route.query.tab || 'predictions')

const isOwner = computed(() => {
  // If no one is logged in, no one is the owner
  if (!authStore.userInfo?.accessToken || !authStore.userInfo?.userId) {
    return false
  }
  // Check if viewing own profile by comparing user IDs
  return userId.value === String(authStore.userInfo.userId)
})

const pageTitle = computed(() => {
  if (!authStore.userInfo?.accessToken || !authStore.userInfo?.userId) {
    return '사용자 프로필'
  }
  return isOwner.value ? '나의 프로필' : '사용자 프로필'
})

const currentUser = computed(() => {
  if (!authStore.userInfo?.accessToken || !authStore.userInfo?.userId) {
    return null
  }
  return {
    id: authStore.userInfo.userId,
    name: authStore.userInfo.userName || 'Current User',
    email: authStore.userInfo.email || '',
    tierCode: authStore.userInfo.tierCode || 'BRONZE',
  }
})

const defaultTab = computed(() => tab.value)

const enableFilters = computed(() =>
  // Only enable filters for own profile or when viewing other user's predictions
  isOwner.value || tab.value === 'predictions'
)

// UI state management (merged from ProfilePageLayout)
const activeTab = ref(defaultTab.value)
const resultFilter = ref('all')

// Watch for tab changes from query params
watch(
  () => defaultTab.value,
  (newValue) => {
    activeTab.value = newValue
  },
)

// Summary computation (merged from ProfilePageLayout)
const summary = computed(() => {
  if (!profile.value?.predictions) {
    return { total: 0, success: 0, failure: 0, pending: 0, accuracy: 0 }
  }
  return calculatePredictionSummary(profile.value.predictions)
})

// Filter handling (merged from ProfilePageLayout)
const handleFilterChange = (filterKey) => {
  resultFilter.value = filterKey
}

const filteredPredictions = computed(() => {
  if (!profile.value?.predictions) {
    return []
  }

  if (resultFilter.value === 'all') {
    return profile.value.predictions
  }
  if (resultFilter.value === 'PENDING') {
    // 결과 대기: result가 null인 경우
    return profile.value.predictions.filter((item) => item.result === null || item.result === 'PENDING')
  }
  if (resultFilter.value === 'SUCCESS') {
    // 예측 성공: result가 "SUCCESS"인 경우
    return profile.value.predictions.filter((item) => item.result === 'SUCCESS')
  }
  if (resultFilter.value === 'FAILURE') {
    // 예측 실패: result가 "FAILURE"인 경우
    return profile.value.predictions.filter((item) => item.result === 'FAILURE')
  }
  return profile.value.predictions
})

// Transform posts data to match CommunityPostCard expected format
const transformedPosts = computed(() => {
  if (!profile.value?.posts) {
    return []
  }

  return profile.value.posts.map(post => ({
    postId: post.postId,
    stockId: post.stockId || 0,
    userId: post.userId || profile.value.user.id,
    opinion: post.opinion || post.tag || 'Buy', // Use API opinion field, fallback to tag, then 'Buy'
    content: post.content,
    createdAt: post.createdAt || post.postedAt,
    userName: post.userName || post.author || profile.value.user.name,
    likedByMe: post.likedByMe || false,
    likeCount: post.likeCount || post.likes || 0,
    commentCount: post.commentCount || post.comments || 0,
    authorTierCode: post.authorTierCode || profile.value.user.tierCode || 'BRONZE',
    imageUrl: post.imageUrl || profile.value.user.imageUrl || null
  }))
})

// Function to update URL when tab changes
const updateTabInUrl = (newTab) => {
  const currentPath = route.path
  const query = { ...route.query }

  if (newTab === 'predictions') {
    // Remove tab query if it's the default
    delete query.tab
  } else {
    query.tab = newTab
  }

  router.push({
    path: currentPath,
    query: Object.keys(query).length > 0 ? query : undefined
  })
}

const fetchUserInfo = async (targetUserId) => {
  try {
    // Always fetch user info from API using the actual user ID
    console.log('Fetching user info from API for user:', targetUserId)
    const response = await axios.get(`http://localhost:8080/api/v1/users/${targetUserId}`)
    const userData = response.data.items[0]
    profile.value.user = {
      id: userData.userId,
      name: userData.name,
      email: userData.email,
      tierCode: userData.tierCode,
      statusMessage: userData.statusMessage,
      imageUrl: userData.imageUrl
    }
    console.log('User info updated:', profile.value.user.name)
  } catch (error) {
    console.error('Failed to fetch user info:', error)
    // Fallback to mock data
    const fallbackUser = (targetUserId === '0') ? myProfile.user : otherProfile.user1
    profile.value.user = { ...fallbackUser, id: targetUserId }
  }
}

const fetchUserPosts = async (targetUserId) => {
  try {
    console.log('Fetching user posts from API for user:', targetUserId)
    const headers = {}
    const token = authStore.userInfo?.accessToken
    if (token && token !== 'demo-access-token') {
      headers.Authorization = `Bearer ${token}`
    }

    const response = await axios.get(`http://localhost:8080/api/v1/board/user/${targetUserId}`, { headers })
    const items = Array.isArray(response.data?.items)
      ? response.data.items
      : Array.isArray(response.data?.data)
        ? response.data.data
        : Array.isArray(response.data)
          ? response.data
          : []

    profile.value.posts = items
    console.log('User posts fetched:', items.length, 'posts')
  } catch (error) {
    console.error('Failed to fetch user posts:', error)
    // Fallback to empty array instead of mock data
    profile.value.posts = []
  }
}

const fetchPredictions = async (targetUserId) => {
  loading.value = true
  console.log('fetchPredictions called with targetUserId:', targetUserId)
  try {
    let response

   // Fetch predictions using direct axios call (for other users or unauthenticated)
    console.log('Fetching predictions for user:', targetUserId)
    response = await axios.get(`http://localhost:8080/api/v1/prediction/user/${targetUserId}`)

    const transformedPredictions = transformPredictionData(response.data || response)
    profile.value.predictions = transformedPredictions
    console.log('Transformed predictions:', transformedPredictions.length, 'items')

    // Fetch user info separately
    await fetchUserInfo(targetUserId)

    // Fetch user posts from API
    await fetchUserPosts(targetUserId)
  } catch (error) {
    console.error('Failed to fetch predictions:', error)
    // Fallback to mock data on error (only for predictions and user info, not posts)
    const fallbackProfile = (targetUserId === '1') ? myProfile : otherProfile
    profile.value = {
      ...fallbackProfile,
      posts: [] // Always start with empty posts array
    }
    // Still try to fetch posts even if predictions fail
    try {
      await fetchUserPosts(targetUserId)
    } catch (postsError) {
      console.error('Failed to fetch posts in fallback:', postsError)
    }
  } finally {
    loading.value = false
  }
}

const handleProfileUpdate = (updatedData) => {
  // Update the local profile data with the new information
  profile.value.user = {
    ...profile.value.user,
    name: updatedData.name,
    statusMessage: updatedData.statusMessage
  }
  console.log('Profile updated locally:', updatedData)
}

// Event handlers for CommunityPostCard interactions
const handlePostSelect = (post) => {
  console.log('Post selected:', post)
  // Navigate to post detail if needed
  router.push({ name: 'CommunityPostDetail', params: { postId: post.postId } })
}

const handlePostLike = (post) => {
  console.log('Post liked:', post)
  // Handle like functionality - could integrate with API in the future
  const postIndex = profile.value.posts.findIndex(p => p.postId === post.postId)
  if (postIndex !== -1) {
    // Toggle like optimistically
    const originalPost = profile.value.posts[postIndex]
    originalPost.likes = post.likedByMe ? originalPost.likes - 1 : originalPost.likes + 1
  }
}

const handlePostComment = (post) => {
  console.log('Post comment clicked:', post)
  // Navigate to post detail for commenting
  router.push({ name: 'CommunityPostDetail', params: { postId: post.postId } })
}

const handleLoginRequired = () => {
  console.log('Login required for post interaction')
  // Could show a toast or redirect to login
  alert('로그인이 필요합니다.')
}

const handleBack = () => {
  router.back()
}

onMounted(() => {
  console.log('ProfileView mounted, route:', route.path, route.params, 'userId:', userId.value)
  fetchPredictions(userId.value)
})

// Watch for route parameter changes
watch(
  () => [route.params.id, route.path],
  () => {
    fetchPredictions(userId.value)
  }
)

// Watch for tab changes (in case we need to fetch different data)
watch(
  () => tab.value,
  (newTab) => {
    // For future enhancement: fetch different data based on tab
    console.log('Tab changed to:', newTab)
  }
)

// Watch for auth state changes
watch(
  () => authStore.userInfo?.accessToken,
  () => {
    // Refetch data when user logs in/out
    fetchPredictions(userId.value)
  }
)
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 32px;
  padding: 40px;
  max-width: 980px;
  margin: 0 auto;
}

.profile-page__back {
  align-self: flex-start;
}

.profile-page__title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #111827;
}

.profile-page__header {
  margin-top: -12px;
}

.profile-page__section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-page__list {
  display: grid;
  gap: 20px;
}

.profile-page__list--stack {
  grid-template-columns: 1fr;
}

.profile-page__list--grid {
  grid-template-columns: 1fr 1fr;
}

.profile-page__loading,
.profile-page__empty {
  text-align: center;
  padding: 40px 20px;
  color: #6b7280;
}

.profile-page__loading p,
.profile-page__empty p {
  margin: 0;
  font-size: 16px;
}

@media (max-width: 768px) {
  .profile-page {
    padding: 24px;
  }

  .profile-page__title {
    font-size: 26px;
  }

  .profile-page__list--grid {
    grid-template-columns: 1fr;
  }
}
</style>