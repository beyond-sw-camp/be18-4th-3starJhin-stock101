<template>
  <span class="state-badge" :class="[`v-${variant}`, `s-${size}`]" :title="title" aria-hidden="true">
    <slot>
      <!-- 기본 아이콘: up/down/flat에 맞춰 표시 (원하면 자유롭게 교체 가능) -->
      <svg v-if="kind==='up'" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor" aria-hidden="true">
        <path d="M5 15l6-6 4 4 4-4v6h-2V9.83l-2.59 2.58L11 10.0 6.41 14.59 5 13.17V15z"/>
      </svg>
      <svg v-else-if="kind==='down'" viewBox="0 0 24 24" width="1em" height="1em" fill="currentColor" aria-hidden="true">
        <path d="M5 9l6 6 4-4 4 4V9h-2v5.17l-2.59-2.58L11 14 6.41 9.41 5 10.83V9z"/>
      </svg>
      <svg v-else viewBox="0 0 24 24" width="1em" height="1em" fill="none" stroke="currentColor" stroke-width="2"
           stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        <path d="M3 12h5l2 3 3-6 3 3h5"/>
      </svg>
    </slot>
  </span>
</template>

<script setup>
const props = defineProps({
  // 의미 계열 색
  variant: { type: String, default: 'success' }, // success|info|danger|neutral
  // 아이콘 종류(기본 제공), 직접 슬롯으로 SVG 넘기면 무시됨
  kind: { type: String, default: 'up' },         // up|down|flat
  // 크기
  size: { type: String, default: 'md' },         // sm|md|lg
  // 접근성용 힌트(툴팁)
  title: { type: String, default: '' }
})
</script>

<style scoped>
.state-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  font-size: 18px; /* md 기본 */
}
.s-sm { font-size: 16px; }
.s-lg { font-size: 20px; }

.v-success { color: #16a34a; } /* 초록 */
.v-info    { color: #2563eb; } /* 파랑 */
.v-danger  { color: #dc2626; } /* 빨강 */
.v-neutral { color: #6b7280; } /* 회색 */
</style>
