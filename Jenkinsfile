#!/usr/bin/env groovy

pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Running Build ..'

                dir('app') {
                    sh '${JENKINS_HOME}/grailsLink/grails refresh-dependencies --non-interactive'
                    sh '${JENKINS_HOME}/grailsLink/grails war --non-interactive ${JENKINS_HOME}/war_files/${BRANCH_NAME}_${BUILD_NUMBER}.war'
                }
            }
        }
        stage('Test') {
            steps {
                echo 'Running Tests ..'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Running Deploy ..'

                env.SERVERDEPLOY = SERVER_DEV

                input("OK to deploy on server ${SERVERDEPLOY}?")
                script {
                    currentBuild.displayName = "${SERVERDEPLOY} - ${currentBuild.number}"

                    sh 'cp ${JENKINS_HOME}/war_files/${BRANCH_NAME}_${BUILD_NUMBER}.war ${TOMCAT_HOME_PATH}/default/webapps/ROOT.war'
                }
            }
        }
        stage('Post-Build') {
            steps {
                echo 'Running Post-Build ..'
            }
        }
    }
    post {
        success {
            echo 'SUCCESS :D'

            script {
                env.changeLog = "No Changes notified .."
                if (currentBuild.changeSets){
                    env.changeLog = "Change Log\n\n\n"
                    echo 'Current changes:'

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

            mail to: 'david.klober@hbz-nrw.de',
                 subject: "DEPLOY SUCCESSFULL: ${SERVERDEPLOY} - ${currentBuild.fullDisplayName}",
                 body: "(¬‿¬) \n\nSuccessfully deployed ${env.BUILD_URL} for ${SERVERDEPLOY}\n\n${changeLog}"

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
            echo 'FAILED :('

            mail to: 'david.klober@hbz-nrw.de',
                 subject: "DEPLOY FAILED: ${currentBuild.fullDisplayName}",
                 body: "(ಠ_ಠ) \n\nDeploy failed for ${SERVERDEPLOY}\n\nSomething went wrong with ${env.BUILD_URL}"
        }
        changed {
            echo 'Things were different before ..'
        }
    }
}
