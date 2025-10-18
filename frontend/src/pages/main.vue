<template>
  <section class="page">

    <!-- 헤더 -->
    <header class="hero">
      <div class="hero__left">
        <h1>Stock 101</h1>
        <p class="tagline">시장의 지표들을 한눈에 찾아보고, 새로운 투자 관점을 탐험해보세요.</p>
      </div>
      <div class="hero__actions">
        <template v-if="!authStore.isLoggedIn">
          <BaseButton class="btn" @click="goRegister">회원 가입</BaseButton>
          <BaseButton class="btn btn--primary" @click="goLogin">로그인</BaseButton>
        </template>
        <template v-else>
          <BaseButton class="btn" @click="goProfile(authStore.userInfo.userId)"> 내 정보</BaseButton>
          <BaseButton class="btn btn--primary" @click="goLogout">로그아웃</BaseButton>
        </template>
      </div>
    </header>

    <!-- 주요 지수 -->
    <section class="sec">
      <BaseGrid :items="indices" :cols="3" gap="16px" itemKey="id">
        <template #default="{ item }">
          <StatCard
            :title="item.title"
            :subtitle="item.subtitle"
            :value="item.value"
            :change="item.change"
            :percentMode="true"
          />
        </template>
      </BaseGrid>
    </section>

    <!-- 지금 주목 받는 주식 -->
    <section class="sec">
      <h2 class="sec__title">지금 주목 받는 주식</h2>
      <div class="hot-stocks">
        <div class="hot-stocks__track">
          <StatCard
            v-for="item in Stocks"
            :key="item.id"
            class="hot-stocks__card"
            :title="item.symbol"
            :subtitle="item.name"
            :value="item.price"
            locale="en-US"
            currency="USD"
            :change="item.change"
            :percentMode="true"
            :clickable="true"
            @click="goStock(item.id)"
          />
        </div>
      </div>
    </section>

    <!-- 최근 핫한 뉴스 -->
    <section class="sec">
      <h2 class="sec__title">최근 핫한 뉴스 <a href=""></a></h2>
      <BaseGrid :items="newsCols" :cols="2" gap="24px">
        <template #default="{ item: col }">
          <div class="news-col">
            <NewsCard
              v-for="n in col"
              :key="n.id"
              :image="n.image"
              :title="n.title"
              :date="n.date"
              :source="n.source"
              :href="n.href"
              target="_blank"
            />
          </div>
        </template>
      </BaseGrid>
    </section>

    <!-- 최강의 투자자들 -->
    <section class="sec">
      <h2 class="sec__title">최강의 투자자들</h2>
      <BaseGrid :items="investors" :cols="2" gap="16px" itemKey="id">
        <template #default="{ item }">
          <ProfileCard
            :avatar="item.imageUrl || item.avatar"
            :name="item.name"
            :status="item.status"
            :tier-code="item.tierCode"
            :userId="item.id"
            @click="goProfile(item.id)"
          />
        </template>
      </BaseGrid>
    </section>

  </section>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

import BaseGrid from '@/components/grid/BaseGrid.vue'
import StatCard from '@/components/card/variants/StatCard.vue'
import NewsCard from '@/components/card/variants/NewsCard.vue'
import ProfileCard from '@/components/card/variants/ProfileCard.vue'
import BaseButton from '@/components/button/BaseButton.vue'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore();
//const isLoggedIn = computed(() =>{ authStore.tokenInfo.accessToken});
const goLogin = () => router.push({ path: '/auth/login', query: { type: 'login' } })
//const goLogout = ()
const goRegister = () => router.push({ path: '/auth/register', query: { type: 'register' } })
const goProfile = (userId) => {
  // If 'me', use the actual current user ID from authStore
  if (userId === 'me' && authStore.userInfo?.userId) {
    router.push({ path: `/profile/${authStore.userInfo.userId}` })
  } else {
    router.push({ path: `/profile/${userId}` })
  }
}
const goStock = (ticker) => router.push({ path: `/stock/${ticker}` })
const goLogout = ()=>{authStore.logout(); router.push({name:'main'})};// 보던페이지로 변경 필요
const extractSource = (url) => {
  try {
    return new URL(url).hostname.replace(/^www\./, '')
  } catch {
    return ''
  }
}

/* 데모 데이터 — API 연동부로 대체 가능 */
const indices = ref([])
const Stocks = ref([])
const fallbackStocks = [
  { id: 'stk1', symbol: 'NVDA', name: 'NVIDIA Corp.', price: 120.34, change: 2.45 },
  { id: 'stk2', symbol: 'TSLA', name: 'Tesla Inc.', price: 215.12, change: -1.18 },
  { id: 'stk3', symbol: 'AAPL', name: 'Apple Inc.', price: 184.72, change: 0.86 },
  { id: 'stk4', symbol: 'MSFT', name: 'Microsoft Corp.', price: 410.09, change: 1.04 },
]

const fallbackNews = [
  {
    id: 'n1',
    image: 'https://picsum.photos/seed/nvda/224/144',
    title: '다시 불거진 AI 버블론…엔비디아, 구세주 될까?',
    date: '2024-08-24',
    source: '이데일리',
    href: '#'
  },
  {
    id: 'n2',
    image: 'https://picsum.photos/seed/ai/224/144',
    title: 'AI 산업의 빅웨이브, 지금이 기회일까?',
    date: '2024-08-24',
    source: '이데일리',
    href: '#'
  },
  { id: 'n3', image: '', title: 'Title', date: '2024-08-21', source: '일부 발행처', href: '#' },
  { id: 'n4', image: '', title: 'Title', date: '2024-08-21', source: '일부 발행처', href: '#' },
]

const news = ref(fallbackNews.map((item) => ({ ...item })))
const investors = ref([])

onMounted(async () => {
  indices.value = [
    { id: 'spx',   title: 'S&P 500', subtitle: '', value: 4567.89, change: 1.23 },
    { id: 'ndaq',  title: 'NASDAQ',  subtitle: '', value: 14234.56, change: -0.89 },
    { id: 'dow',   title: 'DOW',     subtitle: '', value: 34789.12, change: 0.05 },
  ]

  try {
    const { data } = await axios.get('/api/v1/stock')
    const items = Array.isArray(data?.items) ? data.items : []
    Stocks.value = items.length
      ? items.map((item) => ({
          id: item.stockId,
          symbol: item.stockCode || item.name,
          name: item.name,
          price: item.price,
          change: item.fluctuation,
        }))
      : fallbackStocks.map((item) => ({ ...item }))
  } catch (error) {
    console.error('주식 데이터를 불러오지 못했어요', error)
    Stocks.value = fallbackStocks.map((item) => ({ ...item }))
  }


  try {
    const { data } = await axios.get('/api/v1/news/popular-news')
    const items = Array.isArray(data?.items) ? data.items : []
    news.value = items.length
      ? items.map((item) => ({
          id: item.newsId,
          image: item.img_url || '',
          title: item.title,
          date: item.publishedAt,
          source: extractSource(item.link),
          href: item.link,
        }))
      : fallbackNews.map((item) => ({ ...item }))
  } catch (error) {
    console.error('뉴스 데이터를 불러오지 못했습니다.', error)
    news.value = fallbackNews.map((item) => ({ ...item }))
  }

  try {
    const { data } = await axios.get('/api/v1/users/best-predictors')
    const items = Array.isArray(data?.items) ? data.items : []
    investors.value = items.length
      ? items.map((item) => ({
          id: item.userId,
          imageUrl: item.imageUrl || '',
          name: item.name,
          status: item.statusMessage || '', // Use statusMessage instead of tierCode
          tierCode: item.tierCode
        }))
      : fallbackNews.map((item) => ({ ...item }))
  } catch (error) {
    console.error('뉴스 데이터를 불러오지 못했습니다.', error)
    news.value = fallbackNews.map((item) => ({ ...item }))
  }
})

/* 뉴스 2열로 나누기 */
const newsCols = computed(() => {
  const a = [], b = []
  news.value.forEach((n, i) => (i % 2 === 0 ? a : b).push(n))
  return [a, b]
})
</script>

<style scoped>.page {
  padding: 28px 40px 40px;
  display: grid;
  gap: 28px;
  max-width: 1200px;
  margin: 0 auto;
}

/* Hero */
.hero { display:flex; align-items:flex-start; justify-content:space-between; gap:16px; }
.hero__left h1 { margin: 0 0 6px; font-size: 55px; }
.hero__left {margin-left: 30px;}
.tagline { margin: 0; color:#6b7280; }
.hero__actions { display:flex; gap:8px; align-items:center; }
.btn { border-radius:999px; padding:10px 16px; background:#eef2f7; border:1px solid #e5e7eb; }
.btn--primary { background:#111827; color:#fff; border-color:#111827; }

.sec { display: grid; gap: 12px; }
.sec__title { font-size: 18px; margin: 0; }

.hot-stocks { overflow: hidden; }
.hot-stocks__track {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding: 4px 4px 8px;
  margin: 0 -4px;
  scroll-snap-type: x proximity;
  scrollbar-width: thin;
}
.hot-stocks__track::-webkit-scrollbar { height: 6px; }
.hot-stocks__track::-webkit-scrollbar-thumb {
  background: rgba(17, 24, 39, 0.16);
  border-radius: 999px;
}
.hot-stocks__card {
  flex: 0 0 240px;
  scroll-snap-align: start;
}

.news-col { display:grid; gap: 12px; }
</style>
