pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'akilmusic-app'
        DOCKER_TAG = "${BUILD_NUMBER}"
        SONAR_PROJECT_KEY = 'sonify'
    }

    tools {
        maven 'Maven'
        jdk 'JDK'
        dockerTool 'Docker'
    }

    stages {
            stage('Checkout') {
                steps {
                    echo "Checking out the code..."
                    checkout scm
                }
            }

            stage('Build') {
                steps {
                    echo "Building the application..."
                    sh 'mvn clean package -DskipTests'
                }
            }

            stage('Test') {
                steps {
                    echo "Running tests..."
                    sh 'mvn test'
                }
                post {
                    always {
                        junit '**/target/surefire-reports/*.xml'
                    }
                }
            }

            stage('Debug') {
                steps {
                    echo "Running Debug Info..."
                    sh 'printenv'
                    sh 'mvn --version'
                    sh 'docker --version'
                }
            }


                      stage('Install Docker Compose') {
                          steps {
                              script {
                                  sh '''
                                  # Install Docker Compose if it's not installed
                                  if ! command -v docker-compose &> /dev/null; then
                                      curl -L "https://github.com/docker/compose/releases/download/$(curl -s https://api.github.com/repos/docker/compose/releases/latest | jq -r .tag_name)/docker-compose-$(uname -s)-$(uname -m)" -o /tmp/docker-compose
                                      chmod +x /tmp/docker-compose
                                      sudo mv /tmp/docker-compose /usr/local/bin/docker-compose
                                  fi
                                  '''
                              }
                          }
                      }

            stage('Deploy') {
                steps {
                    echo "Deploying the app..."
                    sh '''
                        docker-compose down || true
                        docker-compose up -d
                    '''
                }
            }
        }

        post {
            always {
                echo "Cleaning workspace..."
                cleanWs()
            }
        }
    }