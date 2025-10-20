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
                    sh '''
                        git fetch --unshallow || true
                        git fetch origin main
                    '''
                    def changedFiles = sh(
                        script: 'git diff --name-only origin/main...HEAD',
                        returnStdout: true
                    ).trim().split("\\n")

                    env.BUILD_FRONT = changedFiles.any { it.startsWith("frontend/") } ? "true" : "false"
                    env.BUILD_BACK  = changedFiles.any { it.startsWith("backend/") } ? "true" : "false"

                    echo "Frontend changes: ${env.BUILD_FRONT}"
                    echo "Backend changes: ${env.BUILD_BACK}"

                    if (env.BUILD_FRONT == "false" && env.BUILD_BACK == "false") {
                        echo "No frontend or backend changes detected. Skipping build."
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
