<template>
  <BaseCard
    class="user-card"
    :variant="variant"
    :pad="pad"
    :radius="radius"
    :clickable="clickable || !!to || !!href"
    :to="to"
    :href="href"
    :target="target"
    :rel="rel"
  >
    <div class="row" :class="`s-${size}`">
      <div class="avatar-wrap">
        <img
          v-if="avatar"
          :src="avatar"
          :alt="altText"
          class="avatar"
          loading="lazy"
          decoding="async"
        />
        <div v-else class="avatar placeholder" aria-hidden="true" />
      </div>

      <div class="body">
        <div class="name-line">
          <span class="name">{{ name }}</span>
          <slot name="badge">
            <img
              v-if="tierBadgeSrc"
              :src="tierBadgeSrc"
              :alt="tierBadgeAlt"
              class="badge-image"
              loading="lazy"
              decoding="async"
            />
            <span v-if="verified" class="badge" aria-label="verified">🏅</span>
          </slot>
        </div>
        <div v-if="status" class="status">{{ status }}</div>
        <slot />
      </div>
    </div>
  </BaseCard>
</template>

<script setup>
import { computed } from 'vue'
import BaseCard from '../BaseCard.vue'
import { getTierBadgeSrc } from '@/utils/tierBadge'
/**
 * @component UserCard
 * @description
 * 사용자 정보를 표시하는 카드 컴포넌트
 * - 아바타, 이름, 상태 메시지, 인증 배지를 지원
 * - 링크/라우팅 기능(BaseCard 기반) 제공
 * - `size` 프리셋(sm, md, lg)으로 아바타 크기 조절
 * - badge, body를 슬롯으로 확장 가능
 *
 * @slot badge   이름 옆 배지 영역 (기본: 인증 뱃지 🏅)
 * @slot default 상태(status) 아래 추가 콘텐츠 삽입 영역
 *
 * @example
 * <!-- 기본 사용 -->
 * <UserCard name="홍길동" status="온라인" avatar="/me.png" verified />
 *
 * <!-- 작은 크기 -->
 * <UserCard name="Jane Doe" size="sm" />
 *
 * <!-- 배지 슬롯 커스터마이즈 -->
 * <UserCard name="Admin">
 *   <template #badge><Tag variant="info">관리자</Tag></template>
 * </UserCard>
 *
 * <!-- 링크 카드 -->
 * <UserCard :to="{ name: 'profile', params: { id: 1 } }" name="사용자" />
 */

/**
 * @typedef {Object} UserCardProps
 * @property {string} avatar - 아바타 이미지 URL
 * @property {string} name - 사용자 이름
 * @property {string} [status=''] - 상태 메시지
 * @property {boolean} [verified=false] - 인증 여부 (뱃지 표시)
 * @property {string} [alt=''] - 아바타 대체 텍스트 (기본: "{name}의 아바타")
 * @property {string|object|null} [to=null] - router-link 목적지
 * @property {string} [href=''] - a 태그 링크
 * @property {string} [target='_self'] - 링크 target
 * @property {string} [rel=''] - 링크 rel
 * @property {'outlined'|'elevated'|'soft'|'ghost'} [variant='outlined'] - 카드 스타일
 * @property {'none'|'sm'|'md'|'lg'} [pad='md'] - 카드 padding
 * @property {'md'|'lg'|'xl'|'full'} [radius='xl'] - 카드 모서리 둥글기
 * @property {boolean} [clickable=false] - 클릭 가능 여부
 * @property {'sm'|'md'|'lg'} [size='md'] - 아바타/레이아웃 크기 프리셋
 */
const props = defineProps({
  avatar:   { type: String, default: '' },
  name:     { type: String, required: true },
  tierCode:   { type: String, default: '' },
  status:   { type: String, default: '' },
  verified: { type: Boolean, default: false },
  alt:      { type: String, default: '' },
  userId:   { type: [String, Number], default: null },

  // 링크/라우터(BaseCard로 전달)
  to:     { type: [String, Object], default: null },
  href:   { type: String, default: '' },
  target: { type: String, default: '_self' },
  rel:    { type: String, default: '' },

  // BaseCard 스타일 제어
  variant:   { type: String, default: 'outlined' },
  pad:       { type: String, default: 'md' },
  radius:    { type: String, default: 'xl' },
  clickable: { type: Boolean, default: false },

  // 크기 프리셋
  size: { type: String, default: 'md' } // sm | md | lg
})

/**
 * 아바타 이미지의 대체 텍스트
 * - alt prop이 있으면 우선 사용
 * - 없으면 "{name}의 아바타" 자동 생성
 */
const altText = computed(() => props.alt || `${props.name}의 아바타`)
const tierBadgeSrc = computed(() => {
  if (props.tierCode === undefined || props.tierCode === null || props.tierCode === '') {
    return ''
  }
  return getTierBadgeSrc(props.tierCode)
})
const tierBadgeAlt = computed(() => `${props.name}의 등급 배지`)
</script>

<style scoped>
/* BaseCard가 테두리/패딩/호버 담당 → 내부 레이아웃만 정의 */
.row{
  display:grid; grid-template-columns: 64px 1fr; gap:12px; align-items:center;
}
.s-sm{ grid-template-columns: 48px 1fr; }
.s-lg{ grid-template-columns: 72px 1fr; }

.avatar-wrap{
  width:64px; height:64px; border-radius:12px; overflow:hidden;
}
.s-sm .avatar-wrap{ width:48px; height:48px; border-radius:10px; }
.s-lg .avatar-wrap{ width:72px; height:72px; border-radius:14px; }

.avatar{ width:100%; height:100%; object-fit:cover; display:block; }
.avatar.placeholder{ background:#e5e7eb; }
.badge-image{
  width:24px;
  height:24px;
  object-fit:contain;
}
.body{ min-width:0; padding-right:8px; }
.name-line{ display:flex; align-items:center; gap:6px; }
.name{ font-weight:700; color:#111827; }
.badge{ font-size:18px; line-height:1; }
.status{ margin-top:6px; color:#6b7280; }
</style>
