<template>
  <div class="stock-community">
    <div class="stock-community__inner">
      <BaseBackButton class="stock-community__back" @click="handleBack">돌아가기</BaseBackButton>
      <section class="stock-community__conversation">
        <CommunityFeedView :show-back-button="false" :stock-id="stockId" />
      </section>
    </div>
  </div>
</template>

<script setup>
import BaseBackButton from '@/components/shared/BaseBackButton.vue'
import CommunityFeedView from '@/views/CommunityFeedView.vue'
import axios from 'axios'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

defineProps({
  symbol: {
    type: String,
    default: '',
  },
})

const router = useRouter()
const stockId = ref(null)

onMounted(async () => {
  if (!__props.symbol) return
  try {
    const { data } = await axios.get('/api/v1/stock', { params: { symbol: __props.symbol } })
    // backend shape may return items or data; try a few options
    const items = Array.isArray(data?.items) ? data.items : Array.isArray(data?.data) ? data.data : null
    const found = items?.[0] ?? data?.item ?? data?.stock ?? null
    if (found && typeof found.stockId === 'number') stockId.value = found.stockId
  } catch (e) {
    // ignore - view will fall back to default behavior in CommunityFeedView
    console.error('Failed to resolve stockId for symbol', __props.symbol, e)
  }
})

function handleBack() {
  router.back()
}
</script>

<style scoped>
.stock-community {
  padding-bottom: 80px;
}

.stock-community__inner {
  display: flex;
  flex-direction: column;
  gap: 32px;
  max-width: 960px;
  margin: 40px auto 0;
  padding: 0 40px;
}

.stock-community__conversation :deep(.community-feed) {
  padding: 0;
}

@media (max-width: 768px) {
  .stock-community__inner {
    margin: 24px auto 0;
    padding: 0 24px;
  }
}
</style>
