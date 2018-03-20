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
                input('OK to continue?')
                sh 'cp ${JENKINS_HOME}/war_files/${BRANCH_NAME}_${BUILD_NUMBER}.war ${WORKSPACE}/../../../default/webapps/ROOT.war'
                echo 'Deploying....'
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
