pipeline {
    agent any
    environment {
        DOCKER_HUB_CREDENTIALS = 'dockerhub-creds' // Replace with your Docker Hub credentials ID
    }
    stages {
        stage('Checkout Backend Code') {
            steps {
                echo 'Checking out backend code...'
                git branch: 'dev', url: 'https://github.com/cpgrampoc/backend.git'
            }
        }
        stage('Build Maven Project') {
            steps {
                echo 'Building Maven project...'
                dir('cpgram-application-service') { // Navigate to the correct directory
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                dir('cpgram-application-service') { // Ensure Docker context is correct
                    sh 'docker build -t cpgram/cpgram-application-service:latest -f Dockerfile .'
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                echo 'Pushing Docker image to Docker Hub...'
                withCredentials([usernamePassword(credentialsId: "${DOCKER_HUB_CREDENTIALS}", usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh 'echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin'
                    sh 'docker push cpgram/cpgram-application-service:latest'
                }
            }
        }
        stage('Deploy Docker Image') {
            steps {
                echo 'Deploying Docker image...'
                sh '''
                docker stop cpgram-backend || true
                docker rm cpgram-backend || true
                docker pull cpgram/cpgram-application-service:latest
                docker run -d --name cpgram-backend -p 8087:8087 cpgram/cpgram-application-service:latest
                '''
            }
        }
    }
    post {
        always {
            echo 'Cleaning up workspace...'
            cleanWs()
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
