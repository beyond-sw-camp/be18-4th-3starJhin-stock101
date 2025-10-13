<template>
  <BaseCard
    as="form"
    variant="outlined"
    pad="lg"
    radius="xl"
    @submit.prevent="submit"
  >
    <!-- 헤더 -->
    <template #header>
      <h3 class="title">{{ title }}</h3>
    </template>

    <!-- 본문 -->
    <div class="composer">
      <div class="chips">
        <button
          v-for="opt in OPTIONS"
          :key="opt.key"
          type="button"
          class="chip"
          :class="[{ active: modelSentiment === opt.key }, `tone-${opt.tone}`]"
          :disabled="disabled || loading"
          :aria-pressed="modelSentiment === opt.key"
          @click="toggle(opt.key)"
        >
          {{ opt.label }}
        </button>
      </div>

      <!-- 반드시 닫는 태그 사용 -->
      <textarea
        class="text"
        v-model="text"
        :placeholder="placeholder"
        :maxlength="maxlength || undefined"
        :disabled="disabled || loading"
        rows="5"
      ></textarea>
    </div>

    <!-- 푸터 -->
    <template #footer>
      <div class="actions">
        <BaseButton
          type="submit"
          variant="primary"
          :loading="loading"
          :disabled="disabled || loading || !canSubmit"
        >
          {{ submitText }}
        </BaseButton>
      </div>
    </template>
  </BaseCard>
</template>

<script setup>
import { computed } from 'vue'
import BaseButton from '@/components/button/BaseButton.vue'

/**
 * @component OpinionComposer
 * @description
 * 의견 작성(텍스트 + 투자 의견 선택) 폼 카드 컴포넌트  
 * - `v-model`로 본문 텍스트를, `v-model:sentiment`로 투자 의견을 제어  
 * - 감정 선택 칩(Strong Sell ~ Strong Buy) 제공  
 * - 최소 글자수/감정 선택 강제 등 제출 가능 여부 검증  
 * - 제출 시 `{ text, sentiment }` 페이로드로 `submit` 이벤트 방출
 *
 * @slot header  카드 상단 영역(제목 등) – 기본으로 `title`을 헤더에 렌더링
 * @slot default 본문 커스텀 슬롯(기본은 textarea)
 * @slot footer  하단 액션 영역(기본은 등록 버튼)
 *
 * @example
 * <!-- 기본 사용 -->
 * <OpinionComposer v-model="text" v-model:sentiment="sent" @submit="onSubmit" />
 *
 * <!-- 최소 글자수/감정 선택 강제 -->
 * <OpinionComposer
 *   v-model="text"
 *   v-model:sentiment="sent"
 *   :min-length="10"
 *   :require-sentiment="true"
 *   @submit="({ text, sentiment }) => save(text, sentiment)"
 * />
 *
 * <!-- 플레이스홀더/버튼 텍스트 변경 -->
 * <OpinionComposer
 *   v-model="text"
 *   submit-text="게시"
 *   placeholder="의견을 남겨주세요…"
 * />
 */

/**
 * @typedef {Object} OpinionComposerProps
 * @property {string}  [modelValue='']               - 본문 텍스트 (v-model)
 * @property {string}  [sentiment='']                - 투자 의견 (v-model:sentiment). '', 'strong-sell'|'sell'|'hold'|'buy'|'strong-buy'
 * @property {string}  [title='지금 당신의 생각을 남겨보세요'] - 헤더 제목
 * @property {string}  [placeholder='']              - textarea placeholder
 * @property {string}  [submitText='등록하기']        - 제출 버튼 라벨
 * @property {number}  [maxlength=0]                 - 본문 최대 글자수(0이면 무제한)
 * @property {boolean} [loading=false]               - 로딩 상태(버튼 스피너/비활성화)
 * @property {boolean} [disabled=false]              - 전체 비활성화
 * @property {boolean} [requireSentiment=false]      - 제출 시 감정 선택 필수 여부
 * @property {number}  [minLength=0]                 - 제출 최소 글자수
 */
const props = defineProps({
  /** v-model */
  modelValue: { type: String, default: '' },
  /** v-model:sentiment */
  sentiment: { type: String, default: '' }, // '', 'strong-sell' | 'sell' | 'hold' | 'buy' | 'strong-buy'

  title: { type: String, default: '지금 당신의 생각을 남겨보세요' },
  placeholder: { type: String, default: '' },
  submitText: { type: String, default: '등록하기' },
  maxlength: { type: Number, default: 0 },

  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  requireSentiment: { type: Boolean, default: false }, // true면 선택 강제
  minLength: { type: Number, default: 0 },             // 본문 최소 글자수
})

/**
 * @event update:modelValue
 * @description 본문 텍스트 v-model 업데이트
 * @property {string} value - 변경된 텍스트
 *
 * @event update:sentiment
 * @description 투자 의견 v-model 업데이트
 * @property {string} value - 변경된 의견
 *
 * @event submit
 * @description 제출 시 발생. 유효성 통과 시 `{ text, sentiment }` 페이로드 전달
 * @property {{ text: string, sentiment: string }} payload
 */
const emit = defineEmits(['update:modelValue', 'update:sentiment', 'submit'])

/** 감정 선택 옵션 정의 */
const OPTIONS = [
  { key: 'strong-sell', label: 'Strong Sell', tone: 'danger'  },
  { key: 'sell',        label: 'Sell',        tone: 'warning' },
  { key: 'hold',        label: 'Hold',        tone: 'neutral' },
  { key: 'buy',         label: 'Buy',         tone: 'success' },
  { key: 'strong-buy',  label: 'Strong Buy',  tone: 'success' },
]

/**
 * 양방향 감정 상태(v-model:sentiment)
 * @type {import('vue').ComputedRef<string>}
 */
const modelSentiment = computed({
  get: () => props.sentiment,
  set: (v) => emit('update:sentiment', v)
})

/**
 * 칩 클릭 시 감정 토글
 * - 같은 키 다시 클릭하면 해제('')
 * @param {string} key - 감정 키
 */
function toggle(key) {
  modelSentiment.value = modelSentiment.value === key ? '' : key
}

/**
 * 제출 가능 여부
 * - 최소 글자수 충족
 * - requireSentiment=true면 감정 선택 필수
 * @type {import('vue').ComputedRef<boolean>}
 */
const canSubmit = computed(() => {
  const okText = (props.modelValue?.trim().length || 0) >= props.minLength
  const okSent = props.requireSentiment ? !!modelSentiment.value : true
  return okText && okSent
})

/**
 * 제출 핸들러
 * - canSubmit이 true일 때만 submit 이벤트 방출
 * - payload: { text, sentiment }
 */
function submit() {
  if (!canSubmit.value) return
  emit('submit', { text: props.modelValue.trim(), sentiment: modelSentiment.value })
}

/* -------------------------------------------------------
 * 참고: 템플릿에서 v-model="text" 사용 중이므로
 * 실제 상위에서 defineModel 또는 v-model 바인딩으로
 * `modelValue`/`update:modelValue`를 연결해야 합니다.
 * (이 파일에서는 양방향 바인딩용 보조 상태를 만들지 않고
 *  상위가 넘겨준 modelValue를 그대로 사용합니다)
 * ----------------------------------------------------- */
</script>

<style scoped>
.composer{
  border:1px solid #cfd4dc; border-radius:18px; background:#fff;
  padding:16px; display:grid; gap:12px;
}
.title{ margin:0; font-size:18px; font-weight:700; color:#111827; }

.chips{ display:flex; flex-wrap:wrap; gap:10px; }
.chip{
  appearance:none; border:1px solid #cfd4dc; background:#fff; color:#111827;
  border-radius:999px; padding:8px 14px; cursor:pointer; font-weight:600;
}
.chip.active{ background:#111827; color:#fff; border-color:#111827; }

/* 색감(비활성 텍스트에만 살짝) */
.tone-success{ color:#16a34a; border-color:#bfe7cb; }
.tone-warning{ color:#b45309; border-color:#f3c99a; }
.tone-danger{  color:#991B1B; border-color:#f2b4b4; }
.tone-neutral{ color:#6b7280; border-color:#d8dce2; }

.text{
  width:100%; min-height:140px; resize:vertical;
  border:0; border-radius:12px; background:#eceff3; padding:14px;
  font: inherit; color:#111827;
}
.text:disabled{ opacity:.6; }

.actions{ display:flex; justify-content:flex-end; }
</style>
