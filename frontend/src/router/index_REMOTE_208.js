import { createRouter, createWebHistory } from 'vue-router'
import MyProfilePredictionsFeed from '@/views/MyProfilePredictionsFeed.vue'
import MyProfilePredictionsSuccess from '@/views/MyProfilePredictionsSuccess.vue'
import UserProfilePredictions from '@/views/UserProfilePredictions.vue'
import UserProfilePosts from '@/views/UserProfilePosts.vue'
import Main from '@/pages/main.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: "main",
      component: Main,
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
  ],
})

export default router
