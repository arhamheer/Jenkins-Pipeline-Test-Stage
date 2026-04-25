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
                git branch: 'main', url: 'https://github.com/malik-qasim/JavaMaven.git'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Publish Test Results') {
            steps {
                junit '**/target/surefire-reports/*.xml'
            }
        }
    }

    post {
        always {
            script {
                sh "git config --global --add safe.directory ${env.WORKSPACE}"
                // Always notify collaborators; append committers when available.
                def collaboratorRecipients = "qasimalik@gmail.com,mahrarihat@gmail.com"
                def emailsRaw = ""

                if (env.GIT_PREVIOUS_SUCCESSFUL_COMMIT?.trim() && env.GIT_COMMIT?.trim()) {
                    emailsRaw = sh(
                        script: "git log --format='%ae' ${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT}..${env.GIT_COMMIT}",
                        returnStdout: true
                    ).trim()
                }

                if (!emailsRaw) {
                    emailsRaw = sh(
                        script: "git log -1 --pretty=format:'%ae'",
                        returnStdout: true
                    ).trim()
                }

                def committerRecipients = emailsRaw
                    .split('\n')
                    .collect { it.trim().toLowerCase() }
                    .findAll { it ==~ /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$/ }
                    .unique()

                def recipients = (collaboratorRecipients.split(',')
                    .collect { it.trim().toLowerCase() }
                    .findAll { it ==~ /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/ }) + committerRecipients

                def recipientList = recipients.unique().join(',')

                echo "Resolved committer recipient(s): ${recipientList ?: 'none'}"

                def raw = sh(
                    script: "grep -h \"<testcase\" target/surefire-reports/*.xml",
                    returnStdout: true
                ).trim()

                int total = 0
                int passed = 0
                int failed = 0
                int skipped = 0

                def details = ""

                raw.split('\n').each { line ->
                    total++

                    def name = (line =~ /name=\"([^\"]+)\"/)[0][1]

                    if (line.contains("<failure")) {
                        failed++
                        details += "${name} — FAILED\n"
                    } else if (line.contains("<skipped") || line.contains("</skipped>")) {
                        skipped++
                        details += "${name} — SKIPPED\n"
                    } else {
                        passed++
                        details += "${name} — PASSED\n"
                    }
                }

                def emailBody = """
Test Summary (Build #${env.BUILD_NUMBER})

Total Tests:   ${total}
Passed:        ${passed}
Failed:        ${failed}
Skipped:       ${skipped}

Detailed Results:
${details}

"""

                if (recipientList) {
                    try {
                        emailext(
                            to: recipientList,
                            from: 'mahrarihat@gmail.com',
                            replyTo: 'mahrarihat@gmail.com',
                            subject: "Build #${env.BUILD_NUMBER} Test Results",
                            body: emailBody
                        )
                        echo "Email sent to: ${recipientList}"
                    } catch (Exception e) {
                        echo "Email notification failed: ${e.getMessage()}"
                    }
                } else {
                    echo "No valid committer email found in git metadata. Skipping email notification."
                }
            }
        }
    }
}