
import { useAuthStore } from '@/stores/authStore'
import ProfileView from '@/views/ProfileView.vue'
import { createRouter, createWebHistory } from 'vue-router'

import UserLogin from '@/pages/auth/Login.vue'
import UserRegister from '@/pages/auth/Register.vue'
import Main from '@/pages/main.vue'
import Stock_detail from '@/pages/stock_detail.vue'
import CommunityFeedView from '@/views/CommunityFeedView.vue'
import CommunityPostDetailView from '@/views/CommunityPostDetailView.vue'

import StockCommunityView from '@/views/StockCommunityView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'main',
      component: Main,
    },
    {
      path: '/auth/login',
      name: 'userLogin',
      meta: { requiresAuth: false, guestOnly: true },
      component: UserLogin,
    },
    {
      path: '/auth/register',
      name: 'userRegister',
      component: UserRegister,
      meta: { requiresAuth: false, guestOnly: true },
    },
    {
      path: '/stock/:stockId',
      name: 'stockDetail',
      component: Stock_detail,
      props: true,
    },
    {
      path: '/profile/:id',
      name: 'UserProfile',
      component: ProfileView,
      props: true,
    },
    {
      path: '/community',
      name: 'CommunityFeed',
      component: CommunityFeedView,
    },
    {
      path: '/stock/:symbol',
      name: 'StockCommunity',
      component: StockCommunityView,
      props: true,
    },
    {
      path: '/community/posts/:postId',
      name: 'CommunityPostDetail',
      component: CommunityPostDetailView,
      props: true,
    },
  ],
})
//  네비게이션 가드
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const isLoggedIn = authStore.isLoggedIn // 로그인 상태 가져오기

  if (to.meta.guestOnly && isLoggedIn) {
    next({ name: 'main' })
  } else {
    next()
  }
})

export default router
