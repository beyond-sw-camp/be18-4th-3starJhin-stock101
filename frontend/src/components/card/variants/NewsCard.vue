<template>
  <BaseCard
    class="news-item"
    :variant="variant"
    :pad="pad"
    :radius="radius"
    :clickable="clickable || !!to || !!href"
    :to="to"
    :href="href"
    :target="target"
    :rel="rel"
  >
    <div class="row">
      <img
        v-if="image"
        class="thumb"
        :src="image"
        :alt="alt || title"
        loading="lazy"
        decoding="async"
      />
      <div v-else class="thumb placeholder" aria-hidden="true" />

      <div class="meta">
        <div class="title" :title="title">{{ title }}</div>
        <div class="sub">
          <time v-if="dateText">{{ dateText }}</time>
          <span v-if="source"> {{ source }}</span>
        </div>
      </div>
    </div>
  </BaseCard>
</template>

<script setup>
import { computed } from 'vue'
import BaseCard from '@/components/card/BaseCard.vue'

/**
 * @component NewsItem
 * @description
 * 뉴스/기사 아이템 카드 컴포넌트  
 * - 썸네일 이미지 + 제목 + 날짜 + 출처를 표시  
 * - BaseCard 기반으로 클릭 가능/링크 가능  
 * - 이미지 없을 경우 placeholder 표시  
 * - 날짜는 기본 `ko-KR` 포맷 또는 `dateFormat` prop으로 커스터마이즈 가능
 *
 * @example
 * <!-- 기본 사용 -->
 * <NewsItem
 *   image="/thumb.png"
 *   title="최신 뉴스 제목"
 *   date="2025-09-15"
 *   source="뉴스1"
 * />
 *
 * <!-- 라우터 링크 -->
 * <NewsItem
 *   :to="{ name: 'news-detail', params: { id: 1 } }"
 *   title="기사 제목"
 *   date="2025-09-15"
 *   source="YTN"
 * />
 *
 * <!-- 커스텀 포맷 -->
 * <NewsItem
 *   title="커스텀 날짜 포맷"
 *   :date="new Date()"
 *   :date-format="new Intl.DateTimeFormat('en-US', { dateStyle: 'long' })"
 * />
 */

/**
 * @typedef {Object} NewsItemProps
 * @property {string} [image=''] - 썸네일 이미지 URL
 * @property {string} [alt=''] - 이미지 대체 텍스트
 * @property {string} title - 기사 제목 (필수)
 * @property {string|Date} [date=''] - 날짜 (문자열 또는 Date 객체)
 * @property {Intl.DateTimeFormat|null} [dateFormat=null] - 커스텀 날짜 포맷터
 * @property {string} [source=''] - 기사 출처
 * @property {string|object|null} [to=null] - router-link 목적지
 * @property {string} [href=''] - a 태그 링크
 * @property {string} [target='_self'] - 링크 target
 * @property {string} [rel=''] - 링크 rel
 * @property {'outlined'|'elevated'|'soft'|'ghost'} [variant='outlined'] - 카드 스타일
 * @property {'none'|'sm'|'md'|'lg'} [pad='sm'] - 카드 padding
 * @property {'md'|'lg'|'xl'|'full'} [radius='lg'] - 카드 radius
 * @property {boolean} [clickable=false] - 클릭 가능 여부
 */
const props = defineProps({
  image: { type: String, default: '' },
  alt: { type: String, default: '' },
  title: { type: String, required: true },
  date: { type: [String, Date], default: '' },
  dateFormat: { type: Intl.DateTimeFormat, default: null },
  source: { type: String, default: '' },

  // 링크/라우팅 (BaseCard로 전달)
  to: { type: [String, Object], default: null },
  href: { type: String, default: '' },
  target: { type: String, default: '_self' },
  rel: { type: String, default: '' },

  // BaseCard 스타일 제어 (옵션)
  variant: { type: String, default: 'outlined' }, // outlined | elevated | soft | ghost
  pad: { type: String, default: 'sm' },          // none | sm | md | lg
  radius: { type: String, default: 'lg' },       // md | lg | xl | full
  clickable: { type: Boolean, default: false },
})

/**
 * 표시할 날짜 텍스트
 * - dateFormat prop이 있으면 그대로 사용
 * - 기본은 ko-KR yyyy.MM.dd 형식
 * - 변환 실패 시 원본 문자열 출력
 */
const dateText = computed(() => {
  if (!props.date) return ''
  try {
    const d = typeof props.date === 'string' ? new Date(props.date) : props.date
    if (props.dateFormat) return props.dateFormat.format(d)
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric', month: '2-digit', day: '2-digit'
    }).format(d).replace(/\./g, '.').replace(/\s/g, '')
  } catch { return String(props.date) }
})
</script>

<style scoped>
/* BaseCard가 테두리/패딩/호버 담당 → 내부 레이아웃만 정의 */
.row{
  display:grid; grid-template-columns: 112px 1fr; gap:12px; align-items:center;
}
.thumb{
  width:112px; height:72px; object-fit:cover; border-radius:12px; display:block;
  box-shadow: 0 1px 0 rgba(0,0,0,.04) inset;
}
.thumb.placeholder{ background:#e5e7eb; }
.meta{ min-width:0; }
.title{
  font-weight:600; color:#111827; line-height:1.3;
  display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden;
}
.sub{ margin-top:6px; color:#6b7280; font-size:.9rem; }
.sub time::after{ content:' '; }
</style>
