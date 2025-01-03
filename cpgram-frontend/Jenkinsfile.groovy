pipeline {
    agent any
    environment {
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials-id'
        DOCKER_IMAGE = 'cpgram/frontend-application'
    }
    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE:latest .'
            }
        }
        stage('Push Docker Image') {
            steps {
                withDockerRegistry([credentialsId: "$DOCKER_CREDENTIALS_ID", url: ""]) {
                    sh 'docker push $DOCKER_IMAGE:latest'
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
