pipeline {
    agent any

    triggers {
        cron('H H * * 1') // Ejecución semanal (Lunes de madrugada)
    }

    environment {
        ANDROID_HOME = "/opt/android-sdk" // Adjust this to your Jenkins node path
        PATH = "${env.ANDROID_HOME}/tools:${env.ANDROID_HOME}/platform-tools:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare') {
            steps {
                sh "chmod +x gradlew"
            }
        }

        stage('Lint & Static Analysis') {
            steps {
                sh "./gradlew lintDebug"
            }
            post {
                always {
                    androidLint(pattern: '**/build/reports/lint-results-*.xml')
                }
            }
        }

        stage('Unit Tests') {
            steps {
                sh "./gradlew testDebugUnitTest"
            }
            post {
                always {
                    junit '**/build/test-results/**/*.xml'
                }
            }
        }

        stage('Build APK & AAB') {
            steps {
                sh "./gradlew assembleDebug"
                sh "./gradlew bundleRelease"
            }
        }

        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/*.apk, app/build/outputs/bundle/release/*.aab', fingerprint: true
            }
        }

        stage('Health Report') {
            steps {
                sh "./gradlew generateHealthReport"
            }
            post {
                success {
                    archiveArtifacts artifacts: 'build/reports/health/index.html', fingerprint: true
                    echo "Health Report archived. You can find it in the artifacts section."
                }
            }
        }
    }

    post {
        failure {
            echo "Pipeline failed. Check logs."
        }
        success {
            echo "Pipeline finished successfully!"
        }
    }
}
