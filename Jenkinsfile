pipeline {
    agent {
        docker {
            image 'markhobson/maven-chrome'
            args '-u root:root -v /var/lib/jenkins/.m2:/root/.m2'
        }
    }
    
    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'main', 
                    url: 'https://github.com/arhamheer/Jenkins-Pipeline-Test-Stage.git'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn clean test -U'
            }
        }
        
        stage('Publish Test Results') {
            steps {
                junit 'target/surefire-reports/*.xml'
            }
        }
    }
    
    post {
        always {
            script {
                sh "git config --global --add safe.directory ${env.WORKSPACE}"
                
                def committer = sh(
                    script: "git log -1 --pretty=format:'%ae'",
                    returnStdout: true
                ).trim()
                
                def raw = sh(
                    script: "grep -h '<testcase' target/surefire-reports/*.xml || true",
                    returnStdout: true
                ).trim()
                
                int total = 0
                int passed = 0
                int failed = 0
                int skipped = 0
                def details = ""
                
                if (raw) {
                    raw.split('\n').each { line ->
                        total++
                        def matcher = (line =~ /name="([^"]+)"/)
                        def name = matcher ? matcher[0][1] : "Unknown"
                        
                        if (line.contains("<failure")) {
                            failed++
                            details += "${name} FAILED\n"
                        } else if (line.contains("<skipped") || line.contains("</skipped>")) {
                            skipped++
                            details += "${name} SKIPPED\n"
                        } else {
                            passed++
                            details += "${name} PASSED\n"
                        }
                    }
                }
                
                def emailBody = """
Test Summary (Build #${env.BUILD_NUMBER})
Total Tests: ${total}
Passed: ${passed}
Failed: ${failed}
Skipped: ${skipped}

Detailed Results:
${details}
"""
                
                emailext(
                    to: "${committer}",
                    subject: "Build #${env.BUILD_NUMBER} - Test Results",
                    body: emailBody
                )
            }
        }
    }
}