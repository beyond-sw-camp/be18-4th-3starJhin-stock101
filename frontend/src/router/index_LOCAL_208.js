import { createRouter, createWebHistory } from 'vue-router'
import MyProfilePredictionsFeed from '@/views/MyProfilePredictionsFeed.vue'
import MyProfilePredictionsSuccess from '@/views/MyProfilePredictionsSuccess.vue'
import UserProfilePredictions from '@/views/UserProfilePredictions.vue'
import UserProfilePosts from '@/views/UserProfilePosts.vue'
import CommunityFeedView from '@/views/CommunityFeedView.vue'
import CommunityPostDetailView from '@/views/CommunityPostDetailView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/profile/me/predictions',
    },
    {
      path: '/profile/me/predictions',
      name: 'MyProfilePredictions',
      component: MyProfilePredictionsFeed,
    },
    {
      path: '/profile/me/predictions/success',
      name: 'MyProfilePredictionsSuccess',
      component: MyProfilePredictionsSuccess,
    },
    {
      path: '/profile/users/:id/predictions',
      name: 'UserProfilePredictions',
      component: UserProfilePredictions,
      props: true,
    },
    {
      path: '/profile/users/:id/posts',
      name: 'UserProfilePosts',
      component: UserProfilePosts,
      props: true,
    },
    {
      path: '/community',
      name: 'CommunityFeed',
      component: CommunityFeedView,
    },
    {
      path: '/community/posts/:postId',
      name: 'CommunityPostDetail',
      component: CommunityPostDetailView,
      props: true,
    },
  ],
})

export default router
