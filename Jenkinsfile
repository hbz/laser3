

def deploy_server

pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                dir('app')
                {
                    sh 'grails refresh-dependencies --non-interactive'
                    sh 'grails war --non-interactive ${JENKINS_HOME}/war_files/${BRANCH_NAME}_${BUILD_NUMBER}.war'
                }
                echo 'Building..${SERVER_DEV}'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..${env.SERVER_PROD}'
            }
        }
        stage('Deploy') {
            steps {
                input message: 'On which server do you want deploy?', ok: 'Deploy!',
                                                         parameters: [choice(name: 'DEPLOY_SERVER', choices: ['DEV','QA','PROD'], description: 'Which Server?')]


                    sh 'cp ${JENKINS_HOME}/war_files/${BRANCH_NAME}_${BUILD_NUMBER}.war ${WORKSPACE}/../../../default/webapps/ROOT.war'
                                    echo 'Deploying on ${'SERVER_'+params.DEPLOY_SERVER}....'

            }
        }
        stage('Post-Build'){
            steps {
                echo 'Post-Build'
            }
        }
    }
    post {
            success {
                echo 'I succeeeded!'
                mail to: 'moetez.djebeniani@hbz-nrw.de',
                                             subject: "Succeeeded Pipeline: ${currentBuild.fullDisplayName}",
                                             body: "All Right: ${env.BUILD_URL}"
                cleanWs()
            }
            unstable {
                echo 'I am unstable :/'
            }
            failure {
                echo 'I failed :('
                mail to: 'moetez.djebeniani@hbz-nrw.de, david.klober@hbz-nrw.de',
                             subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                             body: "Something is wrong with ${env.BUILD_URL}"
                 cleanWs()
            }
            changed {
                echo 'Things were different before...'
            }
    }
}