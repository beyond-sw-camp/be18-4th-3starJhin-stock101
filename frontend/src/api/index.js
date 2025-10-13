import axios from "axios";
import { useAuthStore } from "@/stores/authStore";
import router from "@/router";

const apiClient = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 2000,
    withCredentials: true
})

apiClient.interceptors.request.use(

    (config) =>{
        const authStore = useAuthStore();
        const accessToken = authStore.accessToken;

        if(accessToken){
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
        
        return config;
    },
    (error) =>{
        return Promise.reject(error);
    }
);


apiClient.interceptors.response.use(
    (response) =>{
        return response;
    },
    async(error)=>{
        const orignalRequest = error.config;

        if(error.response.status === 401 && !orignalRequest._retry){
            orignalRequest._retry = true;

            try {
                const refreshResponse = await axios.post('/api/v1/auth/refresh');
                const newAccessToken = refreshResponse.data.accessToken;

                authStore.setAccessToken(newAccessToken);
                // 새로운 토큰으로 원래 요청의 헤더를 업데이트하고 재시도
                originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                console.log("리프레시 토큰 만료 갱신 성공");
                return axiosInstance(originalRequest);
            } catch (error) {
                // 리프레시 토큰 만료 등 갱신 실패 시 로그아웃
                //authStore.logout();
                console.log("리프레시 토큰 만료 갱신 실패");
                router.push('/');
                return Promise.reject(error);
            }
        }
        return Promise.reject(error);
    }
);

export default apiClient;