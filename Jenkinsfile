pipeline {
    agent any

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
