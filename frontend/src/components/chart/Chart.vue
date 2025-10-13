<template>
  <div class="chart-wrapper">
    <div class="chart-meta">
      <span class="chart-symbol">{{ symbol }}</span>
      <span class="chart-last-price" v-if="lastPriceDisplay">{{ lastPriceDisplay }}</span>
    </div>
    <apexchart type="candlestick" :options="chartOptions" :series="series"></apexchart>
  </div>
</template>

<script>
import VueApexCharts from 'vue3-apexcharts'

const START_TS = new Date('2024-06-10T09:30:00-04:00').getTime()
const HISTORICAL_ENDPOINT = 'http://localhost:8080/api/v1/rest-client/getStockPrice'

const toPrice = (value) => Number(value.toFixed(2))

const randomBetween = (min, max) => Math.random() * (max - min) + min

const mapApiRecordToCandle = (record) => {
  if (!record) return null
  const timeValue = record.datetime || record.dateTime || record.time
  const timestamp = timeValue ? new Date(timeValue).getTime() : Number.NaN
  const open = Number(record.open)
  const high = Number(record.high)
  const low = Number(record.low)
  const close = Number(record.last ?? record.close)

  if (!Number.isFinite(timestamp) || !Number.isFinite(open) || !Number.isFinite(high) || !Number.isFinite(low) || !Number.isFinite(close)) {
    return null
  }

  const openVal = toPrice(open)
  const closeVal = toPrice(close)
  const highVal = toPrice(Math.max(high, openVal, closeVal))
  const lowVal = toPrice(Math.max(0, Math.min(low, openVal, closeVal)))

  return { x: timestamp, y: [openVal, highVal, lowVal, closeVal] }
}

const createDummyCandles = (count = 200, intervalMinutes = 5) => {
  const candles = []
  const intervalMs = intervalMinutes * 60 * 1000
  let lastClose = 178.35

  for (let i = 0; i < count; i += 1) {
    const timestamp = START_TS + i * intervalMs
    const open = lastClose
    const bias = Math.sin(i / 18) * 0.75 + Math.cos(i / 27) * 0.45
    const randomShock = randomBetween(-1.35, 1.35)
    const close = open + bias + randomShock
    const highRange = Math.abs(randomBetween(0.25, 1.4))
    const lowRange = Math.abs(randomBetween(0.25, 1.4))
    const high = Math.max(open, close) + highRange
    const low = Math.min(open, close) - lowRange

    const openVal = toPrice(open)
    const closeVal = toPrice(close)
    const highVal = toPrice(Math.max(high, openVal, closeVal))
    const lowVal = toPrice(Math.max(0, Math.min(low, openVal, closeVal)))

    candles.push({ x: timestamp, y: [openVal, highVal, lowVal, closeVal] })
    lastClose = close
  }

  return candles
}

const DUMMY_CANDLES = createDummyCandles()

export default {
  components: { apexchart: VueApexCharts },
  props: {
    ticker: {
      type: String,
      default: 'AAPL'
    }
  },
  data() {
    return {
      maxPoints: 240,
      refreshMs: 60_000,
      refreshTimer: null,
      series: [{ data: [] }],
      chartOptions: {
        chart: {
          type: 'candlestick',
          background: '#f9fbff',
          foreColor: '#1e2a4a',
          parentHeightOffset: 0,
          fontFamily:
            "'IBM Plex Sans', 'Spoqa Han Sans Neo', 'Apple SD Gothic Neo', sans-serif",
          toolbar: { show: false },
          animations: {
            enabled: true,
            easing: 'easeinout',
            speed: 600,
            dynamicAnimation: { enabled: true, speed: 350 }
          }
        },
        theme: { mode: 'light' },
        plotOptions: {
          candlestick: {
            colors: { upward: '#ff5252', downward: '#1f87ff' },
            wick: { useFillColor: true }
          }
        },
        annotations: {
          xaxis: []
        },
        dataLabels: { enabled: false },
        stroke: { show: true, width: 1 },
        grid: {
          borderColor: 'rgba(180, 195, 220, 0.6)',
          strokeDashArray: 4,
          padding: { left: 18, right: 18, top: 6, bottom: 6 }
        },
        xaxis: {
          type: 'datetime',
          tooltip: { enabled: false },
          crosshairs: {
            show: true,
            stroke: { color: 'rgba(64, 124, 255, 0.65)', width: 1, dashArray: 3 }
          },
          labels: {
            datetimeUTC: false,
            style: { colors: '#425b8f', fontWeight: 500 }
          },
          axisBorder: { color: 'rgba(128, 158, 214, 0.65)' },
          axisTicks: { color: 'rgba(128, 158, 214, 0.65)' }
        },
        yaxis: {
          tooltip: { enabled: true },
          labels: {
            formatter(val) {
              return typeof val === 'number' ? `${val.toFixed(2)} USD` : ''
            },
            style: { colors: '#425b8f', fontWeight: 500 },
            offsetX: -6
          }
        },
        tooltip: {
          theme: 'light',
          style: { fontFamily: "'IBM Plex Sans', sans-serif" },
          x: { format: 'MMM dd, HH:mm' },
          y: {
            formatter(val) {
              return typeof val === 'number' ? `${val.toFixed(2)} USD` : ''
            }
          }
        },
        legend: { show: false },
        noData: {
          text: '해외 시장 데이터를 불러오는 중입니다...',
          align: 'center',
          style: {
            color: '#425b8f',
            fontSize: '14px',
            fontFamily: "'IBM Plex Sans', 'Spoqa Han Sans Neo', sans-serif"
          }
        }
      }
    }
  },
  computed: {
    activeTicker() {
      const value = typeof this.ticker === 'string' ? this.ticker.trim().toUpperCase() : ''
      return value || 'AAPL'
    },
    symbol() {
      return this.activeTicker
    },
    lastPrice() {
      const data = this.series?.[0]?.data
      if (!Array.isArray(data) || data.length === 0) return null
      const latest = data[data.length - 1]
      return Array.isArray(latest?.y) ? Number(latest.y[3]) : null
    },
    lastPriceDisplay() {
      if (this.lastPrice === null || Number.isNaN(this.lastPrice)) return ''
      return `${this.lastPrice.toFixed(2)} USD`
    }
  },
  mounted() {
    this.bootstrapChart()
  },
  beforeUnmount() {
    this.clearRefreshTimer()
  },
  watch: {
    ticker() {
      this.bootstrapChart()
    }
  },
  methods: {
    async bootstrapChart() {
      this.clearRefreshTimer()
      const loaded = await this.loadHistoricalData(this.activeTicker)
      if (!loaded) {
        this.seedDummyData()
      }
      this.scheduleRefresh()
    },
    async refreshOnce() {
      await this.loadHistoricalData(this.activeTicker)
    },
    scheduleRefresh() {
      this.clearRefreshTimer()
      this.refreshTimer = setInterval(this.refreshOnce, this.refreshMs)
    },
    clearRefreshTimer() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    },
    async loadHistoricalData(symbol = this.activeTicker) {
      try {
        const response = await fetch(`${HISTORICAL_ENDPOINT}/${encodeURIComponent(symbol)}`)
        if (!response.ok) throw new Error(`HTTP ${response.status}`)
        const payload = await response.json()
        const items = Array.isArray(payload?.items) ? payload.items : Array.isArray(payload) ? payload : []
        const candles = items
          .map(mapApiRecordToCandle)
          .filter(Boolean)
          .sort((a, b) => a.x - b.x)

        if (candles.length) {
          const trimmed = candles.slice(-this.maxPoints)
          this.series = [{ data: trimmed }]
          this.applySessionShading(trimmed)
          return true
        }
      } catch (error) {
        console.error('해외 주식 시세를 불러오지 못했습니다.', error)
      }
      return false
    },
    seedDummyData() {
      const fallback = DUMMY_CANDLES.map((candle) => ({ ...candle }))
      this.series = [{ data: fallback }]
      this.applySessionShading(fallback)
    },
    applySessionShading(points = []) {
      const dataPoints = Array.isArray(points) ? points : []
      if (!dataPoints.length) {
        this.chartOptions = {
          ...this.chartOptions,
          annotations: { ...(this.chartOptions.annotations || {}), xaxis: [] }
        }
        return
      }

      const seen = new Set()
      const zones = []

      dataPoints.forEach((point) => {
        const timestamp = Number(point?.x)
        if (!Number.isFinite(timestamp)) return
        const sessionStart = new Date(timestamp)
        if (!Number.isFinite(sessionStart.getTime())) return
        sessionStart.setHours(9, 0, 0, 0)
        const key = sessionStart.getTime()
        if (seen.has(key)) return
        seen.add(key)

        const sessionEnd = new Date(sessionStart)
        sessionEnd.setHours(17, 0, 0, 0)
        if (sessionEnd <= sessionStart) {
          sessionEnd.setTime(sessionStart.getTime() + 8 * 60 * 60 * 1000)
        }

        zones.push({
          x: sessionStart.getTime(),
          x2: sessionEnd.getTime(),
          fillColor: 'rgba(15, 23, 42, 0.08)',
          opacity: 1,
          borderColor: 'rgba(59, 130, 246, 0.12)'
        })
      })

      zones.sort((a, b) => a.x - b.x)

      this.chartOptions = {
        ...this.chartOptions,
        annotations: {
          ...(this.chartOptions.annotations || {}),
          xaxis: zones
        }
      }
    }
  }
}
</script>

<style scoped>
.chart-wrapper {
  background: linear-gradient(145deg, #ffffff 0%, #f0f6ff 100%);
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 18px 36px rgba(23, 58, 136, 0.18);
  border: 1px solid rgba(139, 176, 232, 0.45);
}

.chart-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 12px;
}

.chart-symbol {
  font-size: 1.25rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: #1c2f5c;
  text-transform: uppercase;
}

.chart-last-price {
  font-size: 0.95rem;
  font-weight: 600;
  color: #1f87ff;
  background: rgba(31, 135, 255, 0.1);
  padding: 6px 10px;
  border-radius: 999px;
}

.chart-wrapper :deep(.apexcharts-tooltip.apexcharts-theme-light) {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(140, 174, 232, 0.6);
  box-shadow: 0 10px 28px rgba(40, 88, 168, 0.18);
}

.chart-wrapper :deep(.apexcharts-tooltip-text) {
  color: #1e2a4a;
}
</style>
