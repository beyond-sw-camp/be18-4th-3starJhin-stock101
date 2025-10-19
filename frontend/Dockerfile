FROM node:20-alpine AS build

LABEL maintainer="hong123 <hong123@beyond.com>"
LABEL version="1.0"
LABEL description="Vue.js 프론트엔드 빌드 이미지"

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine

LABEL maintainer="hong123 <hong123@beyond.com>"
LABEL version="1.0"
LABEL description="Vue.js 정적 파일을 제공하는 Nginx 서버"

ENV TZ=Asia/Seoul
ENV NGINX_PORT=80

COPY --from=build /app/dist /usr/share/nginx/html

COPY ./nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE ${NGINX_PORT}
CMD ["nginx", "-g", "daemon off;"]