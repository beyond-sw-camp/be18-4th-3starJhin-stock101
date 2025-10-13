<template>
  <BaseCard
    variant="outlined"
    pad="md"
    radius="xl"
    :to="to"
    :href="href"
    :target="target"
    :clickable="true"
  >
    <template #header>
      <div class="row">
        <div class="avatar" :style="avatarStyle" />
        <Pill class="pill" :text="sentimentLabel" :variant="sentimentVariant" tone="soft" size="sm" />
        <span class="name">{{ username }}</span>
        <span v-if="verified" class="verified">🏅</span>
        <span class="time" v-if="timeText">{{ timeText }}</span>
      </div>
    </template>

    <slot>{{ content }}</slot>

    <template #footer>
      <slot name="footer" />
    </template>
  </BaseCard>
</template>

<script setup>
import { computed } from 'vue'
import Pill from '@/components/ui/Pill.vue'
import BaseCard from '@/components/card/BaseCard.vue'

/**
 * @component OpinionCard
 * @description
 * 사용자 의견(Opinion) 카드 컴포넌트  
 * - 아바타, 닉네임, 인증 배지, 시간 정보 표시  
 * - 투자 의견(sentiment)을 Pill로 표시 (Strong Sell / Sell / Hold / Buy / Strong Buy)  
 * - 카드 본문에는 슬롯(default) 또는 `content` 문자열 표시  
 * - footer 슬롯 제공  
 * - RouterLink / a 태그 링크로도 활용 가능
 *
 * @example
 * <!-- 기본 사용 -->
 * <OpinionCard username="Alice" content="이 종목은 좋아 보입니다" />
 *
 * <!-- 라우터 링크 카드 -->
 * <OpinionCard :to="{ name: 'user', params: { id: 1 } }"
 *              username="Bob" verified :sentiment="'buy'" />
 *
 * <!-- 커스텀 콘텐츠 -->
 * <OpinionCard username="Charlie" :sentiment="'strong-sell'">
 *   <template #default>
 *     <p>이 회사는 위험해 보여요 🚨</p>
 *   </template>
 *   <template #footer>
 *     <LikeButton v-model="liked" :count="12" />
 *   </template>
 * </OpinionCard>
 */

/**
 * @typedef {Object} OpinionCardProps
 * @property {string} avatar - 아바타 이미지 URL
 * @property {string} username - 사용자 이름
 * @property {boolean} [verified=false] - 인증 여부 (뱃지 표시)
 * @property {string} [timeText=''] - 시간 텍스트
 * @property {string} [content=''] - 본문 기본 문자열 (슬롯 없을 때 표시)
 * @property {'strong-sell'|'sell'|'hold'|'buy'|'strong-buy'} [sentiment='hold'] - 투자 의견
 * @property {string|object|null} [to=null] - router-link 목적지
 * @property {string} [href=''] - a 태그 링크
 * @property {string} [target='_self'] - a 태그 target
 * @property {string} [rel=''] - a 태그 rel
 */
const props = defineProps({
  avatar: { type: String, default: '' },
  username: { type: String, required: true },
  verified: { type: Boolean, default: false },
  timeText: { type: String, default: '' },
  content: { type: String, default: '' },

  /** 'strong-sell' | 'sell' | 'hold' | 'buy' | 'strong-buy' */
  sentiment: { type: String, default: 'hold' },

  // 링크 옵션
  to: { type: [String, Object], default: null },
  href: { type: String, default: '' },
  target: { type: String, default: '_self' },
  rel: { type: String, default: '' },
})

/**
 * @slot default - 카드 본문 (본문 내용)
 * @slot footer - 카드 하단 푸터 (액션 버튼 등)
 * @slot header - 카드 헤더 (기본 제공 구조 사용, 오버라이드 가능)
 */

const isRouter = computed(() => !!props.to && !props.href)
const relComputed = computed(() => {
  if (props.rel) return props.rel
  if (props.href && props.target === '_blank') return 'noopener noreferrer'
  return undefined
})

/** 아바타 원형 스타일 (이미지 없으면 회색 배경) */
const avatarStyle = computed(() => ({
  backgroundImage: props.avatar ? `url(${props.avatar})` : undefined
}))

/** 감정 라벨/색상 매핑 */
const MAP = {
  'strong-sell': { label: 'Strong Sell', variant: 'danger'  },
  'sell':        { label: 'Sell',        variant: 'warning' },
  'hold':        { label: 'Hold',        variant: 'neutral' },
  'buy':         { label: 'Buy',         variant: 'success' },
  'strong-buy':  { label: 'Strong Buy',  variant: 'success' },
}
const sentimentLabel = computed(() => (MAP[props.sentiment] || MAP.hold).label)
const sentimentVariant = computed(() => (MAP[props.sentiment] || MAP.hold).variant)
</script>

<style scoped>
.op-card{
  display:grid; gap:10px;
  border:1px solid #cfd4dc; border-radius:18px; background:#fff;
  padding:12px 16px; color:inherit; text-decoration:none;
}
.row.head{ display:flex; justify-content:space-between; align-items:center; gap:8px; }
.left{ display:flex; align-items:center; gap:10px; min-width:0; }
.avatar{
  width:36px; height:36px; border-radius:999px; flex:0 0 auto;
  background:#e5e7eb center/cover no-repeat;
  border:1px solid #e5e7eb;
}
.pill{ margin-left:2px; }
.name{ font-weight:700; color:#111827; }
.verified{ font-size:18px; line-height:1; }
.time{ color:#9ca3af; white-space:nowrap; }
.body{
  color:#111827; line-height:1.6;
  word-break: break-word;
}
</style>
