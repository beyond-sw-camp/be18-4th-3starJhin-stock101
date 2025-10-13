<template>
  <section class="page">

    <!-- 상단: 종목 요약 카드 -->
    <header class="hero">
      <div class="hero-card">
        <div class="hero-main">
          <div v-if="metaPills.length" class="hero-tags">
            <Pill
              v-for="meta in metaPills"
              :key="meta.key"
              :text="meta.text"
              :variant="meta.variant"
              size="sm"
              tone="soft"
              class="hero-pill"
            />
          </div>
          <h1 class="hero-title">{{ tickerDisplay }}</h1>
          <p class="hero-company">{{ companyDisplay }}</p>
          <p v-if="headerDescription" class="hero-desc">{{ headerDescription }}</p>

          <div v-if="indicatorPills.length" class="hero-indicators">
            <div v-for="indicator in indicatorPills" :key="indicator.key" class="indicator-item">
              <span class="indicator-label">{{ indicator.title }}</span>
              <Pill :text="indicator.text" :variant="indicator.variant" tone="solid" size="sm" />
            </div>
          </div>
        </div>

        <div class="hero-price">
          <span class="price-caption">현재가</span>
          <span class="price-value">{{ priceNowText }}</span>
          <span v-if="deltaText" :class="['price-delta', 'delta', deltaClass]">{{ deltaText }}</span>
        </div>
      </div>
    </header>

    <!-- 투자 나침반(게이지 3개) -->
    <section class="sec">
      <h2 class="sec-title">{{ tickerDisplay }}의 투자 나침반</h2>
      <div class="gauges">
        <div v-for="gauge in gaugeConfigs" :key="gauge.key" class="gauge-block">
          <div class="gauge-header">
            <h3 class="gauge-title">{{ gauge.title }}</h3>
            <Pill
              :text="gauge.state"
              :variant="gauge.stateVariant"
              tone="outline"
              size="sm"
            />
          </div>
          <div class="gauge-body">
            <div class="gauge-chart">
              <SentimentGauge
                :value="gauge.value"
                :size="gauge.size"
                :segments="gaugeColorSegments"
                :band-labels="gauge.bandLabels ?? bandLabels"
                :label-distance="gauge.labelDistance"
                :thickness="gauge.thickness"
                :radius="gauge.radius"
                :labels-outside="gauge.labelsOutside"
              />
            </div>
          </div>
          <p v-if="gauge.updateText" class="gauge-update">{{ gauge.updateText }}</p>
        </div>
      </div>
    </section>

    <!-- 재무 지표 -->
    <section class="sec">
      <h2 class="sec-title">{{ tickerDisplay }}의 재무 지표</h2>
      <div class="metrics-grid">
        <BaseGrid :items="metrics" :cols="4" gap="15px" itemKey="key">
          <template #default="{ item }">
            <MetricCard
              :title="item.title"
              :subtitle="item.subtitle"
              :value="item.value"
              :badgeText="item.badgeText"
              :badgeVariant="item.variant"
              pad="lg"
            />
          </template>
        </BaseGrid>
      </div>
    </section>

    <!--차트 및 주가 예측-->
    <section class="sec">
      <Chart :ticker="tickerDisplay"/>
      <div class="cta-row">
        <BaseButton class="cta neg" :class="{ 'cta-locked': !isLoggedIn }" @click="prediction(false)">
          <span class="cta-content">
            <span class="cta-icon" aria-hidden="true">▼</span>
            <span class="cta-text">
              <span class="cta-title">주가가 내려갈 거예요</span>
              <span class="cta-sub">하락 예상 시 기록하고 추이를 추적해요</span>
            </span>
          </span>
        </BaseButton>
        <BaseButton class="cta pos" :class="{ 'cta-locked': !isLoggedIn }" @click="prediction(true)">
          <span class="cta-content">
            <span class="cta-icon" aria-hidden="true">▲</span>
            <span class="cta-text">
              <span class="cta-title">주가가 올라갈 거예요</span>
              <span class="cta-sub">상승 신호를 느꼈다면 지금 남겨보세요</span>
            </span>
          </span>
        </BaseButton>
      </div>
      <p class="hint" :class="{ disabled: !isLoggedIn }">
        {{ isLoggedIn ? '[등록하면 주가를 이메일로 보내요]' : '로그인 후 이용 가능한 기능입니다.' }}
      </p>
    </section>

    <!-- 커뮤니티 대화 -->
    <section class="sec">
      <h2 class="sec-title">{{ tickerDisplay }}의 커뮤니티 대화</h2>
      <div class="community-wrapper">
        <CommunityFeed :stockId="Number(stockId)" :embedded="true" />
      </div>
    </section>
  </section>
  </template>
<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

import BaseGrid from '@/components/grid/BaseGrid.vue'
import MetricCard from '@/components/card/variants/MetricCard.vue'
import SentimentGauge from '@/components/chart/variants/GaugeChart.vue'
import Pill from '@/components/ui/Pill.vue'
import Chart from '@/components/chart/Chart.vue'
import CommunityFeed from '@/views/CommunityFeedView.vue'


const route = useRoute()
const router = useRouter()
const stockId = ref(route.params.stockId ?? '')
const stockInfo = ref(null)
const priceValue = ref(null)
const changeValue = ref(null)

const tickerDisplay = computed(() => stockInfo.value?.stockCode ?? stockInfo.value?.symbol ?? stockInfo.value?.ticker ?? '—')
const companyDisplay = computed(() => stockInfo.value?.name ?? '—')
const readStoredToken = () => {
  const raw = localStorage.getItem('AuthToken') ?? localStorage.getItem('authToken') ?? ''
  const token = typeof raw === 'string' ? raw.trim() : ''
  if (!token || token === 'null' || token === 'undefined') return ''
  return token
}

const authToken = ref(readStoredToken())
const syncToken = () => {
  authToken.value = readStoredToken()
}
const isLoggedIn = computed(() => authToken.value.length > 0)

const formatCurrency = (value) => {
  const num = Number(value)
  if (!Number.isFinite(num)) return '—'
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(num)
}

const priceNowText = computed(() => formatCurrency(priceValue.value))

const deltaText = computed(() => {
  if (!Number.isFinite(changeValue.value)) return ''
  const pct = changeValue.value
  const sign = pct > 0 ? '+' : pct < 0 ? '-' : ''
  const value = Math.abs(pct).toFixed(2)
  return `${sign}${value}%`
})

const deltaClass = computed(() => {
  if (!Number.isFinite(changeValue.value)) return 'flat'
  if (changeValue.value > 0) return 'pos'
  if (changeValue.value < 0) return 'neg'
  return 'flat'
})

const indicatorMap = {
  STRONG_BUY: { variant: 'success', label: 'Strong Buy' },
  BUY: { variant: 'success', label: 'Buy' },
  HOLD: { variant: 'warning', label: 'Hold' },
  SELL: { variant: 'danger', label: 'Sell' },
  STRONG_SELL: { variant: 'danger', label: 'Strong Sell' },
  POSITIVE: { variant: 'success', label: 'Positive' },
  NEGATIVE: { variant: 'danger', label: 'Negative' },
  NEUTRAL: { variant: 'warning', label: 'Neutral' },
}

const indicatorPills = computed(() => {
  if (!stockInfo.value) return []
  const entries = [
    { key: 'individualIndicator', title: '개미 시그널' },
    { key: 'analystIndicator', title: '애널리스트' },
    { key: 'newsIndicator', title: '뉴스' },
  ]
  return entries
    .map(({ key, title }) => {
      const raw = stockInfo.value?.[key]
      if (!raw) return null
      const mapped = indicatorMap[raw] ?? {}
      return {
        key,
        title,
        text: mapped.label ?? raw,
        variant: mapped.variant ?? 'info',
      }
    })
    .filter(Boolean)
})

const metaPills = computed(() => {
  if (!stockInfo.value) return []
  const items = []
  if (tickerDisplay.value && tickerDisplay.value !== '—') items.push({ key: 'ticker', text: tickerDisplay.value, variant: 'info' })
  if (stockInfo.value.industryName) items.push({ key: 'industry', text: stockInfo.value.industryName, variant: 'neutral' })
  if (stockInfo.value.sectorName) items.push({ key: 'sector', text: stockInfo.value.sectorName, variant: 'neutral' })
  return items
})

const headerDescription = computed(() => {
  if (!stockInfo.value) return ''
  const parts = [stockInfo.value.industryName, stockInfo.value.sectorName].filter(Boolean)
  return parts.join(' · ')
})

/* 게이지 데모 값 */
const sentiment = ref(0)
const miniLeft = ref(0)
const miniRight = ref(0)

const bandLabels = ['Strong Sell', 'Sell', 'Hold', 'Buy', 'Strong Buy']

const gaugeColorSegments = [
  [0.2, '#ef4444'],
  [0.4, '#f97316'],
  [0.6, '#facc15'],
  [0.8, '#22c55e'],
  [1.0, '#16a34a'],
]
const gaugeStateText = (value) =>
  value > 80 ? 'Strong Buy'
    : value > 60 ? 'Buy'
    : value > 40 ? 'Hold'
    : value > 20 ? 'Sell'
    : 'Strong Sell'

const gaugeStateVariant = (state) => {
  if (state.includes('Strong Buy')) return 'success'
  if (state === 'Buy') return 'success'
  if (state === 'Hold') return 'warning'
  if (state.includes('Sell')) return 'danger'
  return 'neutral'
}

const gaugeConfigs = computed(() => {
  const configs = [
    { key: 'mood', title: '분석가 지표', value: miniLeft.value, size: 360, labelDistance: 46, thickness: 24, radius: '85%', labelsOutside: true },
    {
      key: 'retail',
      title: '개미 지표',
      value: sentiment.value,
      size: 420,
      labelDistance: 50,
      thickness: 24,
      radius: '82%',
      labelsOutside: true,
      bandLabels: ['Strong Sell', 'Sell', '·', 'Buy', 'Strong Buy']
    },
    { key: 'news', title: '뉴스 지표', value: miniRight.value, size: 360, labelDistance: 46, thickness: 24, radius: '85%', labelsOutside: true },
  ]

  return configs.map((config) => {
    const state = gaugeStateText(config.value)
    return {
      ...config,
      state,
      stateVariant: gaugeStateVariant(state),
    }
  })
})

/* 재무 지표 카드 데이터 */
const metrics = ref([
  { key: 'per1', title: 'PER', subtitle: 'Price-to-Earnings Ratio', value: 4.2, badgeText: 'neutral', variant: 'neutral' },
  { key: 'per2', title: 'PER', subtitle: 'Price-to-Earnings Ratio', value: 4.2, badgeText: 'poor',    variant: 'danger'  },
  { key: 'ebs',  title: 'EBS', subtitle: 'Earnings Before Split',   value: 4.2, badgeText: 'good',    variant: 'success' },
])
const loadprice = async () =>{
  await axios.get('/api/v1/rest-client/getStockPrice')
}

const loadStock = async (id) => {
  if (!id) return
  try {
    const { data } = await axios.get(`/api/v1/stock/${id}`)
    const items = Array.isArray(data?.items) ? data.items : []
    const stock = items[0]
    if (!stock) return

    stockInfo.value = stock
    priceValue.value = Number(stock.price)
    changeValue.value = Number(stock.fluctuation)
  } catch (error) {
    console.error('주식 데이터를 불러오지 못했습니다.', error)
  }
}
const getindicator = async (id) =>{
  if (!id) return
  try {
    const { data } = await axios.get(`/api/v1/indicator-service/news-indicator?stockId=${id}`)
    const items = Array.isArray(data?.items) ? data.items : []
    const ni = items[0]
    if (!ni) return
    let newsval =  ((ni.negativeCount * 0 ) + (ni.neutralCount * 50 ) + (ni.positiveCount * 100 ))/(ni.negativeCount + ni.neutralCount + ni.positiveCount)
    miniRight.value = Number(newsval)
    console.log(miniRight.value)
  } catch (error) {
    console.error('주식 데이터를 불러오지 못했습니다.', error)
  }
  try {
    const { data } = await axios.get(`/api/v1/indicator-service/individual-indicator?stockId=${id}`)
    const items = Array.isArray(data?.items) ? data.items : []
    const ii = items[0]
    if (!ii) return
    let indivval =  ((ii.strongBuy * 100 ) + (ii.buy * 75 ) + (ii.hold * 50 ) + (ii.sell * 25 ) + (ii.strongSell * 0 ))/(ii.strongBuy + ii.buy + ii.hold + ii.sell + ii.strongSell)
    sentiment.value = Number(indivval)
    console.log(sentiment.value)
  } catch (error) {
    console.error('주식 데이터를 불러오지 못했습니다.', error)
  }
  miniLeft.value = (miniRight.value + sentiment.value)/2
  console.log(miniLeft.value)
}
function mapFinancialToMetrics(fin) {
  return [
    {
      key: 'eps',
      title: 'EPS',
      subtitle: 'Earnings Per Share',
      value: fin.eps,
      badgeText: fin.eps > 0 ? 'good' : 'poor',
      variant: fin.eps > 0 ? 'success' : 'danger'
    },
    {
      key: 'bps',
      title: 'BPS',
      subtitle: 'Book Value Per Share',
      value: fin.bps,
      badgeText: 'neutral',
      variant: 'neutral'
    },
    {
      key: 'roe',
      title: 'ROE',
      subtitle: 'Return on Equity',
      value: fin.roe,
      badgeText: fin.roe > 1 ? 'good' : 'poor',
      variant: fin.roe > 1 ? 'success' : 'danger'
    },
    {
      key: 'roa',
      title: 'ROA',
      subtitle: 'Return on Assets',
      value: fin.roa,
      badgeText: fin.roa > 0.5 ? 'good' : 'poor',
      variant: fin.roa > 0.5 ? 'success' : 'danger'
    }
  ]
}
const getfinance = async(id) =>{
  if (!id) return
  try {
    const { data } = await axios.get(`/api/v1/indicator-service/financial-indicator?stockId=${id}`)
    const items = Array.isArray(data?.items) ? data.items : []
    const fi = items[0]
    if (!fi) return
    metrics.value = mapFinancialToMetrics(fi)
    console.log(metrics.value)
  } catch (error) {
    console.error('주식 데이터를 불러오지 못했습니다.', error)
  }
}
const buildPredictionPayload = (isBullish) => ({
  stockId: stockId.value,
  userId: localStorage.getItem("userId"),
  ticker: tickerDisplay.value,
  prediction: isBullish ? 'UP' : 'DOWN',
  // TODO: confidence, memo, userId 등을 서버 스펙에 맞게 채워주세요.
  predicted_at: new Date().toISOString()
})

const prediction = async (isBullish) => {
  if (!isLoggedIn.value) {
    alert('로그인 후 이용해 주세요.')
    router.push({path: "/auth/login?type=login"})
    return
  }
  if (!stockId.value || !tickerDisplay.value || tickerDisplay.value === '—') {
    console.warn('예측을 등록할 종목 정보를 확인할 수 없습니다.')
    return
  }

  const payload = buildPredictionPayload(isBullish)

  try {
    await axios.post('/api/v1/prediction/create', payload,{
      headers: {
        Authorization: `Bearer ${authToken.value}`
        // TODO: 필요 시 Content-Type 등 추가 헤더를 정의하세요.
      }
    })
    console.log("등록 완료")
    alert("등록 완료")
  } catch (error) {
    console.error('예측을 등록하지 못했습니다.', error)
    alert('오류가 발생하였습니다')
  }
}

onMounted(() => {
  syncToken()
  window.addEventListener('storage', syncToken)

  loadprice()
  loadStock(stockId.value)
  getindicator(stockId.value)
  getfinance(stockId.value)
})

onUnmounted(() => {
  window.removeEventListener('storage', syncToken)
})

watch(
  () => route.params.stockId,
  (next) => {
    if (typeof next === 'undefined') return
    stockId.value = next
    loadStock(next)
  }
)
</script>

<style scoped>
.page{
  display:grid;
  gap:28px;
  padding:42px;
  max-width:1020px;
  width:100%;
  margin:0 auto;
  background:#f8fafc;
  border-radius:24px;
  box-sizing:border-box;
}

.hero{ display:flex; }
.hero-card{
  display:flex; justify-content:space-between; align-items:flex-start; gap:32px;
  width:100%; padding:32px; border-radius:30px;
  background:linear-gradient(135deg, rgba(59,130,246,0.15) 0%, rgba(14,165,233,0.18) 50%, rgba(236,72,153,0.16) 100%);
  border:1px solid rgba(148,163,184,0.18);
  box-shadow:0 28px 50px rgba(15,23,42,0.18);
  position:relative; overflow:hidden;
}

.hero-card::after{
  content:""; position:absolute; inset:-140px -160px auto auto; width:420px; height:420px;
  background:radial-gradient(circle at center, rgba(59,130,246,0.28), transparent 65%);
  pointer-events:none; mix-blend-mode:screen;
}

.hero-main{ display:flex; flex-direction:column; gap:16px; max-width:60%; position:relative; z-index:1; }
.hero-tags{ display:flex; flex-wrap:wrap; gap:8px; }
.hero-pill{ box-shadow:0 4px 16px rgba(37,99,235,0.18); }
.hero-title{ margin:0; font-size:40px; font-weight:800; color:#0f172a; letter-spacing:-0.01em; }
.hero-company{ margin:0; font-size:22px; font-weight:600; color:#1f2937; }
.hero-desc{ margin:0; font-size:0.95rem; color:#475569; }

.hero-indicators{ display:flex; flex-wrap:wrap; gap:12px 20px; }
.indicator-item{ display:flex; align-items:center; gap:8px; }
.indicator-label{ font-size:0.85rem; font-weight:600; color:#334155; }

.hero-price{ position:relative; z-index:1; display:flex; flex-direction:column; align-items:flex-end; gap:6px; text-align:right; }
.price-caption{ font-size:0.85rem; color:#475569; }
.price-value{ font-size:36px; font-weight:800; color:#0f172a; }
.price-delta{ font-size:1rem; font-weight:700; }

.delta{ font-weight:600; }
.delta.pos{ color:#16a34a; } .delta.neg{ color:#dc2626; } .delta.flat{ color:#4b5563; }

.sec{ display:grid; gap:16px; }
.sec-title{ margin:0; font-size:20px; font-weight:700; color:#0f172a; }

.gauges{
  display:grid;
  grid-template-columns: 1fr 1.35fr 1fr;
  gap:22px;
  align-items:center;
  justify-items:stretch;
}


.gauge-block{
  display:flex;
  flex-direction:column;
  gap:12px;
  padding:14px 12px 16px;
  border:1px solid rgba(148,163,184,0.18);
  border-radius:18px;
  background:#ffffff;
  box-shadow:0 10px 20px rgba(15,23,42,0.1);
  min-height:0;
}

.gauge-header{ display:flex; align-items:center; justify-content:space-between; width:100%; gap:12px; }
.gauge-title{ margin:0; font-size:18px; font-weight:700; color:#111827; }

.gauge-body{
  background:linear-gradient(180deg, rgba(241,245,249,0.45) 0%, rgba(241,245,249,0.12) 100%);
  border-radius:14px;
  padding:4px 6px 10px;
  display:flex;
  align-items:center;
  justify-content:center;
  min-height:0;
  overflow:visible;
}
.gauge-chart{
  flex:1 1 auto;
  min-width:220px;
  max-height:220px;
  display:flex;
  align-items:center;
  justify-content:center;
  overflow:visible;
}
.gauge-update{ margin:0; font-size:0.85rem; color:#6b7280; }

.metrics-grid{
  --metric-card-min-height: 0px;
}
.metrics-grid :deep(.metric-card){
  height:100%;
  background:linear-gradient(180deg, rgba(255,255,255,0.95) 0%, rgba(248,250,252,0.96) 100%);
  border:1px solid rgba(148,163,184,0.22);
  box-shadow:0 10px 20px rgba(15,23,42,0.08);
  display:flex;
  flex-direction:column;
}
.metrics-grid :deep(.metric-card .row.head){ margin-bottom:12px; }
.metrics-grid :deep(.metric-card .title){ font-size:18px; }
.metrics-grid :deep(.metric-card .content){ min-height:0; gap:12px; }
.metrics-grid :deep(.metric-card .subtitle){ font-size:0.85rem; color:#64748b; }
.metrics-grid :deep(.metric-card .value){ font-size:1.4rem; font-weight:700; color:#0f172a; }

.cta-row{
  margin-top:20px;
  padding:12px 0;
  display:grid;
  grid-template-columns:repeat(auto-fit, minmax(260px,1fr));
  gap:16px;
}
.cta{
  width:100%;
  padding:0;
  border:none;
  background:transparent;
}
.cta:focus-visible .cta-content{
  outline:3px solid rgba(59,130,246,0.45);
  outline-offset:3px;
}
.cta.cta-locked .cta-content{
  filter:grayscale(0.25);
  opacity:0.65;
  box-shadow:none;
}
.cta.cta-locked:hover .cta-content{
  transform:none;
  box-shadow:none;
}
.cta.cta-locked .cta-icon{
  opacity:0.75;
}
.cta.cta-locked .cta-title,
.cta.cta-locked .cta-sub{
  color:rgba(15,23,42,0.55);
}
.cta-content{
  position:relative;
  display:flex;
  align-items:center;
  gap:18px;
  width:100%;
  padding:20px 24px;
  border-radius:20px;
  transition:transform 0.2s ease, box-shadow 0.2s ease;
  box-shadow:0 14px 24px rgba(15,23,42,0.08);
}
.cta:hover .cta-content{
  transform:translateY(-4px);
  box-shadow:0 20px 38px rgba(15,23,42,0.16);
}
.cta-icon{
  flex:0 0 48px;
  height:48px;
  border-radius:16px;
  display:flex;
  align-items:center;
  justify-content:center;
  font-size:1.35rem;
  font-weight:700;
  background:rgba(255,255,255,0.38);
}
.cta-text{
  display:flex;
  flex-direction:column;
  gap:6px;
}
.cta-title{
  font-size:1.05rem;
  font-weight:700;
  letter-spacing:-0.01em;
}
.cta-sub{
  font-size:0.9rem;
  color:rgba(15,23,42,0.65);
}
.cta.neg .cta-content{
  background:linear-gradient(135deg, rgba(254,226,226,0.95) 0%, rgba(252,165,165,0.85) 100%);
  border:1px solid rgba(248,113,113,0.45);
  color:#9f1239;
}
.cta.neg .cta-icon{
  color:#be123c;
  background:rgba(255,255,255,0.58);
}
.cta.neg .cta-sub{ color:rgba(153,27,27,0.75); }
.cta.pos .cta-content{
  background:linear-gradient(135deg, rgba(217,249,255,0.95) 0%, rgba(187,247,208,0.9) 100%);
  border:1px solid rgba(52,211,153,0.45);
  color:#047857;
}
.cta.pos .cta-icon{
  color:#0f766e;
  background:rgba(255,255,255,0.6);
}
.cta.pos .cta-sub{ color:rgba(22,101,52,0.75); }

@media (max-width: 1024px){
  .page{ padding:34px; }
  .hero-card{ flex-direction:column; align-items:flex-start; }
  .hero-main{ max-width:100%; }
  .hero-price{ align-items:flex-start; text-align:left; }
  .gauges{ grid-template-columns:repeat(auto-fit, minmax(280px, 1fr)); }
}

@media (max-width: 768px){
  .page{ padding:24px; border-radius:20px; }
  .hero-card{ padding:24px; }
  .hero-title{ font-size:32px; }
  .hero-company{ font-size:18px; }
  .gauges{ gap:18px; }
  .gauge-block{ padding:12px 10px 14px; }
}

@media (max-width: 720px){
  .cta-row{ grid-template-columns: 1fr; }
  .cta-content{
    padding:18px 20px;
  }
}
.hint{ text-align:center; color:#9ca3af; font-size:.9rem; transition:color 0.2s ease; }
.hint.disabled{ color:#ef4444; font-weight:600; }

.op-list{ display:grid; gap:10px; }

.community-wrapper {
  background: #ffffff;
  border: 1px solid rgba(148,163,184,0.18);
  border-radius: 18px;
  box-shadow: 0 10px 20px rgba(15,23,42,0.1);
}

.community-wrapper :deep(.community-feed) {
  padding: 24px;
  margin: 0;
  max-width: none;
}

.community-wrapper :deep(.community-feed__back),
.community-wrapper :deep(.community-feed__title) {
  display: none;
}
</style>
