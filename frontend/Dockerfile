FROM node:20-alpine AS build

LABEL maintainer="hong123 <hong123@beyond.com>"
LABEL version="1.0"
LABEL description="Vue.js 프론트엔드 빌드 이미지"

# 작업 디렉토리 생성
WORKDIR /app

# 패키지 복사 및 의존성 설치
COPY package*.json ./
RUN npm install

# 전체 코드 복사 후 빌드 실행
COPY . .
RUN npm run build

FROM nginx:alpine

# 메타데이터 (관리 목적)
LABEL maintainer="hong123 <hong123@beyond.com>"
LABEL version="1.0"
LABEL description="Vue.js 정적 파일을 제공하는 Nginx 서버"

# 타임존 및 환경 변수 설정
ENV TZ=Asia/Seoul
ENV NGINX_PORT=80

# Nginx 기본 HTML 디렉토리에 빌드 결과 복사
COPY --from=build /app/dist /usr/share/nginx/html

# 필요 시 커스텀 nginx.conf도 복사 가능 (옵션)
# COPY ./nginx.conf /etc/nginx/conf.d/default.conf

# 포트 노출
EXPOSE ${NGINX_PORT}

# 실행
CMD ["nginx", "-g", "daemon off;"]
