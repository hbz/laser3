#!/usr/bin/env groovy

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
                echo 'Building..'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
            }
        }
        stage('Deploy') {
            steps {

            script{
                    env.SERVERDELOPY = input message: 'On which Server you want to deploy', ok: 'Deploy!',
                                            parameters: [choice(name: 'Server to deploy', choices: "${SERVER_DEV}\n${SERVER_QA}\n${SERVER_PROD}", description: '')]
                    echo "Server Set to: ${SERVERDELOPY}"
                    echo "Deploying on ${SERVERDELOPY}...."
                }

                input('OK to continue?')
                script{
                    if(SERVERDELOPY == SERVER_DEV){
                        sh 'cp ${JENKINS_HOME}/war_files/${BRANCH_NAME}_${BUILD_NUMBER}.war ${TOMCAT_HOME_PATH}/default/webapps/ROOT.war'

                    }else{
                        sh 'cp ${JENKINS_HOME}/war_files/${BRANCH_NAME}_${BUILD_NUMBER}.war ${WORKSPACE}/ROOT.war'
                        writeFile file: "${WORKSPACE}/job.batch", text: "put /{WORKSPACE}/ROOT.war\n quit"
                        sh 'sftp -b ${WORKSPACE}/job.batch -i ${TOMCAT_HOME_PATH}/.ssh/id_rsa ${SERVERDELOPY}:${TOMCAT_HOME_PATH}/default/webapps/'
                    }
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
                mail to: 'moetez.djebeniani@hbz-nrw.de',
                             subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                             body: "Something is wrong with ${env.BUILD_URL}"
            }
            changed {
                echo 'Things were different before...'
            }
    }
}