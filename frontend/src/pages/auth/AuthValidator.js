import { ref, watch } from 'vue';

// 로그인, 회원가입 폼의 유효성 검증 로직을 담은 컴포저블 함수
export function useAuthValidation(email, password, passwordConfirm = null, name = null) {

    // 각 필드에 대한 에러 메시지를 저장할 변수
    const nameError = ref('');
    const emailError = ref('');
    const passwordError = ref('');

    if (name) {
        watch(name, (newName) => {
            if (!newName || !newName.trim()) {
                nameError.value = '이름을 입력해주세요.';
            } else {
                nameError.value = '';
            }
        },{immediate: true});
    }

    // 이메일 형식 실시간 검증
    watch(email, (newEmail) => {
        if (!newEmail) { // 비어있을 땐 에러 없음
            emailError.value = '';
            return;
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(newEmail)) {
            emailError.value = '올바른 이메일 형식이 아닙니다.';
        } else {
            emailError.value = '';
        }
    });



    const validatePassword = () => {
            const hasLetter = /[a-zA-Z]/.test(password.value);
            const hasNumber = /[0-9]/.test(password.value);

        // 1. 비밀번호 강도 검증 (8자 이상) - 회원가입 시에만 필요할 수 있으나, 일단 공통 로직으로 포함
        if ((password.value && password.value.length < 8 ) || (hasLetter ==false) || (hasNumber==false)) {
            passwordError.value = '비밀번호는 8자 이상 (숫자 + 영문) 이어야 합니다.';
            return;
        }
        

        // 2. 비밀번호 일치 여부 검증 (passwordConfirm 인자가 있을 경우에만 실행)
        if (passwordConfirm && passwordConfirm.value && password.value !== passwordConfirm.value) {
            passwordError.value = '비밀번호가 서로 다릅니다.';
            return;
        }

        // 모든 검증 통과 시 에러 메시지 초기화
        passwordError.value = '';
    };

    // password와 passwordConfirm 값이 바뀔 때마다 검증 함수 실행
    watch(password, validatePassword);
    if (passwordConfirm) {
        watch(passwordConfirm, validatePassword);
    }

    // 검증 결과(에러 메시지)를 반환
    return {
        nameError,
        emailError,
        passwordError,
    };
}