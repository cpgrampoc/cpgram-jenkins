pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "cpgram/backend-application:latest"
        DOCKER_HUB_CREDENTIALS = 'dockerhub-creds' // This matches your Jenkins credentials ID
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
                sh 'docker build -t $DOCKER_IMAGE .'
            }
        }

        stage('Push Docker Image') {
            steps {
                echo 'Pushing Docker image to Docker Hub...'
                withDockerRegistry(credentialsId: "$DOCKER_HUB_CREDENTIALS", url: '') {
                    sh 'docker push $DOCKER_IMAGE'
                }
            }
        }

        stage('Deploy Docker Image') {
            steps {
                echo 'Deploying Docker image...'
                sh '''
                docker pull $DOCKER_IMAGE
                docker stop backend-container || true
                docker rm backend-container || true
                docker run -d --name backend-container -p 8080:8080 $DOCKER_IMAGE
                '''
            }
        }
    }

    post {
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline execution failed!'
        }
        always {
            echo 'Performing cleanup...'
            cleanWs()
        }
    }
}
