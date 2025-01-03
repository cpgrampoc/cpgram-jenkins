pipeline {
    agent any
    environment {
        DOCKER_HUB_CREDENTIALS = 'dockerhub-creds' // Jenkins credentials ID for Docker Hub
        DOCKER_IMAGE = 'cpgram/cpgram-application-service:latest'
    }
    stages {
        stage('Checkout Code') {
            steps {
                echo 'Checking out code...'
                checkout scm
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
                sh "docker build -t ${DOCKER_IMAGE} -f cpgram-backend/Dockerfile ."
            }
        }
        stage('Push Docker Image') {
            steps {
                echo 'Pushing Docker image to Docker Hub...'
                withCredentials([usernamePassword(credentialsId: DOCKER_HUB_CREDENTIALS, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'docker login -u $DOCKER_USER -p $DOCKER_PASS'
                    sh "docker push ${DOCKER_IMAGE}"
                }
            }
        }
        stage('Deploy Docker Image') {
            steps {
                echo 'Deploying Docker image...'
                sh '''
                # Remove the existing container
                docker stop cpgram-backend || true
                docker rm cpgram-backend || true

                # Run the updated container
                docker run -d --name cpgram-backend -p 8087:8087 ${DOCKER_IMAGE}
                '''
            }
        }
    }
    post {
        always {
            echo 'Performing cleanup...'
            cleanWs()
        }
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline execution failed!'
        }
    }
}
