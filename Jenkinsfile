pipeline {
    agent {
        docker {
            image 'markhobson/maven-chrome'
            args '--shm-size=2g -u root:root -v /var/lib/jenkins/.m2:/root/.m2'
        }
    }

    stages {
        stage('Clone Repository') {
            steps {
                checkout scm
            }
        }

        stage('Chrome Smoke Test') {
            steps {
                sh '''
                    set -eux
                    google-chrome --version
                    chromedriver --version
                    timeout 30s google-chrome \
                      --headless \
                      --no-sandbox \
                      --disable-setuid-sandbox \
                      --disable-dev-shm-usage \
                      --disable-gpu \
                      --disable-software-rasterizer \
                      --disable-background-networking \
                      --disable-component-update \
                      --disable-default-apps \
                      --disable-features=TranslateUI,MediaRouter,OptimizationHints,VizDisplayCompositor \
                      --disable-renderer-backgrounding \
                      --disable-ipc-flooding-protection \
                      --no-first-run \
                      --no-default-browser-check \
                      --user-data-dir=/tmp/chrome-smoke-${BUILD_NUMBER} \
                      --dump-dom about:blank
                '''
            }
        }

        stage('Test') {
            steps {
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    sh 'mvn -B test'
                }
            }
        }

        stage('Publish Test Results') {
            steps {
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
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

                int total = 0
                int passed = 0
                int failed = 0
                int skipped = 0
                def details = ""

                def reportList = sh(
                    script: "find . -path '*/target/surefire-reports/*.xml' -type f 2>/dev/null | sort -u",
                    returnStdout: true
                ).trim()

                reportList.split('\n').findAll { it }.each { path ->
                    def cleanedPath = path.replaceFirst('^\\.\\/', '')
                    def xml = readFile(file: cleanedPath)
                    def testcases = (xml =~ /(?s)<testcase\b[^>](?:\/>|>.?<\/testcase>)/)
                    testcases.each { match ->
                        def testcase = match[0]
                        total++
                        def classMatch = (testcase =~ /classname="([^"]*)"/)
                        def testMatch = (testcase =~ /name="([^"]*)"/)
                        def className = classMatch.find() ? classMatch.group(1) : ""
                        def testName = testMatch.find() ? testMatch.group(1) : "unknown"
                        def name = className ? "${className}.${testName}" : testName
                        if (testcase.contains("<failure") || testcase.contains("<error")) {
                            failed++
                            details += "${name} - FAILED\n"
                        } else if (testcase.contains("<skipped")) {
                            skipped++
                            details += "${name} - SKIPPED\n"
                        } else {
                            passed++
                            details += "${name} - PASSED\n"
                        }
                    }
                }

                if (!details) {
                    details = "No Surefire test report was found.\n"
                }

                def emailBody = """Test Summary (Build #${env.BUILD_NUMBER})

Total Tests:   ${total}
Passed:        ${passed}
Failed:        ${failed}
Skipped:       ${skipped}

Detailed Results:
${details}
"""

                emailext(
                    to: committer,
                    subject: "Build #${env.BUILD_NUMBER} Test Results",
                    body: emailBody
                )
            }
        }
    }
}