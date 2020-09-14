#!/usr/bin/env groovy

pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                dir('app')
                {
                    sh '${JENKINS_HOME}/grailsLink/grails refresh-dependencies --non-interactive'
                    sh '${JENKINS_HOME}/grailsLink/grails war --non-interactive ${JENKINS_HOME}/war_files/${BRANCH_NAME}_${BUILD_NUMBER}.war'
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

                script {
                    env.SERVERDEPLOY = input message: 'Choose your Server to deploy..  ', ok: 'Deploy!',
                            parameters: [choice(name: 'Server to deploy', choices: "${SERVER_DEV}\n${SERVER_QA}\n${SERVER_PROD}", description: '')]

                    echo "Server set to: ${SERVERDEPLOY}"
                }

                input("OK to continue the Deploying on Server ${env.SERVERDEPLOY}? ")

                script {
                    currentBuild.displayName = "${currentBuild.number}: Deploy on ${SERVERDEPLOY}"

                    if (SERVERDEPLOY == SERVER_DEV){
                        echo "Deploying on ${SERVERDEPLOY}...."
                        sh 'cp ${JENKINS_HOME}/war_files/${BRANCH_NAME}_${BUILD_NUMBER}.war ${TOMCAT_HOME_PATH}/default/webapps/ROOT.war'
                    }
                    else {
                        if (SERVERDEPLOY == SERVER_PROD){
                            input("Is the notification in the Invoice Tool activated? ")
                        }
                        sh 'cp ${JENKINS_HOME}/war_files/${BRANCH_NAME}_${BUILD_NUMBER}.war ${WORKSPACE}/ROOT.war'
                        writeFile file: "${WORKSPACE}/job.batch", text: "put /${WORKSPACE}/ROOT.war\n quit"
                        sh 'sftp -b ${WORKSPACE}/job.batch -i ${TOMCAT_HOME_PATH}/.ssh/id_rsa ${SERVERDEPLOY}:${TOMCAT_HOME_PATH}/default/webapps/'
                    }
                }
            }
        }
        stage('Post-Build') {
            steps {
                echo 'Post-Build'
            }
        }
    }
    post {
        success {
            echo 'I succeeeded!'

            script {
                env.changeLog = "No Changes"
                if (currentBuild.changeSets){
                    env.changeLog = "Change Log:\n\n\n"
                    echo 'Change Log'

                    def changeLogSets = currentBuild.changeSets
                        for (int i = 0; i < changeLogSets.size(); i++){
                            def entries = changeLogSets[i].items
                            for (int j = 0; j < entries.length; j++){
                                def entry = entries[j]
                                echo "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}"
                                env.changeLog += "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}\n"
                            }
                        }
                }
            }

            mail to: 'moetez.djebeniani@hbz-nrw.de, david.klober@hbz-nrw.de, miriam.konze@hbz-nrw.de, andreas.galffy@hbz-nrw.de, ingrid.bluoss@hbz-nrw.de, rupp@hbz-nrw.de, selbach@hbz-nrw.de, christin.seegert@hbz-nrw.de, sarah.dolguschin@hbz-nrw.de',
                    subject: "SUCCESS DEPLOY on Server ${SERVERDEPLOY}: ${currentBuild.fullDisplayName}",
                    body: "(¬‿¬) \n\nSuccessfully deployed ${env.BUILD_URL} on Server ${SERVERDEPLOY} \n\n\n${changeLog}"

            cleanWs()

            ///script {
            //    if ((SERVERDEPLOY == SERVER_QA) && LASER_SCRIPTS_PATH) {
            //        timeout(time: 10, unit: 'MINUTES') {
            //            sh "${LASER_SCRIPTS_PATH}/schemaSpy.sh"
            //        }
            //    }
            //}
        }
        unstable {
            echo 'I am unstable :/'
        }
        failure {
            echo 'I failed :('
            mail to: 'moetez.djebeniani@hbz-nrw.de, david.klober@hbz-nrw.de',
                    subject: "FAILED: ${currentBuild.fullDisplayName}",
                    body: "(ಠ_ಠ) \n\nFailed Deploy on Server ${SERVERDEPLOY} \n\n Something is wrong with ${env.BUILD_URL}"
        }
        changed {
            echo 'Things were different before...'
        }
    }
}
