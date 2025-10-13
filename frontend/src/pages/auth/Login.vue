<template>
    <section class="card">
        <div class="back" @click="goBack"> ← 돌아가기</div>

        <h1>환영해요</h1>
        <p class="sub">오늘도 **stock101**과 함께 똑똑한 투자 여정을 이어가세요. </p>

        <div>
            <form @submit.prevent="submitLogin" novalidate class="login-form">

                <div class="input-group flex-group">
                    <span class="input-label">아이디 :</span>
                    <input v-model="email" type="email" id="login-email" required class="custom-input flex-item"
                        placeholder="이메일을 입력하세요" />
                </div>

                <div class="input-group flex-group">
                    <span class="input-label">비밀번호 :</span>
                    <input v-model="password" type="password" id="login-password" required
                        class="custom-input flex-item" placeholder="비밀번호를 입력하세요" />
                </div>
                <div class="checkbox-group">
                    <input type="checkbox" id="terms-agree" v-model="stayLoggedIn" required class="custom-checkbox">
                    <label for="terms-agree" class="checkbox-label">로그인 상태 유지</label>
                </div>

                <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
                <button type="submit" class="login-button">로그인</button>


                <p class="switch">계정이 없나요?
                    <router-link :to="{ name: 'userRegister' }" class="register-link">회원 가입하기</router-link>
                </p>
            </form>
        </div>
    </section>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import router from '@/router';


// 예시 데이터 및 함수 정의
const email = ref('');
const password = ref('');
const submitted = ref(false);
const authStore = useAuthStore()
const errorMessage = ref('');
const goBack = () => { router.back(); }
const stayLoggedIn =ref('');

async function submitLogin() {
    submitted.value = true;
    errorMessage.value = '';

    //console.log('로그인 시도:', email.value, password.value);
    const result = await authStore.login(email.value, password.value, stayLoggedIn.value);

    if (result.success) {
        router.push('/');
    } else {
        errorMessage.value = result.message;
    }
}
</script>

<style scoped>
/* ==================================== */
/* 1. 레이아웃 및 제목 스타일 */
/* ==================================== */

.card {
    max-width: 560px;
    margin: 20px auto;
    padding: 28px 24px;
    border: 1px solid #e5e7eb;
    border-radius: 16px;
    background: #fff;
    text-align: center;
    /* 전체 텍스트 중앙 정렬 */
}

.back {
    text-align: left;
    /* '돌아가기'만 왼쪽 정렬 */
    margin-bottom: 8px;
    color: #6b7280;
    font-size: 14px;
    cursor: pointer;
}

h1 {
    font-size: 34px;
    line-height: 1.25;
    margin: 0 0 6px;
}

.sub {
    margin: 0 0 24px;
    color: #6b7280;
}

.login-form {
    display: grid;
    gap: 16px;
    /* 입력 필드 및 버튼 간 간격 */
}

/* ==================================== */
/* 2. 입력 필드 (아이디/비밀번호) 스타일 */
/* ==================================== */

/* Flexbox로 라벨과 인풋을 한 줄에 정렬 */
.flex-group {
    display: flex;
    align-items: center;
    gap: 10px;
}

/* 라벨 텍스트 ('아이디 :'/'비밀번호 :') 스타일 */
.input-label {
    width: 80px;
    /* 라벨 너비 고정 */
    text-align: left;
    font-size: 16px;
    font-weight: 500;
    color: #333;
    flex-shrink: 0;
}

/* 입력 필드 (input) 스타일 */
.custom-input.flex-item {
    flex-grow: 1;
    /* 남은 공간 모두 차지 */
}

.custom-input {
    padding: 12px 16px;
    border: 1px solid #d1d5db;
    border-radius: 8px;
    font-size: 16px;
    box-sizing: border-box;
    outline: none;
    transition: border-color 0.2s;
}

.custom-input:focus {
    border-color: #5a2c51;
}

/* ==================================== */
/* 3. 버튼 및 링크 스타일 */
/* ==================================== */
.error-message {
    color: red;
    font-weight: bold;
    font-size: 13px;
}

.login-button {
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

.login-button:hover {
    background-color: #5a2c51;
}

.switch {
    margin-top: 12px;
    text-align: center;
    color: #6b7280;
    font-size: 14px;
}

.register-link {
    color: #3f1e38;
    text-decoration: none;
    font-weight: 600;
}
</style>