<template>
  <section class="profile-header">
    <div class="profile-header__avatar" aria-hidden="true">
      <img v-if="user.imageUrl || user.avatarUrl" :src="user.imageUrl || user.avatarUrl" :alt="`${user.name} 아바타`" />
      <div v-else class="profile-header__avatar--placeholder"></div>
    </div>
    <div class="profile-header__details">
      <div class="profile-header__name-row">
        <h2 class="profile-header__name">{{ user.name }}</h2>
        <img
          v-if="tierBadgeSrc"
          :src="tierBadgeSrc"
          :alt="`${user.tierCode || 'BRONZE'} 등급 배지`"
          class="profile-header__tier-badge"
          loading="lazy"
          decoding="async"
        />
        <span v-if="user.badge" class="profile-header__badge">{{ user.badge }}</span>
      </div>
      <p class="profile-header__status">{{ user.statusMessage }}</p>
    </div>
    <button v-if="showEditButton" type="button" class="profile-header__action" @click="openEditModal">수정하기</button>
  </section>

  <!-- Edit Profile Modal -->
  <div v-if="showModal" class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>프로필 수정</h3>
        <button type="button" class="modal-close" @click="closeModal">&times;</button>
      </div>

      <form @submit.prevent="saveProfile" class="modal-body">
        <div class="form-group">
          <label for="edit-name">이름</label>
          <input
            id="edit-name"
            v-model="editForm.name"
            type="text"
            class="form-input"
            placeholder="이름을 입력하세요"
            required
          />
        </div>

        <div class="form-group">
          <label for="edit-status">상태 메시지</label>
          <textarea
            id="edit-status"
            v-model="editForm.statusMessage"
            class="form-textarea"
            placeholder="상태 메시지를 입력하세요"
            rows="3"
          ></textarea>
        </div>

        <div class="modal-actions">
          <button type="button" class="btn btn-cancel" @click="closeModal">취소</button>
          <button type="submit" class="btn btn-save" :disabled="loading">
            {{ loading ? '저장 중...' : '저장' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, reactive } from 'vue'
import { getTierBadgeSrc } from '@/utils/tierBadge'
import axios from 'axios'
import { useAuthStore } from '@/stores/authStore'

const props = defineProps({
  user: {
    type: Object,
    required: true,
    default: () => ({ name: '', statusMessage: '' }),
  },
  isOwner: {
    type: Boolean,
    default: false,
  },
  currentUser: {
    type: Object,
    default: null,
  },
})

const emits = defineEmits(['profile-updated'])

const authStore = useAuthStore()
const showModal = ref(false)
const loading = ref(false)

const editForm = reactive({
  name: '',
  statusMessage: ''
})

const tierBadgeSrc = computed(() => {
  if (!props.user?.tierCode) {
    return getTierBadgeSrc('BRONZE')
  }
  return getTierBadgeSrc(props.user.tierCode)
})

const showEditButton = computed(() => {
  // Show edit button only if current user exists and is viewing their own profile
  if (!props.currentUser) {
    return false
  }
  return props.currentUser.id === props.user.id || props.isOwner
})

const openEditModal = () => {
  // Pre-fill form with current user data
  editForm.name = props.user.name || ''
  editForm.statusMessage = props.user.statusMessage || ''
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  // Reset form
  editForm.name = ''
  editForm.statusMessage = ''
}

const saveProfile = async () => {
  loading.value = true
  try {
    const response = await axios.patch('http://localhost:8080/api/v1/users/me', {
      name: editForm.name,
      statusMessage: editForm.statusMessage
    }, {
      headers: {
        'Authorization': `Bearer ${authStore.userInfo.accessToken}`
      }
    })

    console.log('Profile updated successfully:', response.data)

    // Emit event to parent component to refresh data
    emits('profile-updated', {
      name: editForm.name,
      statusMessage: editForm.statusMessage
    })

    closeModal()
  } catch (error) {
    console.error('Failed to update profile:', error)
    alert('프로필 업데이트에 실패했습니다. 다시 시도해주세요.')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 24px;
  border: 1px solid #e6e8ec;
  border-radius: 24px;
  background-color: #fff;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.05);
}

.profile-header__avatar {
  width: 96px;
  height: 96px;
  flex-shrink: 0;
}

.profile-header__avatar img,
.profile-header__avatar--placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  background: linear-gradient(135deg, #e5e7eb, #f3f4f6);
}

.profile-header__details {
  flex: 1;
  min-width: 0;
}

.profile-header__name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.profile-header__name {
  font-size: 24px;
  font-weight: 600;
  color: #111827;
}

.profile-header__tier-badge {
  width: 28px;
  height: 28px;
  object-fit: contain;
  margin-left: 4px;
}

.profile-header__badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
  color: #2563eb;
  background-color: rgba(37, 99, 235, 0.12);
}

.profile-header__status {
  color: #4b5563;
  font-size: 15px;
  line-height: 1.5;
}

.profile-header__action {
  margin-left: auto;
  padding: 10px 18px;
  border-radius: 12px;
  border: 1px solid #d1d5db;
  background-color: #f9fafb;
  font-weight: 500;
  font-size: 14px;
  color: #1f2937;
  transition: background-color 0.15s ease, border-color 0.15s ease;
}

.profile-header__action:hover {
  background-color: #2563eb;
  border-color: #2563eb;
  color: #fff;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 16px;
  width: 90%;
  max-width: 480px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 24px 0 24px;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 24px;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #6b7280;
  cursor: pointer;
  padding: 4px;
  line-height: 1;
}

.modal-close:hover {
  color: #374151;
}

.modal-body {
  padding: 0 24px 24px 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
  box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
  border: 1px solid transparent;
}

.btn-cancel {
  background-color: #f9fafb;
  border-color: #d1d5db;
  color: #374151;
}

.btn-cancel:hover {
  background-color: #f3f4f6;
  border-color: #9ca3af;
}

.btn-save {
  background-color: #2563eb;
  color: #fff;
}

.btn-save:hover:not(:disabled) {
  background-color: #1d4ed8;
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
