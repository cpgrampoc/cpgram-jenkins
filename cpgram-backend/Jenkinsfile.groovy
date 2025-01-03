pipeline {
    agent any
    environment {
        DOCKER_HUB_CREDENTIALS = 'dockerhub-creds'
        IMAGE_NAME = 'cpgram/cpgram-application-service'
        CONTAINER_NAME = 'cpgram-backend'
    }
    stages {
        stage('Checkout Code') {
            steps {
                echo 'Checking out code...'
                checkout scm
            }
        }
        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh "docker build -t ${IMAGE_NAME}:latest -f cpgram-backend/Dockerfile ."
            }
        }
        stage('Push Docker Image') {
            steps {
                echo 'Pushing Docker image to Docker Hub...'
                withDockerRegistry([credentialsId: DOCKER_HUB_CREDENTIALS, url: '']) {
                    sh "docker push ${IMAGE_NAME}:latest"
                }
            }
        }
        stage('Deploy Docker Image') {
            steps {
                echo 'Deploying Docker image...'
                sh '''
                    # Stop and remove the old container
                    if [ $(docker ps -q -f name=${CONTAINER_NAME}) ]; then
                        docker stop ${CONTAINER_NAME}
                        docker rm ${CONTAINER_NAME}
                    fi
                    
                    # Run the new container
                    docker run -d --name ${CONTAINER_NAME} -p 8087:8087 ${IMAGE_NAME}:latest
                '''
            }
        }
    }
    post {
        always {
            echo 'Performing cleanup...'
            cleanWs()
        }
        failure {
            echo 'Pipeline execution failed!'
        }
        success {
            echo 'Pipeline execution completed successfully!'
        }
    }
}
