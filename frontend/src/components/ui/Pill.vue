<template>
  <component
    :is="as"
    class="pill"
    :class="[`v-${variant}`, `t-${tone}`, `s-${size}`, { clickable, active }]"
    :aria-pressed="clickable ? String(active) : undefined"
    :tabindex="clickable ? 0 : undefined"
  >
    <slot name="icon" />
    <slot>{{ text }}</slot>
  </component>
</template>

<script setup>
const props = defineProps({
  text: { type: String, default: '' },
  // 의미 계열
  variant: { type: String, default: 'neutral' }, // neutral|success|danger|info|warning
  // 표현 방식
  tone: { type: String, default: 'soft' },       // soft|solid|outline
  // 크기
  size: { type: String, default: 'md' },         // sm|md
  // 선택형 칩처럼 쓸 때
  clickable: { type: Boolean, default: false },
  active: { type: Boolean, default: false },
  // 태그
  as: { type: String, default: 'span' }          // 'span'|'button'|'a'
})
</script>

<style scoped>
/* 기본 토큰(컴포넌트 스코프) */
.pill {
  --bg: #fff;
  --bd: transparent;
  --fg: #111827;

  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 10px;
  border-radius: 999px;
  border: 1px solid var(--bd);
  background: var(--bg);
  color: var(--fg);
  font-weight: 600;
  line-height: 1.6;
  white-space: nowrap;
  font-size: .875rem;
}

.s-sm { padding: 1px 8px; font-size: .8125rem; gap: 4px; }

/* 팔레트(스코프 내부 변수로 정의) */
.pill.v-neutral { --bg-soft:#FEF9C3; --bd-soft:#FDE68A; --fg-soft:#854D0E; --fg-solid:#854D0E; }
.pill.v-success { --bg-soft:#DCFCE7; --bd-soft:#86EFAC; --fg-soft:#166534; --fg-solid:#166534; }
.pill.v-danger  { --bg-soft:#FEE2E2; --bd-soft:#FCA5A5; --fg-soft:#991B1B; --fg-solid:#991B1B; }
.pill.v-info    { --bg-soft:#DBEAFE; --bd-soft:#93C5FD; --fg-soft:#1E3A8A; --fg-solid:#1E3A8A; }
.pill.v-warning { --bg-soft:#FFEDD5; --bd-soft:#FDBA74; --fg-soft:#9A3412; --fg-solid:#9A3412; }

/* tone 조합 */
.pill.t-soft   { --bg: var(--bg-soft); --bd: var(--bd-soft); --fg: var(--fg-soft); }
.pill.t-solid  { --bg: var(--fg-solid); --bd: var(--fg-solid); --fg: #fff; }
.pill.t-outline{ --bg: #fff; --bd: var(--fg-solid); --fg: var(--fg-solid); }

/* 인터랙션(선택형 칩 대비) */
.pill.clickable { cursor: pointer; user-select: none; }
.pill.clickable:focus-visible { outline: none; box-shadow: 0 0 0 2px rgba(59,130,246,.35); }
.pill.clickable:active { transform: translateY(.5px); }
.pill.active.t-outline { background: rgba(59,130,246,.08); }
</style>
