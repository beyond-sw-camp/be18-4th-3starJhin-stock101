<template>
    <section class="card">
        <div class="back" @click="goBack"> ← 돌아가기</div>

        <h1 class="main-title">Stock101에서</h1>
        <h1 class="main-title">투자 여정을 시작하세요</h1>
        <p class="sub">계정을 만들고 시장의 흐름을 함께 경험해보세요</p>

        <div>
            <form @submit.prevent="submitRegister" novalidate class="register-form">

                <div class="input-group">
                    <label for="register-name" class="input-label">이름</label>
                    <input type="text" id="register-name" v-model="name" required class="custom-input" />
                    <p v-if="nameError" class="error-message">{{ nameError }}</p>
                </div>

                <div class="input-group">
                    <label for="register-email" class="input-label">이메일</label>
                    <input type="email" id="register-email" v-model="email" required class="custom-input" />
                    <p v-if="emailError" class="error-message">{{ emailError }}</p>
                </div>

                <div class="input-group">
                    <label for="register-password" class="input-label">비밀번호</label>
                    <input type="password" id="register-password" v-model="password" required class="custom-input" />
                </div>

                <div class="input-group">
                    <label for="confirm-password" class="input-label">비밀번호 확인</label>
                    <input type="password" id="confirm-password" v-model="passwordConfirm" required
                        class="custom-input" />
                </div>
                <p v-if="passwordError" class="error-message">{{ passwordError }}</p>

                <div class="checkbox-group">
                    <input type="checkbox" id="terms-agree" v-model="termsAgreed" required class="custom-checkbox">
                    <label for="terms-agree" class="checkbox-label">약관 및 개인정보 처리 동의<span
                            class="required-text">(필수)</span></label>
                </div>

                <p v-if="serverErrorMessage" class="error-message">{{ serverErrorMessage }}</p>

                <button type="submit" class="register-button" :disabled="!isFormValid">회원 가입</button>

                <p class="switch">계정이 있다면 <router-link :to="{ name: 'userLogin' }" class="login-link">로그인</router-link>
                    해주세요</p>

            </form>
        </div>
    </section>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthValidation } from './AuthValidator';
import apiClient from '@/api';

// 데이터 정의
const name = ref('');
const email = ref('');
const password = ref('');
const passwordConfirm = ref('');
const termsAgreed = ref(false);
const serverErrorMessage = ref('');
// const newsletterSubscribed = ref(false);
const router = useRouter();
const goBack = () => { router.back(); }

const { nameError, emailError, passwordError } = useAuthValidation(email, password, passwordConfirm, name);

const isFormValid = computed(() => {
    return name.value && email.value && password.value &&
        (password.value === passwordConfirm.value) && termsAgreed.value &&
        !emailError.value && !passwordError.value;
})

// 회원가입 제출 함수
async function submitRegister() {
    if (!isFormValid.value) {
        console.error('폼 제출이 거부되었습니다. 유효성 검사를 확인하세요.');
        return;
    }

    serverErrorMessage.value = ''; // 이전 에러 메시지 초기화

    try {
        // API 호출
        await apiClient.post('/api/v1/users/register', {
            name: name.value,
            email: email.value,
            password: password.value,
        });
        // 회원가입 성공 시
        alert('회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.');
        router.push({ name: 'userLogin' });

    } catch (error) {
        // API 호출 실패 시
        console.error('회원가입 실패:', error);
        if (error.response && error.response.status === 409) {
            // 409 Conflict: 이미 사용 중인 이메일
            serverErrorMessage.value = '이미 사용 중인 이메일입니다.';
        } else {
            // 그 외 다른 에러
            serverErrorMessage.value = '회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
            alert('회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        }
    }
};
</script>

<style scoped>
.error-message {
    color: red;
    font-size: 12px;
    font-weight: bold;
    /* 추가: 위쪽 마진을 주어 입력 필드와의 간격을 조절합니다. */
    margin-top: -4px;
    /* 입력 필드와 에러 메시지 사이의 간격을 줄입니다. */
    padding-left: 2px;
    /* 라벨과 맞추어 들여쓰기 합니다. */
}

/* 카드 컨테이너: 로그인 페이지와 동일한 스타일 유지 */
.card {
    max-width: 560px;
    margin: 20px auto;
    padding: 28px 24px;
    border: 1px solid #e5e7eb;
    border-radius: 16px;
    background: #fff;
    /* 회원가입은 폼 콘텐츠가 중앙이 아니므로 text-align: left를 기본으로 사용하고, 
       제목 부분만 중앙 정렬처럼 보이도록 조정합니다. */
}

/* 돌아가기 링크: 왼쪽 정렬 */
.back {
    text-align: left;
    margin-bottom: 8px;
    color: #6b7280;
    font-size: 14px;
    cursor: pointer;
}

/* 제목 및 부제목: 중앙 정렬 */
.main-title {
    font-size: 34px;
    line-height: 1.25;
    margin: 0;
    text-align: center;
    /* 제목 중앙 정렬 */
}

.sub {
    margin: 6px 0 24px;
    color: #6b7280;
    text-align: center;
    /* 부제목 중앙 정렬 */
}

/* 폼 그룹 */
.register-form {
    display: grid;
    gap: 20px;
    /* 섹션 간 간격 (입력 필드, 체크박스 등) */
    text-align: left;
    /* 폼 내부 요소 왼쪽 정렬 */
}

/* 입력 필드 그룹 */
.input-group {
    display: grid;
    gap: 8px;
}

/* 라벨 스타일 (필드 위에 위치) */
.input-label {
    font-size: 14px;
    font-weight: 500;
    color: #333;
    padding-left: 2px;
    /* 살짝 들여쓰기 */
}

/* --- 입력 필드 스타일: 로그인 페이지와 일관성 유지 --- */
.custom-input {
    width: 100%;
    padding: 12px 16px;
    /* 내부 여백 */
    border: 1px solid #d1d5db;
    /* 연한 회색 테두리 */
    border-radius: 8px;
    /* 둥근 모서리 */
    font-size: 16px;
    box-sizing: border-box;
    outline: none;
    transition: border-color 0.2s;
}

.custom-input:focus {
    border-color: #5a2c51;
}


/* --- 체크박스 스타일 --- */
.checkbox-group {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: -5px;
    /* 입력 필드와의 간격 조정 */
}

.custom-checkbox {
    /* 기본 체크박스 스타일 재정의 (필요에 따라 더 커스텀 가능) */
    width: 16px;
    height: 16px;
    /* 여기에 커스텀 체크박스 스타일을 더 추가할 수 있습니다. */
}

.checkbox-label {
    font-size: 14px;
    color: #333;
    cursor: pointer;
}

.required-text {
    color: #9ca3af;
    /* 필수 텍스트 색상 */
    font-weight: 400;
}

.optional-text {
    color: #9ca3af;
    /* 선택 텍스트 색상 */
    font-weight: 400;
}


/* --- 회원가입 버튼 스타일: 로그인 버튼과 일관성 유지 --- */
.register-button {
    width: 100%;
    padding: 14px 20px;
    border: none;
    border-radius: 8px;
    background-color: #3f1e38;
    color: white;
    font-size: 18px;
    font-weight: bold;
    cursor: pointer;
    margin-top: 8px;
    transition: background-color 0.2s;
}

.register-button:hover:not(:disabled) {
    background-color: #5a2c51;
    /* 호버 시 색상 변화 */
}

.register-button:disabled {
    background-color: #a3a3a3;
    /* 비활성화 시 색상 */
    cursor: not-allowed;
}


/* --- 로그인 이동 링크 --- */
.switch {
    margin-top: 12px;
    text-align: center;
    /* 중앙 정렬 */
    color: #6b7280;
    font-size: 14px;
}

.login-link {
    color: #3f1e38;
    /* 링크 색상 */
    text-decoration: none;
    font-weight: 600;
}
</style>