pipeline {
    agent any

    triggers {
        githubPush()
        pollSCM('H/5 * * * *')
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        booleanParam(name: 'RUN_TESTS', defaultValue: false, description: 'Run Maven tests before packaging')
        booleanParam(name: 'BUILD_DOCKER', defaultValue: true, description: 'Build Docker image after packaging')
        booleanParam(name: 'DEPLOY_LOCAL', defaultValue: false, description: 'Run the Docker image on the Jenkins agent')
        string(name: 'APP_PORT', defaultValue: '8080', description: 'Host port used when DEPLOY_LOCAL is enabled')
    }

    environment {
        APP_NAME = 'hr-management-system'
        IMAGE_NAME = 'hr-management-system'
        CONTAINER_NAME = 'hr-management-system'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare') {
            steps {
                script {
                    runCommand('chmod +x mvnw || true', 'if exist mvnw.cmd echo Maven wrapper ready')
                    runCommand('./mvnw -version', 'mvnw.cmd -version')
                }
            }
        }

        stage('Compile') {
            steps {
                script {
                    runCommand('./mvnw -q -DskipTests compile', 'mvnw.cmd -q -DskipTests compile')
                }
            }
        }

        stage('Test') {
            when {
                expression { return params.RUN_TESTS }
            }
            steps {
                script {
                    runCommand('./mvnw -q test', 'mvnw.cmd -q test')
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    runCommand('./mvnw -q -DskipTests package', 'mvnw.cmd -q -DskipTests package')
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build') {
            when {
                expression { return params.BUILD_DOCKER }
            }
            steps {
                script {
                    def imageTag = "${env.IMAGE_NAME}:${env.BUILD_NUMBER}"
                    def latestTag = "${env.IMAGE_NAME}:latest"
                    runCommand("docker build -f Dockerfile.ci -t ${imageTag} -t ${latestTag} .", "docker build -f Dockerfile.ci -t ${imageTag} -t ${latestTag} .")
                }
            }
        }

        stage('Deploy Local') {
            when {
                allOf {
                    expression { return params.BUILD_DOCKER }
                    expression { return params.DEPLOY_LOCAL }
                    expression { return env.BRANCH_NAME == null || env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master' }
                }
            }
            steps {
                script {
                    runCommand(
                        "docker rm -f ${env.CONTAINER_NAME} || true && docker run -d --name ${env.CONTAINER_NAME} -p ${params.APP_PORT}:8080 -e SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3306/hr_management_system?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=123456 -e GOOGLE_CLIENT_ID=jenkins-local -e GOOGLE_CLIENT_SECRET=jenkins-local -e FACEBOOK_CLIENT_ID=jenkins-local -e FACEBOOK_CLIENT_SECRET=jenkins-local ${env.IMAGE_NAME}:latest",
                        "docker rm -f ${env.CONTAINER_NAME} || exit /b 0\r\ndocker run -d --name ${env.CONTAINER_NAME} -p ${params.APP_PORT}:8080 -e SPRING_DATASOURCE_URL=\"jdbc:mysql://host.docker.internal:3306/hr_management_system?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh\" -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=123456 -e GOOGLE_CLIENT_ID=jenkins-local -e GOOGLE_CLIENT_SECRET=jenkins-local -e FACEBOOK_CLIENT_ID=jenkins-local -e FACEBOOK_CLIENT_SECRET=jenkins-local ${env.IMAGE_NAME}:latest"
                    )
                }
            }
        }
    }

    post {
        success {
            echo "CI/CD pipeline completed for ${env.APP_NAME}."
        }
        failure {
            echo "CI/CD pipeline failed. Check the stage logs above."
        }
    }
}

def runCommand(String unixCommand, String windowsCommand) {
    if (isUnix()) {
        sh unixCommand
    } else {
        bat windowsCommand
    }
}
