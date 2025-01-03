pipeline {
    agent any
    stages {
        stage('Checkout Backend Code') {
            steps {
                git branch: 'dev', url: 'https://github.com/cpgrampoc/backend.git'
            }
        }
        stage('Build Maven Project') {
            steps {
                echo 'Building Maven project...'
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh 'docker build -t cpgram/backend-application:latest .'
            }
        }
        stage('Push Docker Image') {
            steps {
                echo 'Pushing Docker image to Docker Hub...'
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                    sh 'docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD'
                }
                sh 'docker push cpgram/backend-application:latest'
            }
        }
        stage('Deploy Docker Image') {
            steps {
                echo 'Deploying Docker image to the server...'
                sh '''
                docker stop cpgram-backend || true
                docker rm cpgram-backend || true
                docker run -d --name cpgram-backend -p 8087:8087 cpgram/backend-application:latest
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
