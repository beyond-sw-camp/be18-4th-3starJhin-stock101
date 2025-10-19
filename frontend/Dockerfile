FROM node:20-alpine AS build

ENV NODE_ENV=production
ENV VITE_MODE=production

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build --mode production

FROM nginx:alpine
ENV TZ=Asia/Seoul
ENV NGINX_PORT=80

COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE ${NGINX_PORT}
CMD ["nginx", "-g", "daemon off;"]