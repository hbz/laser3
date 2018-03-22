

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
                echo 'Testing..${SERVER_DEV}'
            }
        }
        stage('Deploy') {
            steps {
                deploy_server = input message: 'On which server do you want deploy?', ok: 'Deploy!',
                                                         parameters: [choice(name: 'DEPLOY_SERVER', choices: ['DEV','QA','PROD'], description: 'Which Server?')]

                if(deploy_server == "DEV")
                {
                    sh 'cp ${JENKINS_HOME}/war_files/${BRANCH_NAME}_${BUILD_NUMBER}.war ${WORKSPACE}/../../../default/webapps/ROOT.war'
                                    echo 'Deploying on ${SERVER_DEV}....'
                }
                elsif(deploy_server == "QA")
                {
                    sh 'cp ${JENKINS_HOME}/war_files/${BRANCH_NAME}_${BUILD_NUMBER}.war ${WORKSPACE}/../../../default/webapps/ROOT.war'
                                    echo 'Deploying on ${SERVER_QA}....'
                }
                elsif(deploy_server == "PROD")
                {
                    sh 'cp ${JENKINS_HOME}/war_files/${BRANCH_NAME}_${BUILD_NUMBER}.war ${WORKSPACE}/../../../default/webapps/ROOT.war'
                                    echo 'Deploying on ${SERVER_PROD}....'
                }
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