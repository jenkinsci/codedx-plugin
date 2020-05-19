pipeline {
    agent {
        label 'codebuild-small'
    }
    
    stages {
        stage('Build') {
            steps {
                git 'https://github.com/jenkinsci/codedx-plugin'
                
                sh "mvn clean package"
                
                archiveArtifacts artifacts: 'target/*.hpi', fingerprint: true, onlyIfSuccessful: true
            }
            
			post {
				success {
					slackSend channel: '#bots', message: "Code Dx Jenkins Plugin build successful (<${env.BUILD_URL}|Open>)"
				}

				unstable {
					slackSend channel: '#devchat', color: 'warning', message: "Code Dx Jenkins Plugin build unstable (<${env.BUILD_URL}|Open>)"
					slackSend channel: '#bots', color: 'warning', message: "Code Dx Jenkins Plugin build unstable (<${env.BUILD_URL}|Open>)"
				}

				failure {
					slackSend channel: '#devchat', color: 'danger', message: "Code Dx Jenkins Plugin build failed (<${env.BUILD_URL}|Open>)"
					slackSend channel: '#bots', color: 'danger', message: "Code Dx Jenkins Plugin build failed (<${env.BUILD_URL}|Open>)"
				}
			}
        }
    }
}