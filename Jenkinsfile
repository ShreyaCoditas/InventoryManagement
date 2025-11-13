pipeline {
    agent {label "java-jenkins-agent"}

    environment {
        AWS_REGION = "ap-south-1"
        CONTAINER_NAME = "inventory_management"
        DEPLOY_USER = "ubuntu"
        DEPLOY_HOST = credentials('java_deploy_host')

    }

    stages {

        stage('Checkout') {
            steps {
                script {
                    def commit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.COMMIT_ID = commit
                    env.IMAGE_TAG = "build-${env.BUILD_NUMBER}-${commit}"
                    echo "Commit ID: ${commit}"
                    echo "Image Tag: ${env.IMAGE_TAG}"
                }
            }
        }


        stage("Build for static analysis") {
            steps {
                sh "mvn clean install -DskipTests"
            }
        }

        stage("SonarQube Analysis and Report Generation") {
            steps{
                withSonarQubeEnv("java-sonarqube-server") {
                    sh '''
                        mvn sonar:sonar \
                            -Dsonar.projectKey="shreya_inventory_management" \
                            -Dsonar.projectName="shreya_inventory_management" \
                            -Dsonar.login="sonarqube-java
                    '''
                }
            }
        }

        stage('ECR Auth & Docker Build & Push') {
            steps {
                withCredentials([string(credentialsId: 'ECR_URI', variable: 'ECR_REPO')]) {
                    sh """
                        export PATH=\$PATH:/usr/local/bin

                        echo "Login to ECR"
                        aws ecr get-login-password --region ${AWS_REGION} \
                        | docker login --username AWS --password-stdin \$ECR_REPO/inventory_management_system

                        echo "Building Image: ${IMAGE_TAG}"
                        docker build -t \$ECR_REPO/inventory_management_system:${IMAGE_TAG} .

                        echo "Pushing image"
                        docker push \$ECR_REPO/inventory_management_system:${IMAGE_TAG}

                        echo "Tag & push latest"
                        docker tag \$ECR_REPO/inventory_management_system:${IMAGE_TAG} \$ECR_REPO:latest
                        docker push \$ECR_REPO/inventory_management_system:latest
                    """
                }
            }
        }


        stage('Deploy on Private Server') {
            steps {
                sshagent(credentials: ['newnewnew']) {
                    withCredentials([string(credentialsId: 'ECR_URI', variable: 'ECR_REPO')]) {
                        sh """
                            ssh -o StrictHostKeyChecking=no ${DEPLOY_USER}@${DEPLOY_HOST} "
                                echo 'Login to ECR'
                                aws ecr get-login-password --region ${AWS_REGION} \
                                | docker login --username AWS --password-stdin \$ECR_REPO/inventory_management_system

                                echo 'Stopping old container'
                                docker stop ${CONTAINER_NAME} || true

                                echo 'Removing old container'
                                docker rm -f ${CONTAINER_NAME} || true

                                echo 'Starting new container'
                                docker run -d --name ${CONTAINER_NAME} --restart always -p 5000:5000 \
                                    -e PORT=\${PORT} \
                                    -e DB_USER=\${DB_USER} \
                                    -e DB_NAME=\${DB_NAME} \
                                    -e DB_PASSWORD=\${DB_PASSWORD} \
                                    -e DB_HOST=\${DB_HOST} \
                                    -e JWT_SECRET=\${JWT_SECRET} \
                                    -e SUPER_ADMIN_EMAIL=\${SUPER_ADMIN_EMAIL} \
                                    -e SUPER_ADMIN_PASSWORD=\${SUPER_ADMIN_PASSWORD} \
                                    -e API_KEY=\${API_KEY} \
                                    \$ECR_REPO/inventory_management_system:\${IMAGE_TAG}

                                echo 'Pruning old images'
                                docker image prune -f
                            "
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            googlechatnotification(
                url: "id:gchat-jenkins-webhook",
                message: "${env.JOB_NAME}: Build #${env.BUILD_NUMBER} -> SUCCESS"
            )
        }
        failure {
            googlechatnotification(
                url: "id:gchat-jenkins-webhook",
                message: "${env.JOB_NAME}: Build #${env.BUILD_NUMBER} -> FAILED"
            )
        }
    }
}