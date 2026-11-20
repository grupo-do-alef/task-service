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
                    USER_SERVICE_URL="http://$(/workspace/jenkins/scripts/service-ip.sh user-service private):8080"
                    test "$USER_SERVICE_URL" != 'http://None:8080'
                    /workspace/jenkins/scripts/ensure-ec2.sh task-service > .ec2.env
                    printf 'USER_SERVICE_URL=%s\n' "$USER_SERVICE_URL" >> .ec2.env
                '''
            }
        }

        stage('Publicar') {
            when { expression { params.DEPLOY_TO_AWS } }
            steps {
                sshagent(credentials: ['ec2-deploy-key']) {
                    sh '. ./.ec2.env && cd "$WORKSPACE/source" && /workspace/jenkins/scripts/deploy-compose.sh task-service "$PUBLIC_IP" "http://localhost:3000" "$USER_SERVICE_URL"'
                }
            }
        }
    }
}
