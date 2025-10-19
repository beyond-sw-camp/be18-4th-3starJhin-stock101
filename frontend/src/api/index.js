import axios from "axios";
import { useAuthStore } from "@/stores/authStore";
import router from "@/router";

const apiClient = axios.create({
  baseURL: "/",
  timeout: 2000,
  withCredentials: true,
});

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
    (response) => {
        return response;
    },
    async (error) => {
        const orignalRequest = error.config;

        if (error.response.status === 401 && !orignalRequest._retry) {
            orignalRequest._retry = true;
            const authStore = useAuthStore();

            try {
                const refreshResponse = await axios.post('/api/v1/auth/refresh');
                const newAccessToken = refreshResponse.data.accessToken;

                authStore.setAccessToken(newAccessToken);
                
                orignalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                console.log("액세스 토큰 갱신 성공");
                return apiClient(orignalRequest);
            } catch (refreshError) {
                
                authStore.logout();
                console.log("리프레시 토큰 만료, 갱신 실패");
                router.push('/');
                return Promise.reject(refreshError);
            }
        }
        return Promise.reject(error);
    }
);

export default apiClient;