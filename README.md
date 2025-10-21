# ![header](https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=10&height=200&animation=twinkling&section=header&text=stock101&fontSize=50&fontAlign=75&fontAlignY=36)

# 📈Stock101 - 투자를 근거있게

## 👀목차
1. [👥팀원](#팀원)
2. [📚프로젝트 개요](#프로젝트-개요)  
	2.2 [🎯요구사항 분석](#요구사항-분석)
3. [🔧시스템 아키텍쳐](#시스템-아키텍쳐)
4. [📅요구사항 명세서](#요구사항-명세서)
5. [🪧ERD](#ERD)
6. [🗃️테이블 명세서](#테이블-명세서)
7. [🎯API 명세서](#API-명세서)
8. [🎬CI/CD Flow](#CI/CD-Flow)
9. [🧪Unit Test](#Unit-Test)
---
### 👥팀원

| 박종원 | 임성민 | 이승진 | 조원석 | 손혜원 |
| --- | --- | --- | --- | --- |

## 📌프로젝트 개요
### 📘 프로젝트 소개

Stock101은 S&P500 상위 10개 종목을 중심으로 기업 정보 + 투자자 커뮤니티를 통합 제공하는 웹 기반 플랫폼이다.
투자자들이 “왜 이 기업이 지금 주목받는가?”를 스스로 분석하고 토론할 수 있게, 기초 재무 데이터부터 뉴스, 투자 지표, 커뮤니티 콘텐츠까지 한 곳에서 제공하는 걸 목표로 한다.
이 플랫폼이 사용자들에게 “투자의 바이블(Bible)”처럼 신뢰받는 참고 자료가 되는 게 궁극적인 목표이다.

### ✅ 왜 이 플랫폼이 필요한가?
• 정보의 편향성 극복

보통 투자자는 기관 리포트나 애널리스트 의견에 많이 의존하게 된다.
하지만 플랫폼을 통해 일반 투자자들도 기업 정보를 직접 보고 해석하고, 커뮤니티 피드백도 참고할 수 있으면 정보의 균형이 맞춰질 수 있다.

• 투자자 심리 + 시장 흐름 연결

단순히 기업의 재무만 보는 걸로는 부족하며 시장 참여자(특히 개인투자자)의 움직임이 종목 흐름을 바꾸는 경우가 잦다.
최근엔 ‘동학개미’들이 증시를 흔드는 ‘큰손’으로 불릴 정도로 영향력이 커지고 있다는 기사도 나온 적이 있다.  ￼

• 트렌드 감지 + 토론 공간 제공

어떤 뉴스나 테마가 빨리 퍼지는지, 사람들이 그걸 어떻게 해석하는지 커뮤니티에서 힌트를 얻을 수 있어야 한다.
또, 각종 지표 + 토론 + 유저 간 상호작용이 결합되면 유의미한 투자 아이디어도 나올 수 있다.

## 📑요구사항 분석
### 🔍 1. 기업 분석
- 뉴스: 종목별 뉴스 크롤링, 날짜별 조회, 인기 뉴스 강조
- 투자 지표: PER, PBR, EPS, BPS, ROE, ROA 제공
- 재무 제표: 분기/연도 기준 손익계산서, 대차대조표, 현금흐름표
- 기업 개요 및 사업 내용: 설립연도, 본사 위치, 섹터 등 정보
- 배당 정보: 최근 4분기 배당금, 배당락일, 수익률 등
- 기업 이벤트 캘린더: IR 일정, 공시 정보 통합 제공


### 📈 2. 시장 데이터
- 실시간 주가 정보: 종목별 주가 흐름 및 등락률
- 시장지표: S&P500, 다우지수 등 주요 지수 제공
- 주식 종목별 데이터 시각화: 차트/히트맵 기반 인터페이스


### 👤 3. 유저 기능
- 회원 관리: 가입, 로그인, 회원탈퇴, 인증 절차
- 사용자 정보: 개인화된 투자 성향 및 선호 설정
- 활동 내역: 댓글, 게시글, 조회 기록 등 저장
- 관심 목록 관리: 종목 즐겨찾기 및 알림 기능
- 계정 보안: 비밀번호 변경, 이중 인증 등 보안 강화


### 💬 4. 커뮤니티
- 게시판: 종목 토론, 자유게시판, Q&A 등 다양한 공간 제공
- 위키: 사용자 기반 종목/용어 설명 편집 시스템
- 외부 커뮤니티 연동: Reddit, 증권 커뮤니티 등 연계
- 관리자 컨택: 신고/문의 기능 제공
- 사용자 등급/평판 시스템: 활동 기반 등급 부여 및 뱃지 시스템

## 시연
<details><summary><strong>Overview</strong></summary>
  
<details><summary>Stock101 빠르게 살펴보기</summary>
<img src="https://github.com/user-attachments/assets/0ed82ffa-ab9b-4b2c-8a46-9bc9b6d37550" alt="살펴보기" />
</details>
</details>


<details><summary><strong>로그인/회원가입</strong></summary>
  
<details><summary>회원가입</summary>
<img src="https://github.com/user-attachments/assets/b3ee4078-2de2-4838-8de5-02fc31d6daf6" alt="회원가입" />
</details>

<details><summary>로그인</summary>
<img src="https://github.com/user-attachments/assets/e11f9460-88c0-4cfb-bfd7-1c9670832602" alt="로그인" />
</details>
</details>

<details><summary><strong>내 정보</strong></summary>
  
<details><summary>내 정보 조회 및 수정</summary>
<img src="https://github.com/user-attachments/assets/10262347-8098-44c6-abae-ab0f3e17cbd2" alt="내 정보" />
</details>

</details>
<details><summary><strong>주식 상세</strong></summary>
  
<details><summary>주식 상세 화면</summary>
<img src="https://github.com/user-attachments/assets/e287bcd5-9fc4-4eb7-a719-f2b6b9ccc055" alt="주식 상세 화면" />
</details>
</details>

</details>
<details><summary><strong>커뮤니티</strong></summary>
  
<details><summary>게시물 등록</summary>
<img src="https://github.com/user-attachments/assets/10178f01-3bf4-4b4c-b31b-5b9969fe55f8" alt="게시물 등록" />
</details>


<details><summary>게시물 좋아요</summary>
<img src="https://github.com/user-attachments/assets/6ee028e4-9408-4166-9022-0a220d5b5903" alt="게시물 좋아요" />
</details>

<details><summary>게시물 댓글 등록 및 대댓글 등록</summary>
<img src="https://github.com/user-attachments/assets/5230a957-357a-43d8-bf4c-e4b5c4fd5b7d" alt="게시물 댓글 등록 및 대댓글 등록" />
</details>

</details>
<details><summary><strong>예측</strong></summary>
  
<details><summary>예측 등록</summary>
<img src="https://github.com/user-attachments/assets/91cb5af2-415c-4eb5-a64a-7a0745eb01b3" alt="예측 등록" />
</details>

<details><summary>다른 사용자 예측 조회</summary>
<img src="https://github.com/user-attachments/assets/26d9952f-9e61-4b8a-8b52-46f2a1ddfda9" alt="다른 사용자 예측 조회" />
</details>

</details>



## 🔧시스템 아키텍쳐
<img width="1230" height="589" alt="image" src="https://github.com/user-attachments/assets/5ff0ed2d-797f-4000-91a0-fabd99f7b10e" />

## [📋요구사항 명세서](https://docs.google.com/spreadsheets/d/14wwJn_T3o99kF_oEq8uZ0xYGWGwoTiGmOdv44CbQwt0/edit?gid=434890356#gid=434890356)
<img width="1428" height="819" alt="image" src="https://github.com/user-attachments/assets/aaf346ab-7e0c-4373-b899-f4df031fd0d0" />

## [🪧ERD](https://www.erdcloud.com/d/zo7c3EtFTFLD6g6gD)
<img width="2808" height="1647" alt="Stock101-3" src="https://github.com/user-attachments/assets/c13278dc-4556-4397-8f58-511caae2f100" />


## [🗃️테이블 명세서](https://docs.google.com/spreadsheets/d/1aHppUl-7VBDqVcq6Ckb8BzpCgmxDSZCnURZARkLruMo/edit?usp=sharing)
<img width="1582" height="744" alt="image" src="https://github.com/user-attachments/assets/81813c0d-3465-46ae-b7c4-3ef0fc4eeaf9" />


## [🎯API 명세서](https://ber5f1ykpv.apidog.io/)
<img width="1445" height="1082" alt="image" src="https://github.com/user-attachments/assets/68fbff20-239e-419f-bcd8-bb7c196edfe6" />

## 🔗CI/CD 파이프라인
<details>
	<summary><b>세부정보</b></summary>
	
```groovy
pipeline {
    agent {
        kubernetes {
            yaml '''
            apiVersion: v1
            kind: Pod
            metadata:
              name: fullstack-agent
            spec:
              containers:
              - name: node
                image: node:20-alpine
                command: ["cat"]
                tty: true
              - name: maven
                image: maven:3.9.9-eclipse-temurin-21-alpine
                command: ["cat"]
                tty: true
              - name: docker
                image: docker:28.5.1-cli-alpine3.22
                command: ["cat"]
                tty: true
                volumeMounts:
                - name: docker-socket
                  mountPath: "/var/run/docker.sock"
              - name: git
                image: alpine/git
                command: ["cat"]
                tty: true
              volumes:
              - name: docker-socket
                hostPath:
                  path: "/var/run/docker.sock"
            '''
        }
    }

    environment {
        FRONT_IMAGE = 'iiijong/frontend'
        BACK_IMAGE  = 'iiijong/backend'
        DOCKER_CREDENTIALS_ID = 'dockerhub-access'
        GITOPS_REPO = 'https://github.com/IIIjong/stock101-k8s-manifests.git'
        GITOPS_CREDENTIALS_ID = 'github-credentials'
        DISCORD_WEBHOOK_CREDENTIALS_ID = 'discord-webhook'
    }

    stages {

        stage('Detect Changes') {
            steps {
                script {
                    echo "Checking changed files..."
                    def changedFiles = sh(
                        script: 'git diff --name-only HEAD~1 HEAD',
                        returnStdout: true
                    ).trim().split("\\n")

                    env.BUILD_FRONT = changedFiles.any { it.startsWith("frontend/") } ? "true" : "false"
                    env.BUILD_BACK  = changedFiles.any { it.startsWith("backend/") } ? "true" : "false"

                    if (env.BUILD_FRONT == "false" && env.BUILD_BACK == "false") {
                        echo "No frontend or backend changes detected."
                        currentBuild.result = 'SUCCESS'
                        return
                    }
                }
            }
        }

        stage('Frontend Build') {
            when { expression { env.BUILD_FRONT == "true" } }
            steps {
                container('node') {
                    dir('frontend') {
                        echo "Building frontend..."
                        sh '''
                            npm ci
                            npm run build
                        '''
                    }
                }
            }
        }

        stage('Backend Unit Test') {
            when { expression { env.BUILD_BACK == "true" } }
            steps {
                container('maven') {
                    dir('backend') {
                        echo "Running backend unit tests..."
                        sh '''
                            mvn -B clean test
                        '''
                    }
                }
            }
        }

        stage('Backend Build') {
            when { expression { env.BUILD_BACK == "true" } }
            steps {
                container('maven') {
                    dir('backend') {
                        echo "Packaging backend..."
                        sh '''
                            mvn -B clean package -DskipTests
                        '''
                    }
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                container('docker') {
                    script {
                        def tag = env.BUILD_NUMBER
                        withCredentials([usernamePassword(
                            credentialsId: DOCKER_CREDENTIALS_ID,
                            usernameVariable: 'DOCKER_USERNAME',
                            passwordVariable: 'DOCKER_PASSWORD'
                        )]) {
                            sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
                        }

                        if (env.BUILD_FRONT == "true") {
                            dir('frontend') {
                                sh """
                                    echo "Building frontend Docker image..."
                                    docker build --no-cache -t $FRONT_IMAGE:$tag .
                                    docker push $FRONT_IMAGE:$tag
                                """
                            }
                        }

                        if (env.BUILD_BACK == "true") {
                            dir('backend') {
                                sh """
                                    echo "Building backend Docker image..."
                                    docker build --no-cache -t $BACK_IMAGE:$tag .
                                    docker push $BACK_IMAGE:$tag
                                """
                            }
                        }
                    }
                }
            }
        }

        stage('Update GitOps Repository') {
            steps {
                container('git') {
                    script {
                        def tag = env.BUILD_NUMBER
                        withCredentials([usernamePassword(
                            credentialsId: GITOPS_CREDENTIALS_ID,
                            usernameVariable: 'GIT_USERNAME',
                            passwordVariable: 'GIT_PASSWORD'
                        )]) {
                            sh '''
                                rm -rf stock101-k8s-manifests
                                git clone https://$GIT_USERNAME:$GIT_PASSWORD@github.com/IIIjong/stock101-k8s-manifests.git stock101-k8s-manifests
                            '''

                            if (env.BUILD_FRONT == "true") {
                                sh """
                                    cd stock101-k8s-manifests/k8s/frontend
                                    sed -i 's|image: .*|image: ${FRONT_IMAGE}:${tag}|' deployment.yaml
                                    git config user.name "iiijong"
                                    git config user.email "pjwfish@naver.com"
                                    git add deployment.yaml
                                    git commit -m "update frontend image to ${tag}" || true
                                """
                            }

                            if (env.BUILD_BACK == "true") {
                                sh """
                                    cd stock101-k8s-manifests/k8s/backend
                                    sed -i 's|image: .*|image: ${BACK_IMAGE}:${tag}|' deployment.yaml
                                    git config user.name "iiijong"
                                    git config user.email "pjwfish@naver.com"
                                    git add deployment.yaml
                                    git commit -m "update backend image to ${tag}" || true
                                """
                            }

                            sh '''
                                cd stock101-k8s-manifests
                                git push origin main
                            '''
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            withCredentials([string(
                credentialsId: DISCORD_WEBHOOK_CREDENTIALS_ID,
                variable: 'DISCORD_WEBHOOK_URL'
            )]) {
                discordSend description: """
                Fullstack CI/CD Build Summary

                Result: ${currentBuild.currentResult}
                Job: ${env.JOB_NAME}
                Build: ${currentBuild.displayName}
                Frontend Changed: ${env.BUILD_FRONT}
                Backend Changed: ${env.BUILD_BACK}
                Duration: ${(currentBuild.duration / 1000).intValue()}s
                """,
                result: currentBuild.currentResult,
                title: "Fullstack CI/CD (GitOps Auto Deploy)",
                webhookURL: "${DISCORD_WEBHOOK_URL}"
            }
        }
    }
}
```

</details>

## 🎬CI/CD Flow
### 1. 코드 수정 후 Commit & Push
![커밋.gif](./docs/커밋.gif)

### 2. 젠킨스 수행
[![시연 영상 보기](https://img.shields.io/badge/Watch%20on-Google%20Drive-blue?logo=google-drive&logoColor=white)](https://drive.google.com/file/d/1GCoVwWQh5lDIPsXWY5ST48eVvDx1qd5C/view)

<img width="1213" height="610" alt="image" src="https://github.com/user-attachments/assets/41134be9-e480-4d56-b10d-078eb4cf1271" />


### 3. 디스코드 알림 확인
![디스코드-알림.png](./docs/디스코드-알림.png)

### 4. Docker Hub에 Push된 이미지 확인
#### 4.1. Backend
![](./docs/도커허브백엔드.png)
#### 4.2. Frontend
![](./docs/도커허브프론트.png)

### 5. Manifests 반영 결과
![](./docs/매니페스트-결과.png)

### 6. ArgoCD 배포 확인
#### 6.1. Backend
![](./docs/argocd백엔드.png)
#### 6.1. Frontend
![](./docs/argocd프론트.png)

![](./docs/프론트결과.gif)

## 🧪 Unit Test
<details>
  <summary>📌 <b>CommunityServiceTest</b></summary>
  <img src="./docs/CommunityServiceTest.png" alt="CommunityServiceTest" width="600"/>
</details>

<details>
  <summary>📈 <b>IndicatorServiceImplTest</b></summary>
  <img src="./docs/IndicatorServiceImplTest.png" alt="IndicatorServiceImplTest" width="600"/>
</details>

<details>
  <summary>📰 <b>NewsServiceImplTest</b></summary>
  <img src="./docs/NewsServiceImplTest.png" alt="NewsServiceImplTest" width="600"/>
</details>

<details>
  <summary>🔮 <b>PredicationServiceImplTest</b></summary>
  <img src="./docs/PredicationServiceImplTest.png" alt="PredicationServiceImplTest" width="600"/>
</details>

<details>
  <summary>💹 <b>StockRestClientServiceTest</b></summary>
  <img src="./docs/StockRestClientServiceTest.png" alt="StockRestClientServiceTest" width="600"/>
</details>

<details>
  <summary>📊 <b>StockServiceImplTest</b></summary>
  <img src="./docs/StockServiceImplTest.png" alt="StockServiceImplTest" width="600"/>
</details>

## 회고록
|   이 름  | 내용 |
|--------|--------|
| 박종원 |  |
| 손혜원 |  |
| 이승진 |  |
| 임성민 |  |
| 조원석 |  |
