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
                string(credentialsId: 'ECR_URI', variable: 'ECR_REPO'),
                string(credentialsId: 'shreya_ios_java', variable: 'ENV_VARS')
            ]) {
                sh """
                    ssh -o StrictHostKeyChecking=no ${DEPLOY_USER}@${DEPLOY_HOST} "
                        echo 'Creating temporary env file'
                        echo \"\$ENV_VARS\" > /tmp/inventory_env
                        chmod 600 /tmp/inventory_env

                        echo 'Login to ECR'
                        aws ecr get-login-password --region ${AWS_REGION} \
                            | docker login --username AWS --password-stdin \$ECR_REPO/inventory_management_system_java

                        echo 'Stopping old container'
                        docker stop ${CONTAINER_NAME} || true

                        echo 'Removing old container'
                        docker rm -f ${CONTAINER_NAME} || true

                        echo 'Starting new container'
                        docker run -d --name ${CONTAINER_NAME} --restart always -p 5000:5000 \
                            --env-file /tmp/inventory_env \
                            \$ECR_REPO/inventory_management_system_java:${IMAGE_TAG}

                        echo 'Deleting temporary env file'
                        rm -f /tmp/inventory_env

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