pipeline {
    agent {label "java"}

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
                    '''
                }
            }
        }

        stage('ECR Auth & Docker Build & Push') {
            steps {
                withCredentials([string(credentialsId: 'ECR_URI', variable: 'ECR_REPO')]) {
                    sh """
                        export PATH=\$PATH:/usr/local/bin
                        export AWS_PROFILE=default
                        export AWS_CONFIG_FILE=/home/ubuntu/.aws/config
                        export AWS_SHARED_CREDENTIALS_FILE=/home/ubuntu/.aws/credentials

                        echo "Login to ECR"
                        aws ecr get-login-password --region ${AWS_REGION} \
                        | docker login --username AWS --password-stdin \$ECR_REPO/inventory_management_system_java

                        echo "Building Image: ${IMAGE_TAG}"
                        docker build -t \$ECR_REPO/inventory_management_system_java:${IMAGE_TAG} .

                        echo "Pushing image"
                        docker push \$ECR_REPO/inventory_management_system_java:${IMAGE_TAG}

                        echo "Tag & push latest"
                        docker tag \$ECR_REPO/inventory_management_system_java:${IMAGE_TAG} \$ECR_REPO/inventory_management_system_java:latest
                        docker push \$ECR_REPO/inventory_management_system_java:latest
                    """
                }
            }
        }

        stage('Deploy on Private Server') {
    steps {
        sshagent(credentials: ['newnewnew']) {
            withCredentials([
                string(credentialsId: 'ECR_URI', variable: 'ECR_REPO')]) {
                sh """
                    ssh -o StrictHostKeyChecking=no ${DEPLOY_USER}@${DEPLOY_HOST} '
                        bash /home/ubuntu/docker_run.sh $ECR_REPO ${IMAGE_TAG} > /home/ubuntu/logs.txt
                    '
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