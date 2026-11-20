pipeline {
    agent any
    options { disableConcurrentBuilds() }

    parameters {
        string(name: 'SOURCE_REF', defaultValue: 'main', description: 'Branch a validar e publicar.')
        booleanParam(name: 'DEPLOY_TO_AWS', defaultValue: false, description: 'Cria/inicia a EC2 e publica o task-service.')
    }

    environment {
        SOURCE_REF = "${params.SOURCE_REF}"
        AWS_DEFAULT_REGION = 'us-east-1'
        AWS_PAGER = ''
        AWS_PROFILE = 'lab'
    }

    stages {
        stage('Obter código da main') {
            steps {
                sh '/workspace/jenkins/scripts/checkout-main.sh https://github.com/grupo-do-alef/task-service.git "$WORKSPACE/source"'
            }
        }

        stage('Validar acesso AWS') {
            when { expression { params.DEPLOY_TO_AWS } }
            steps {
                sh 'aws sts get-caller-identity'
            }
        }

        stage('Compilar e testar') {
            steps {
                sh 'bash /workspace/jenkins/scripts/maven-verify.sh task-service "$WORKSPACE/source"'
            }
        }

        stage('Garantir EC2') {
            when { expression { params.DEPLOY_TO_AWS } }
            steps {
                sh '''
                    printf 'PUBLIC_IP=44.195.250.109\nUSER_SERVICE_URL=http://172.31.89.66:8080\n' > .ec2.env
                '''
            }
        }

        stage('Publicar') {
            when { expression { params.DEPLOY_TO_AWS } }
            steps {
                sshagent(credentials: ['ec2-deploy-key']) {
                    sh '. ./.ec2.env && cd "$WORKSPACE/source" && /workspace/jenkins/scripts/deploy-compose.sh task-service "$PUBLIC_IP" "http://23.20.190.24:3000" "$USER_SERVICE_URL"'
                }
            }
        }
    }
}
